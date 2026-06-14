import { Component, Input } from '@angular/core';
import { CommonModule }     from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [CommonModule, MatProgressSpinnerModule],
  template: `
    <div class="spinner-overlay" [class.inline]="inline">
      <mat-spinner [diameter]="diameter" color="primary" />
      <p *ngIf="message" class="spinner-message">{{ message }}</p>
    </div>
  `,
  styles: [`
    .spinner-overlay {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 48px;
      width: 100%;

      &.inline {
        padding: 16px;
      }
    }

    .spinner-message {
      margin-top: 16px;
      color: var(--okane-text-light);
      font-size: 0.9rem;
    }
  `]
})
export class LoadingSpinnerComponent {
  @Input() diameter = 48;
  @Input() message  = '';
  @Input() inline   = false;
}
