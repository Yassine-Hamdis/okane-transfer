import {
  Component, OnInit, OnDestroy,
} from '@angular/core';
import { CommonModule }        from '@angular/common';
import { RouterModule }        from '@angular/router';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { MatStepperModule }         from '@angular/material/stepper';
import { MatFormFieldModule }       from '@angular/material/form-field';
import { MatInputModule }           from '@angular/material/input';
import { MatSelectModule }          from '@angular/material/select';
import { MatButtonModule }          from '@angular/material/button';
import { MatIconModule }            from '@angular/material/icon';
import { MatCardModule }            from '@angular/material/card';
import { MatRadioModule }           from '@angular/material/radio';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule }         from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import {
  Subject, debounceTime, distinctUntilChanged, takeUntil,
} from 'rxjs';

import { CorridorService }         from '../../../core/services/corridor.service';
import { FeeGridService }          from '../../../core/services/fee-grid.service';
import { TransferService }         from '../../../core/services/transfer.service';
import { CountryService }          from '../../../core/services/country.service';
import { Corridor }                from '../../../core/models/corridor.model';
import { Country }                 from '../../../core/models/country.model';
import { FeeSimulationResponse, TransferType } from '../../../core/models/fee-grid.model';
import { Transfer }                from '../../../core/models/transfer.model';
import { CurrencyFormatPipe }      from '../../../shared/pipes/currency-format.pipe';
// import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { MobileMoneyService } from '../../../core/services/mobile-money.service';
import { MobileMoneyOperator } from '../../../core/models/mobile-money.model';


@Component({
  selector: 'app-send-transfer',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatStepperModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatRadioModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatSnackBarModule,
    CurrencyFormatPipe,
    // LoadingSpinnerComponent,
  ],
  templateUrl: './send-transfer.component.html',
  styleUrls:   ['./send-transfer.component.scss'],
})
export class SendTransferComponent implements OnInit, OnDestroy {

  // ── Steps ────────────────────────────────────────────────────────────────────
  corridorForm!: FormGroup;   // Step 1
  senderForm!:   FormGroup;   // Step 3
  recipientForm!: FormGroup;  // Step 4

  // ── Data ─────────────────────────────────────────────────────────────────────
  corridors:   Corridor[] = [];
  countries:   Country[]  = [];
  selectedCorridor: Corridor | null = null;

  // ── Fee simulation ────────────────────────────────────────────────────────────
  simulation:       FeeSimulationResponse | null = null;
  simulating        = false;
  simulationError   = '';

  // ── Transfer types ────────────────────────────────────────────────────────────
  readonly transferTypes: { value: TransferType; label: string; icon: string }[] = [
    { value: 'STANDARD',     label: 'Standard',     icon: 'schedule' },
    { value: 'EXPRESS',      label: 'Express',      icon: 'flash_on' },
    { value: 'MOBILE_MONEY', label: 'Mobile Money', icon: 'phone_android' },
  ];

    // ── Mobile Money Operators ────────────────────────────────────────────────────
  readonly mobileMoneyOperators: { value: MobileMoneyOperator; label: string }[] = [
    { value: 'ORANGE_MONEY', label: 'Orange Money' },
    { value: 'WAVE',         label: 'Wave' },
    { value: 'M_PESA',       label: 'M-Pesa' },
  ];

  // ── Success state ─────────────────────────────────────────────────────────────
  submitting        = false;
  createdTransfer:  Transfer | null = null;

  private destroy$ = new Subject<void>();
  private amountChange$ = new Subject<number>();

  constructor(
    private fb:              FormBuilder,
    private corridorService: CorridorService,
    private feeGridService:  FeeGridService,
    private transferService: TransferService,
    private countryService:  CountryService,
    private mobileMoneyService: MobileMoneyService,
    private snackBar:        MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.buildForms();
    this.loadData();
    this.setupSimulation();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ── Form builders ─────────────────────────────────────────────────────────────

  private buildForms(): void {
    this.corridorForm = this.fb.group({
      corridorId:   [null, Validators.required],
      transferType: ['STANDARD', Validators.required],
      sentAmount:   [null, [Validators.required, Validators.min(1)]],
    });

    this.senderForm = this.fb.group({
      senderFirstName:  ['', Validators.required],
      senderLastName:   ['', Validators.required],
      senderPhone:      ['', Validators.required],
      senderIdNumber:   ['', Validators.required],
      senderEmail:      ['', [Validators.email]],
      senderCountryId:  [null, Validators.required],
    });

    this.recipientForm = this.fb.group({
      recipientFirstName: ['', Validators.required],
      recipientLastName:  ['', Validators.required],
      recipientPhone:     ['', Validators.required],
      recipientCountryId: [null, Validators.required],
      notes:              [''],
      // ── Mobile Money fields ──
      mobileMoneyOperator: [null],
      mobileMoneyWalletPhone: [''],
    });

    // ── Add validators when MOBILE_MONEY is selected ──
    this.corridorForm.get('transferType')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(type => {
        const operatorControl = this.recipientForm.get('mobileMoneyOperator')!;
        const walletControl = this.recipientForm.get('mobileMoneyWalletPhone')!;

        if (type === 'MOBILE_MONEY') {
          operatorControl.setValidators([Validators.required]);
          walletControl.setValidators([Validators.required]);
        } else {
          operatorControl.clearValidators();
          walletControl.clearValidators();
        }

        operatorControl.updateValueAndValidity();
        walletControl.updateValueAndValidity();
      });
  }

  // ── Data loading ──────────────────────────────────────────────────────────────

  private loadData(): void {
    this.corridorService.getActive().subscribe(
      corridors => this.corridors = corridors
    );
    this.countryService.getActive().subscribe(
      countries => this.countries = countries
    );
  }

  // ── Live fee simulation ───────────────────────────────────────────────────────

  private setupSimulation(): void {
    // Watch amount field
    this.corridorForm.get('sentAmount')!.valueChanges
      .pipe(
        debounceTime(600),
        distinctUntilChanged(),
        takeUntil(this.destroy$),
      )
      .subscribe(() => this.runSimulation());

    // Watch corridor/type changes
    this.corridorForm.get('corridorId')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.onCorridorChange();
        this.runSimulation();
      });

