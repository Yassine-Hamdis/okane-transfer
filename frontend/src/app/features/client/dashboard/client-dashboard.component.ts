import { Component, OnInit }       from '@angular/core';
import { CommonModule }            from '@angular/common';
import { RouterModule }            from '@angular/router';
import { MatCardModule }           from '@angular/material/card';
import { MatButtonModule }         from '@angular/material/button';
import { MatIconModule }           from '@angular/material/icon';
import { MatDividerModule }        from '@angular/material/divider';
import { forkJoin }                from 'rxjs';

import { ClientService }           from '../../../core/services/client.service';
import { NotificationService }     from '../../../core/services/notification.service';
import { AuthService }             from '../../../core/services/auth.service';
import { TransferSummary }         from '../../../core/models/transfer.model';
import { Notification }            from '../../../core/models/notification.model';
import { User }                    from '../../../core/models/user.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent }    from '../../../shared/components/status-badge/status-badge.component';
import { CurrencyFormatPipe }      from '../../../shared/pipes/currency-format.pipe';

@Component({
  selector: 'app-client-dashboard',
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
  templateUrl: './client-dashboard.component.html',
  styleUrls:   ['./client-dashboard.component.scss'],
})
export class ClientDashboardComponent implements OnInit {

  loading          = true;
  profile: User | null = null;
  recentTransfers: TransferSummary[] = [];
  unreadNotifs:    Notification[]    = [];

  stats = {
    total:     0,
    pending:   0,
    paid:      0,
    cancelled: 0,
  };

  constructor(
    private clientService:       ClientService,
    private notificationService: NotificationService,
    private authService:         AuthService,
  ) {}

  ngOnInit(): void {
    forkJoin({
      profile:    this.clientService.getProfile(),
      transfers:  this.clientService.getMyTransfers(),
      notifs:     this.notificationService.getUnread(),
    }).subscribe({
      next: ({ profile, transfers, notifs }) => {
        this.profile          = profile;
        this.recentTransfers  = transfers.slice(0, 5);
        this.unreadNotifs     = notifs.slice(0, 3);
        this.buildStats(transfers);
        this.loading          = false;
      },
      error: () => { this.loading = false; },
    });
  }

  private buildStats(transfers: TransferSummary[]): void {
    this.stats = {
      total:     transfers.length,
      pending:   transfers.filter(t => t.status === 'EN_ATTENTE').length,
      paid:      transfers.filter(t => t.status === 'PAYE').length,
      cancelled: transfers.filter(t => t.status === 'ANNULE').length,
    };
  }

  get greeting(): string {
    const h = new Date().getHours();
    if (h < 12) return 'Good morning';
    if (h < 18) return 'Good afternoon';
    return 'Good evening';
  }

  get today(): Date { return new Date(); }
}
