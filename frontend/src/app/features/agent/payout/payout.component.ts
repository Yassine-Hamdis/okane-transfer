import { Component }            from '@angular/core';
import { CommonModule }         from '@angular/common';
import { RouterModule }         from '@angular/router';
import {
  ReactiveFormsModule, FormBuilder, FormGroup, Validators,
} from '@angular/forms';
import { MatCardModule }        from '@angular/material/card';
import { MatFormFieldModule }   from '@angular/material/form-field';
import { MatInputModule }       from '@angular/material/input';
import { MatButtonModule }      from '@angular/material/button';
import { MatIconModule }        from '@angular/material/icon';
import { MatDividerModule }     from '@angular/material/divider';
import { MatStepperModule }     from '@angular/material/stepper';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { HttpErrorResponse }    from '@angular/common/http';

import { TransferService }         from '../../../core/services/transfer.service';
import { Transfer }                from '../../../core/models/transfer.model';
import { StatusBadgeComponent }    from '../../../shared/components/status-badge/status-badge.component';
import { CurrencyFormatPipe }      from '../../../shared/pipes/currency-format.pipe';

type PayoutStep = 'search' | 'verify' | 'success';

@Component({
  selector: 'app-payout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatStepperModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    StatusBadgeComponent,
    CurrencyFormatPipe,
  ],
  templateUrl: './payout.component.html',
  styleUrls:   ['./payout.component.scss'],
})
export class PayoutComponent {

  step: PayoutStep    = 'search';
  searching           = false;
  paying              = false;
  errorMessage        = '';

  foundTransfer: Transfer | null = null;
  paidTransfer:  Transfer | null = null;

  searchForm: FormGroup;
  verifyForm: FormGroup;

  constructor(
    private fb:              FormBuilder,
    private transferService: TransferService,
    private snackBar:        MatSnackBar,
  ) {
    this.searchForm = this.fb.group({
      code: ['', [Validators.required, Validators.minLength(6)]],
    });

    this.verifyForm = this.fb.group({
      recipientIdNumber: ['', Validators.required],
    });
  }

  // ── Step 1: Search ────────────────────────────────────────────────────────────

  onSearch(): void {
    if (this.searchForm.invalid) { this.searchForm.markAllAsTouched(); return; }
    this.searching    = true;
    this.errorMessage = '';

    this.transferService
      .getByCode(this.searchForm.value.code.trim().toUpperCase())
      .subscribe({
        next: transfer => {
          this.searching     = false;
          this.foundTransfer = transfer;

          if (transfer.status !== 'EN_ATTENTE') {
            this.errorMessage = `This transfer is ${transfer.status} and cannot be paid out.`;
            this.foundTransfer = null;
            return;
          }
          this.step = 'verify';
        },
        error: (err: HttpErrorResponse) => {
          this.searching    = false;
          this.errorMessage = err.error?.message ?? 'Transfer not found.';
        },
      });
  }

  // ── Step 2: Verify & Pay ──────────────────────────────────────────────────────

  onPayout(): void {
    if (this.verifyForm.invalid || !this.foundTransfer) {
      this.verifyForm.markAllAsTouched();
      return;
    }

    this.paying       = true;
    this.errorMessage = '';

    this.transferService.payout({
      withdrawalCode:    this.foundTransfer.withdrawalCode,
      recipientIdNumber: this.verifyForm.value.recipientIdNumber,
    }).subscribe({
      next: transfer => {
        this.paying       = false;
        this.paidTransfer = transfer;
        this.step         = 'success';
      },
      error: (err: HttpErrorResponse) => {
        this.paying       = false;
        this.errorMessage = err.error?.message ?? 'Payout failed.';
      },
    });
  }

  // ── Reset ─────────────────────────────────────────────────────────────────────

  reset(): void {
    this.step           = 'search';
    this.foundTransfer  = null;
    this.paidTransfer   = null;
    this.errorMessage   = '';
    this.searchForm.reset();
    this.verifyForm.reset();
  }

  goBack(): void {
    this.step          = 'search';
    this.foundTransfer = null;
    this.errorMessage  = '';
    this.verifyForm.reset();
  }
}
