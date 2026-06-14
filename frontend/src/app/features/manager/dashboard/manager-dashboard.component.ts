import { Component, OnInit }       from '@angular/core';
import { CommonModule }            from '@angular/common';
import { RouterModule }            from '@angular/router';
import { MatCardModule }           from '@angular/material/card';
import { MatButtonModule }         from '@angular/material/button';
import { MatIconModule }           from '@angular/material/icon';
import { forkJoin }                from 'rxjs';

import { UserService }             from '../../../core/services/user.service';
import { TransferService }         from '../../../core/services/transfer.service';
import { AuthService }             from '../../../core/services/auth.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
// import { StatusBadgeComponent }    from '../../../shared/components/status-badge/status-badge.component';
// import { CurrencyFormatPipe }      from '../../../shared/pipes/currency-format.pipe';

@Component({
  selector: 'app-manager-dashboard',
  standalone: true,
  imports: [
    CommonModule, RouterModule,
    MatCardModule, MatButtonModule, MatIconModule,
    LoadingSpinnerComponent,
    //  StatusBadgeComponent, CurrencyFormatPipe,
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Manager Dashboard</h1>
        <p class="text-muted">{{ today | date:'fullDate' }}</p>
      </div>

      <app-loading-spinner *ngIf="loading" />

      <ng-container *ngIf="!loading">
        <div class="stats-grid mb-24">
          <mat-card class="stat-card" *ngFor="let s of stats">
            <div class="stat-icon" [style.background]="s.color + '22'">
              <mat-icon [style.color]="s.color">{{ s.icon }}</mat-icon>
            </div>
            <div>
              <span class="stat-value">{{ s.value }}</span>
              <span class="stat-label">{{ s.label }}</span>
            </div>
          </mat-card>
        </div>

        <div class="quick-links-grid">
          <mat-card class="quick-link-card" routerLink="/manager/agents">
            <mat-icon>badge</mat-icon>
            <span>Manage Agents</span>
            <mat-icon>chevron_right</mat-icon>
          </mat-card>
          <mat-card class="quick-link-card" routerLink="/manager/transfers">
            <mat-icon>swap_horiz</mat-icon>
            <span>View Transfers</span>
            <mat-icon>chevron_right</mat-icon>
          </mat-card>
        </div>
      </ng-container>
    </div>
  `,
  styles: [`
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 16px;
    }
    .stat-card {
      display: flex; align-items: center; gap: 16px;
      padding: 20px !important; border-radius: 12px !important;
    }
    .stat-icon {
      width: 48px; height: 48px; border-radius: 12px;
      display: flex; align-items: center; justify-content: center; flex-shrink: 0;
      mat-icon { font-size: 24px; width: 24px; height: 24px; }
    }
    .stat-value { display: block; font-size: 1.8rem; font-weight: 700; line-height: 1; }
    .stat-label { font-size: 0.78rem; color: var(--okane-text-light); margin-top: 4px; display: block; }
    .quick-links-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    .quick-link-card {
      display: flex; align-items: center; gap: 12px; padding: 20px !important;
      border-radius: 12px !important; cursor: pointer;
      &:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.12) !important; }
      span { flex: 1; font-weight: 600; }
    }
    .mb-24 { margin-bottom: 24px; }
  `]
})
export class ManagerDashboardComponent implements OnInit {

  loading = true;
  stats:  { label: string; value: number; icon: string; color: string }[] = [];
  today = new Date();

  constructor(
    private userService:     UserService,
    private transferService: TransferService,
    private authService:     AuthService,
  ) {}

  ngOnInit(): void {
    forkJoin({
      agents:    this.userService.getByRole('ROLE_AGENT'),
      transfers: this.transferService.getAllAdmin(),
    }).subscribe({
      next: ({ agents, transfers }) => {
        const today = new Date().toDateString();
        this.stats = [
          { label: 'Total Agents',       value: agents.length,                                         icon: 'badge',       color: '#3949ab' },
          { label: 'Active Agents',      value: agents.filter((a: any) => a.active).length,            icon: 'check_circle', color: '#2e7d32' },
          { label: 'Transfers Today',    value: transfers.filter(t => new Date(t.createdAt).toDateString() === today).length, icon: 'swap_horiz', color: '#0277bd' },
          { label: 'Pending Transfers',  value: transfers.filter(t => t.status === 'EN_ATTENTE').length, icon: 'pending',    color: '#e65100' },
        ];
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });
  }
}