    this.corridorForm.get('transferType')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.runSimulation());
  }

  onCorridorChange(): void {
    const id = this.corridorForm.value.corridorId;
    this.selectedCorridor = this.corridors.find(c => c.id === id) ?? null;
    this.simulation       = null;
  }

  private runSimulation(): void {
    const { corridorId, transferType, sentAmount } = this.corridorForm.value;

    // ── Skip simulation for MOBILE_MONEY ──
    if (transferType === 'MOBILE_MONEY') {
      this.simulation = null;
      this.simulating = false;
      return;
    }

    if (!corridorId || !sentAmount || sentAmount <= 0) {
      this.simulation = null;
      return;
    }

    const corridor = this.corridors.find(c => c.id === corridorId);
    if (!corridor) return;

    this.simulating      = true;
    this.simulationError = '';

    this.feeGridService.simulate({
      corridorId,
      currencyId:   corridor.sourceCurrencyId,
      amount:       sentAmount,
      transferType,
    }).subscribe({
      next: sim => {
        this.simulation = sim;
        this.simulating = false;
      },
      error: err => {
        this.simulating      = false;
        this.simulationError = err.error?.message ?? 'Could not simulate fees';
        this.simulation      = null;
      },
    });
  }

  // ── Submit ────────────────────────────────────────────────────────────────────

  onSubmit(): void {
    if (
      this.corridorForm.invalid ||
      this.senderForm.invalid   ||
      this.recipientForm.invalid
    ) return;

    this.submitting = true;
    const cf = this.corridorForm.value;
    const sf = this.senderForm.value;
    const rf = this.recipientForm.value;

    const corridor = this.corridors.find(c => c.id === cf.corridorId)!;

    // Build transfer payload - ALWAYS include sentCurrencyId
    const transferPayload = {
      ...sf,
      ...rf,
      corridorId:      cf.corridorId,
      sentAmount:      cf.sentAmount,
      sentCurrencyId:  corridor.sourceCurrencyId, // ← Always include this
      transferType:    cf.transferType,
    };

    console.log('🚀 Transfer Payload:', transferPayload);

    // Create the base transfer first
    this.transferService.create(transferPayload).subscribe({
      next: transfer => {
        // If MOBILE_MONEY, create the mobile money record
        if (cf.transferType === 'MOBILE_MONEY') {
          this.createMobileMoneyRecord(transfer.id, rf);
        } else {
          this.submitting = false;
          this.createdTransfer = transfer;
        }
      },
      error: err => {
        this.submitting = false;
        this.snackBar.open(
          err.error?.message ?? 'Transfer failed', 'OK', { duration: 5000 }
        );
      },
    });
  }

  private createMobileMoneyRecord(transferId: number, recipientData: any): void {
    this.mobileMoneyService.create({
      transferId,
      operator:    recipientData.mobileMoneyOperator,
      walletPhone: recipientData.mobileMoneyWalletPhone,
    }).subscribe({
      next: () => {
        this.submitting = false;
        // Fetch the transfer again to get the complete data
        this.transferService.getById(transferId).subscribe({
          next: transfer => {
            this.createdTransfer = transfer;
            this.snackBar.open(
              'Mobile Money transfer created successfully!',
              'OK',
              { duration: 3000 }
            );
          },
          error: () => {
            // Even if we can't fetch, we know it was created
            this.snackBar.open(
              'Mobile Money transfer created!',
              'OK',
              { duration: 3000 }
            );
          }
        });
      },
      error: err => {
        this.submitting = false;
        this.snackBar.open(
          err.error?.message ?? 'Mobile Money creation failed',
          'OK',
          { duration: 5000 }
        );
      },
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────────

  reset(): void {
    this.createdTransfer  = null;
    this.simulation       = null;
    this.selectedCorridor = null;
    this.corridorForm.reset({ transferType: 'STANDARD' });
    this.senderForm.reset();
    this.recipientForm.reset();
  }

  isMobileMoneySelected(): boolean {
    return this.corridorForm.value.transferType === 'MOBILE_MONEY';
  }

  getCorridorLabel(corridor: Corridor): string {
    return `${corridor.sourceCountryName} → ${corridor.destinationCountryName} `
         + `(${corridor.sourceCurrencyCode} → ${corridor.destinationCurrencyCode})`;
  }

  getSenderCountryLabel(): string {
    const id = this.senderForm.value.senderCountryId;
    return this.countries.find(c => c.id === id)?.name ?? '';
  }

  getRecipientCountryLabel(): string {
    const id = this.recipientForm.value.recipientCountryId;
    return this.countries.find(c => c.id === id)?.name ?? '';
  }

  isMobileMoneyTransfer(): boolean {
  return this.createdTransfer?.transferType === 'MOBILE_MONEY';
}
}
