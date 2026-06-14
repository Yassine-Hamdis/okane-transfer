import { Component, Input } from '@angular/core';
import { CommonModule }     from '@angular/common';
import { TransferStatus }   from '../../../core/models/transfer.model';
import { KycStatus }        from '../../../core/models/kyc.model';
import { MobileMoneyStatus } from '../../../core/models/mobile-money.model';

type BadgeStatus = TransferStatus | KycStatus | MobileMoneyStatus | string;

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="badge" [ngClass]="badgeClass">
      {{ label }}
    </span>
  `,
  styles: [`
    .badge {
      display: inline-flex;
      align-items: center;
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 0.72rem;
      font-weight: 700;
      letter-spacing: 0.6px;
      text-transform: uppercase;
      white-space: nowrap;
    }

    /* Transfer statuses */
    .status-pending     { background: #e3f2fd; color: #0277bd; }
    .status-paid        { background: #e8f5e9; color: #2e7d32; }
    .status-cancelled   { background: #ffebee; color: #c62828; }
    .status-expired     { background: #f5f5f5; color: #757575; }

    /* KYC statuses */
    .status-passed      { background: #e8f5e9; color: #2e7d32; }
    .status-flagged     { background: #fff3e0; color: #e65100; }
    .status-blocked     { background: #ffebee; color: #c62828; }

    /* Mobile Money statuses */
    .status-mm-pending      { background: #e3f2fd; color: #0277bd; }
    .status-mm-sent         { background: #e8f5e9; color: #2e7d32; }
    .status-mm-reconciled   { background: #ede7f6; color: #4527a0; }
    .status-mm-failed       { background: #ffebee; color: #c62828; }

    /* Generic */
    .status-active      { background: #e8f5e9; color: #2e7d32; }
    .status-inactive    { background: #f5f5f5; color: #757575; }
    .status-default     { background: #f5f5f5; color: #424242; }
  `]
})
export class StatusBadgeComponent {

  @Input() set status(value: BadgeStatus) {
    this._status = value;
    this.updateBadge(value);
  }

  private _status: BadgeStatus = '';
  badgeClass = 'status-default';
  label      = '';

  private updateBadge(status: BadgeStatus): void {
    switch (status) {
      // Transfer statuses
      case 'EN_ATTENTE':
        this.badgeClass = 'status-pending';
        this.label      = 'Pending';
        break;
      case 'PAYE':
        this.badgeClass = 'status-paid';
        this.label      = 'Paid';
        break;
      case 'ANNULE':
        this.badgeClass = 'status-cancelled';
        this.label      = 'Cancelled';
        break;
      case 'EXPIRE':
        this.badgeClass = 'status-expired';
        this.label      = 'Expired';
        break;

      // KYC statuses
      case 'PASSED':
        this.badgeClass = 'status-passed';
        this.label      = 'Passed';
        break;
      case 'FLAGGED':
        this.badgeClass = 'status-flagged';
        this.label      = 'Flagged';
        break;
      case 'BLOCKED':
        this.badgeClass = 'status-blocked';
        this.label      = 'Blocked';
        break;

      // Mobile Money statuses
      case 'PENDING':
        this.badgeClass = 'status-mm-pending';
        this.label      = 'Pending';
        break;
      case 'SENT':
        this.badgeClass = 'status-mm-sent';
        this.label      = 'Sent';
        break;
      case 'RECONCILED':
        this.badgeClass = 'status-mm-reconciled';
        this.label      = 'Reconciled';
        break;
      case 'FAILED':
        this.badgeClass = 'status-mm-failed';
        this.label      = 'Failed';
        break;

      // Boolean-like
      case 'ACTIVE':
        this.badgeClass = 'status-active';
        this.label      = 'Active';
        break;
      case 'INACTIVE':
        this.badgeClass = 'status-inactive';
        this.label      = 'Inactive';
        break;

      default:
        this.badgeClass = 'status-default';
        this.label      = status || '—';
    }
  }
}
