import { Component, OnInit }       from '@angular/core';
import { CommonModule }            from '@angular/common';
import { RouterModule }            from '@angular/router';
import { MatCardModule }           from '@angular/material/card';
import { MatButtonModule }         from '@angular/material/button';
import { MatIconModule }           from '@angular/material/icon';
import { MatDividerModule }        from '@angular/material/divider';
import { forkJoin }                from 'rxjs';

import { TransferService }         from '../../../core/services/transfer.service';
import { CashService }             from '../../../core/services/cash.service';
import { AuthService }             from '../../../core/services/auth.service';
import { Transfer }                from '../../../core/models/transfer.model';
import { CashRegisterResponse }    from '../../../core/models/cash.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent }    from '../../../shared/components/status-badge/status-badge.component';
import { CurrencyFormatPipe }      from '../../../shared/pipes/currency-format.pipe';

interface QuickAction {
  label: string;
  icon:  string;
  route: string;
  color: string;
  description: string;
}

@Component({
  selector: 'app-agent-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    LoadingSpinnerComponent,
    StatusBadgeComponent,
    CurrencyFormatPipe,
  ],
  templateUrl: './agent-dashboard.component.html',
  styleUrls:   ['./agent-dashboard.component.scss'],
})
export class AgentDashboardComponent implements OnInit {

  loading       = true;
  agentName     = '';
  recentTransfers: Transfer[]           = [];
  cashRegister: CashRegisterResponse | null = null;

  quickActions: QuickAction[] = [
    {
      label:       'Send Transfer',
      icon:        'send',
      route:       '/agent/send',
      color:       '#3949ab',
      description: 'Create a new money transfer',
    },
    {
      label:       'Payout',
      icon:        'payments',
      route:       '/agent/payout',
      color:       '#2e7d32',
      description: 'Pay out a received transfer',
    },
    {
      label:       'My Transfers',
      icon:        'receipt_long',
      route:       '/agent/my-transfers',
      color:       '#0277bd',
      description: 'View all my transfers',
    },
    {
      label:       'Cash Register',
      icon:        'point_of_sale',
      route:       '/agent/cash',
      color:       '#e65100',
      description: 'View cash balance & operations',
    },
  ];

  todayStats = {
    sent:      0,
    paid:      0,
    cancelled: 0,
    totalSent: 0,
  };

  constructor(
    private transferService: TransferService,
    private cashService:     CashService,
    private authService:     AuthService,
  ) {}

  ngOnInit(): void {
    this.agentName = this.authService.getFullName() ?? 'Agent';

    forkJoin({
      transfers: this.transferService.getMyTransfers(),
      cash:      this.cashService.getMyRegister(),
    }).subscribe({
      next: ({ transfers, cash }) => {
        this.cashRegister = cash;
        this.buildStats(transfers);
        this.recentTransfers = transfers.slice(0, 5);
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });
  }

  private buildStats(transfers: Transfer[]): void {
    const today = new Date().toDateString();
    const todayT = transfers.filter(
      t => new Date(t.createdAt).toDateString() === today
    );
    this.todayStats = {
      sent:      todayT.filter(t => t.status === 'EN_ATTENTE').length,
      paid:      todayT.filter(t => t.status === 'PAYE').length,
      cancelled: todayT.filter(t => t.status === 'ANNULE').length,
      totalSent: todayT.reduce((s, t) => s + t.sentAmount, 0),
    };
  }

  get greeting(): string {
    const h = new Date().getHours();
    if (h < 12) return 'morning';
    if (h < 18) return 'afternoon';
    return 'evening';
  }

  get today(): Date { return new Date(); }
}
