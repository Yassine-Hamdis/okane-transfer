import { Component, OnInit }        from '@angular/core';
import { CommonModule }             from '@angular/common';
import { RouterModule }             from '@angular/router';
import { MatCardModule }            from '@angular/material/card';
import { MatIconModule }            from '@angular/material/icon';
import { MatButtonModule }          from '@angular/material/button';
import { MatProgressBarModule }     from '@angular/material/progress-bar';
import { NgChartsModule }           from 'ng2-charts';
// import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { forkJoin }                 from 'rxjs';

import { TransferService }          from '../../../core/services/transfer.service';
import { AgencyService }            from '../../../core/services/agency.service';
import { KycService }               from '../../../core/services/kyc.service';
import { UserService }              from '../../../core/services/user.service';
import { LoadingSpinnerComponent }  from '../../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent }     from '../../../shared/components/status-badge/status-badge.component';
//import { CurrencyFormatPipe }       from '../../../shared/pipes/currency-format.pipe';
import { Transfer }                 from '../../../core/models/transfer.model';

interface StatCard {
  label: string;
  value: number | string;
  icon:  string;
  color: string;
  route?: string;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatProgressBarModule,
    NgChartsModule,
    // BaseChartDirective,
    LoadingSpinnerComponent,
    StatusBadgeComponent,
    // CurrencyFormatPipe,
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrls:   ['./admin-dashboard.component.scss'],
})
export class AdminDashboardComponent implements OnInit {

  loading = true;
  stats: StatCard[] = [];

  // ── today getter ─────────────────────────────────────────────────────────────
  get today(): Date { return new Date(); }

  // ── Bar chart ─────────────────────────────────────────────────────────────────
  barChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [{
      data:            [],
      label:           'Transfers',
      backgroundColor: 'rgba(57, 73, 171, 0.7)',
      borderColor:     '#3949ab',
      borderWidth:     1,
      borderRadius:    6,
    }],
  };

  barChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: { mode: 'index' },
    },
    scales: {
      y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } },
      x: { grid: { display: false } },
    },
  };

  // ── Pie chart ─────────────────────────────────────────────────────────────────
  pieChartData: ChartData<'pie'> = {
    labels:   ['Pending', 'Paid', 'Cancelled', 'Expired'],
    datasets: [{
      data:            [0, 0, 0, 0],
      backgroundColor: ['#0277bd', '#2e7d32', '#c62828', '#9e9e9e'],
      hoverOffset:     6,
    }],
  };

  pieChartOptions: ChartConfiguration<'pie'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom', labels: { padding: 16, usePointStyle: true } },
    },
  };

  // ── Line chart ────────────────────────────────────────────────────────────────
  lineChartData: ChartData<'line'> = {
    labels: [],
    datasets: [{
      data:            [],
      label:           'Revenue (fees)',
      borderColor:     '#ffb300',
      backgroundColor: 'rgba(255,179,0,0.1)',
      fill:            true,
      tension:         0.4,
      pointRadius:     4,
      pointBackgroundColor: '#ffb300',
    }],
  };

  lineChartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } },
      x: { grid: { display: false } },
    },
  };

  recentTransfers: Transfer[] = [];

  constructor(
    private transferService: TransferService,
    private agencyService:   AgencyService,
    private kycService:      KycService,
    private userService:     UserService,
  ) {}

  ngOnInit(): void {
    forkJoin({
      transfers: this.transferService.getAllAdmin(),
      agencies:  this.agencyService.getActive(),
      flagged:   this.kycService.getFlagged(),
      blocked:   this.kycService.getBlocked(),
      agents:    this.userService.getByRole('ROLE_AGENT'),
    }).subscribe({
      next: ({ transfers, agencies, flagged, blocked, agents }) => {
        this.buildStats(transfers, agencies, flagged, blocked, agents);
        this.buildCharts(transfers);
        this.recentTransfers = transfers.slice(0, 5);
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });
  }

  private buildStats(
    transfers: Transfer[],
    agencies:  any[],
    flagged:   any[],
    blocked:   any[],
    agents:    any[],
  ): void {
    const todayStr    = new Date().toDateString();
    const todayCount  = transfers.filter(
      t => new Date(t.createdAt).toDateString() === todayStr
    ).length;

    this.stats = [
      { label: 'Transfers Today',  value: todayCount,                                        icon: 'swap_horiz',    color: '#3949ab', route: '/admin/transfers' },
      { label: 'Pending',          value: transfers.filter(t => t.status === 'EN_ATTENTE').length, icon: 'pending_actions', color: '#0277bd', route: '/admin/transfers' },
      { label: 'KYC Flagged',      value: flagged.length,                                    icon: 'flag',          color: '#e65100', route: '/admin/kyc' },
      { label: 'KYC Blocked',      value: blocked.length,                                    icon: 'block',         color: '#c62828', route: '/admin/kyc' },
      { label: 'Active Agencies',  value: agencies.length,                                   icon: 'store',         color: '#2e7d32', route: '/admin/agencies' },
      { label: 'Active Agents',    value: agents.filter((a: any) => a.active).length,        icon: 'badge',         color: '#6a1b9a', route: '/admin/users' },
    ];
  }

  private buildCharts(transfers: Transfer[]): void {
    const days = Array.from({ length: 7 }, (_, i) => {
      const d = new Date();
      d.setDate(d.getDate() - (6 - i));
      return d;
    });

    const labels = days.map(d =>
      d.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })
    );

    const countPerDay   = days.map(d =>
      transfers.filter(t => new Date(t.createdAt).toDateString() === d.toDateString()).length
    );
    const revenuePerDay = days.map(d =>
      transfers
        .filter(t => new Date(t.createdAt).toDateString() === d.toDateString())
        .reduce((s, t) => s + t.feeAmount, 0)
    );

    this.barChartData  = { ...this.barChartData,  labels, datasets: [{ ...this.barChartData.datasets[0],  data: countPerDay }] };
    this.lineChartData = { ...this.lineChartData, labels, datasets: [{ ...this.lineChartData.datasets[0], data: revenuePerDay }] };
    this.pieChartData  = {
      ...this.pieChartData,
      datasets: [{
        ...this.pieChartData.datasets[0],
        data: [
          transfers.filter(t => t.status === 'EN_ATTENTE').length,
          transfers.filter(t => t.status === 'PAYE').length,
          transfers.filter(t => t.status === 'ANNULE').length,
          transfers.filter(t => t.status === 'EXPIRE').length,
        ],
      }],
    };
  }
}
