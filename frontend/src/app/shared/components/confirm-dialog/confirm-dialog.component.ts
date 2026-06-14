import { Component, Inject }   from '@angular/core';
import { CommonModule }        from '@angular/common';
import { MatButtonModule }     from '@angular/material/button';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule }       from '@angular/material/icon';

export interface ConfirmDialogData {
  title:       string;
  message:     string;
  confirmText?: string;   // default: 'Confirm'
  cancelText?:  string;   // default: 'Cancel'
  danger?:      boolean;  // true → red confirm button
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
  ],
  template: `
    <div class="confirm-dialog">
      <div class="dialog-header" [class.danger]="data.danger">
        <mat-icon>{{ data.danger ? 'warning' : 'help_outline' }}</mat-icon>
        <h2 mat-dialog-title>{{ data.title }}</h2>
      </div>

      <mat-dialog-content>
        <p>{{ data.message }}</p>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-stroked-button
                [mat-dialog-close]="false">
          {{ data.cancelText || 'Cancel' }}
        </button>
        <button mat-flat-button
                [color]="data.danger ? 'warn' : 'primary'"
                [mat-dialog-close]="true">
          {{ data.confirmText || 'Confirm' }}
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .confirm-dialog {
      min-width: 360px;
      max-width: 480px;
    }

    .dialog-header {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 20px 24px 0;

      mat-icon {
        font-size: 28px;
        width: 28px;
        height: 28px;
        color: var(--okane-primary);
      }

      h2 {
        margin: 0;
        font-size: 1.2rem;
        font-weight: 600;
      }

      &.danger mat-icon {
        color: var(--okane-danger);
      }
    }

    mat-dialog-content p {
      color: var(--okane-text-light);
      line-height: 1.6;
      margin: 0;
    }

    mat-dialog-actions {
      padding: 16px 24px;
      gap: 8px;
    }
  `]
})
export class ConfirmDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData,
  ) {}
}
