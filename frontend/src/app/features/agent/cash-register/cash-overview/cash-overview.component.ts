import { Component, OnInit }       from '@angular/core';
import { CommonModule }            from '@angular/common';
import { RouterModule }            from '@angular/router';
import { MatCardModule }           from '@angular/material/card';
import { MatButtonModule }         from '@angular/material/button';
import { MatIconModule }           from '@angular/material/icon';
import { MatDividerModule }        from '@angular/material/divider';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule }      from '@angular/material/form-field';
import { MatInputModule }          from '@angular/material/input';
import { MatSelectModule }         from '@angular/material/select';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { CashService }              from '../../../../core/services/cash.service';
import { CurrencyService }          from '../../../../core/services/currency.service';
import { CashRegisterResponse }     from '../../../../core/models/cash.model';
import { Currency }                 from '../../../../core/models/currency.model';
import { LoadingSpinnerComponent }  from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent }   from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { CurrencyFormatPipe }       from '../../../../shared/pipes/currency-format.pipe';

@Component({
  selector: 'app-cash-overview',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule,
    LoadingSpinnerComponent,
    CurrencyFormatPipe,
  ],
  templateUrl: './cash-overview.component.html',
  styleUrls:   ['./cash-overview.component.scss'],
})
export class CashOverviewComponent implements OnInit {

  loading:      boolean               = true;
  register:     CashRegisterResponse | null = null;
  currencies:   Currency[]            = [];
  showDiscrepancyForm = false;
  discrepancyForm!:   FormGroup;
  saving        = false;

  constructor(
    private cashService:     CashService,
    private currencyService: CurrencyService,
    private fb:              FormBuilder,
    private dialog:          MatDialog,
    private snackBar:        MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.buildDiscrepancyForm();
    this.loadData();
  }

  private buildDiscrepancyForm(): void {
    this.discrepancyForm = this.fb.group({
      currencyId: [null, Validators.required],
      amount:     [null, [Validators.required, Validators.min(0.01)]],
      note:       ['',   Validators.required],
    });
  }

  loadData(): void {
    this.loading = true;
    this.cashService.getMyRegister().subscribe({
      next: register => {
        this.register = register;
        this.loading  = false;
      },
      error: () => { this.loading = false; },
    });

    this.currencyService.getActive().subscribe(
      currencies => this.currencies = currencies
    );
  }

  closeRegister(): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title:       'Close Register',
        message:     'Are you sure you want to close the cash register for today? This action will be logged.',
        confirmText: 'Close Register',
        danger:      true,
      },
    });

    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.cashService.closeRegister({ note: null }).subscribe({
        next: msg => {
          this.snackBar.open(msg || 'Register closed', 'OK', { duration: 3000 });
          this.loadData();
        },
        error: err =>
          this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 }),
      });
    });
  }

  submitDiscrepancy(): void {
    if (this.discrepancyForm.invalid) {
      this.discrepancyForm.markAllAsTouched();
      return;
    }
    this.saving = true;
    this.cashService.reportDiscrepancy(this.discrepancyForm.value).subscribe({
      next: msg => {
        this.saving              = false;
        this.showDiscrepancyForm = false;
        this.discrepancyForm.reset();
        this.snackBar.open(msg || 'Discrepancy reported', 'OK', { duration: 3000 });
      },
      error: err => {
        this.saving = false;
        this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 });
      },
    });
  }
}
