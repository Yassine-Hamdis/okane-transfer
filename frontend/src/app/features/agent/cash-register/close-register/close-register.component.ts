import { Component }          from '@angular/core';
import { CommonModule }       from '@angular/common';
import { RouterModule }       from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { MatCardModule }      from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule }     from '@angular/material/input';
import { MatButtonModule }    from '@angular/material/button';
import { MatIconModule }      from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router }             from '@angular/router';

import { CashService }        from '../../../../core/services/cash.service';

@Component({
  selector: 'app-close-register',
  standalone: true,
  imports: [
    CommonModule, RouterModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatSnackBarModule,
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <div style="display:flex;align-items:center;gap:8px">
          <a mat-icon-button routerLink="/agent/cash">
            <mat-icon>arrow_back</mat-icon>
          </a>
          <h1>Close Register</h1>
        </div>
      </div>

      <mat-card style="max-width:480px;border-radius:16px !important">
        <div style="text-align:center;padding:24px 0 16px">
          <div style="width:64px;height:64px;background:#ffebee;border-radius:50%;display:flex;align-items:center;justify-content:center;margin:0 auto 12px">
            <mat-icon style="color:#c62828;font-size:32px;width:32px;height:32px">lock</mat-icon>
          </div>
          <h2 style="font-size:1.3rem;font-weight:700;margin-bottom:4px">Close Cash Register</h2>
          <p class="text-muted" style="margin:0;font-size:0.875rem">
            This will end the current shift. All balances will be recorded.
          </p>
        </div>

        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" style="width:100%">
              <mat-label>Closing Note (optional)</mat-label>
              <textarea matInput formControlName="note" rows="3"
                        placeholder="Any discrepancies or notes..."></textarea>
            </mat-form-field>

            <div style="display:flex;gap:12px;justify-content:flex-end;margin-top:8px">
              <button mat-stroked-button type="button" routerLink="/agent/cash">
                Cancel
              </button>
              <button mat-flat-button color="warn" type="submit" [disabled]="closing">
                <mat-icon>lock</mat-icon>
                {{ closing ? 'Closing...' : 'Close Register' }}
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
})
export class CloseRegisterComponent {

  form:    FormGroup;
  closing = false;

  constructor(
    private cashService: CashService,
    private fb:          FormBuilder,
    private snackBar:    MatSnackBar,
    private router:      Router,
  ) {
    this.form = this.fb.group({ note: [''] });
  }

  onSubmit(): void {
    this.closing = true;
    this.cashService.closeRegister({ note: this.form.value.note || null })
      .subscribe({
        next: msg => {
          this.closing = false;
          this.snackBar.open(msg || 'Register closed', 'OK', { duration: 3000 });
          this.router.navigate(['/agent/cash']);
        },
        error: err => {
          this.closing = false;
          this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 });
        },
      });
  }
}
