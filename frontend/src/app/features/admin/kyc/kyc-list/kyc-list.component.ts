import { Component, OnInit }    from '@angular/core';
import { CommonModule }         from '@angular/common';
import { RouterModule }         from '@angular/router';
import { MatCardModule }        from '@angular/material/card';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatButtonModule }      from '@angular/material/button';
import { MatIconModule }        from '@angular/material/icon';
import { MatTabsModule }        from '@angular/material/tabs';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule }     from '@angular/material/tooltip';
import { MatChipsModule }       from '@angular/material/chips';

import { KycService }               from '../../../../core/services/kyc.service';
import { KycRecord }                from '../../../../core/models/kyc.model';
import { LoadingSpinnerComponent }  from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent }     from '../../../../shared/components/status-badge/status-badge.component';
import { forkJoin }                 from 'rxjs';

@Component({
  selector: 'app-kyc-list',
  standalone: true,
  imports: [
    CommonModule, RouterModule,
    MatCardModule, MatTableModule, MatButtonModule,
    MatIconModule, MatTabsModule, MatProgressBarModule,
    MatTooltipModule, MatChipsModule,
    LoadingSpinnerComponent, StatusBadgeComponent,
  ],
  templateUrl: './kyc-list.component.html',
  styleUrls:   ['./kyc-list.component.scss'],
})
export class KycListComponent implements OnInit {

  loading  = true;
  flagged: KycRecord[]  = [];
  blocked: KycRecord[]  = [];
  watchlist: KycRecord[] = [];

  displayedColumns = [
    'withdrawalCode', 'status', 'riskScore',
    'watchlistHit', 'suspicion', 'checkedAt', 'actions',
  ];

  constructor(private kycService: KycService) {}

  ngOnInit(): void {
    forkJoin({
      flagged:   this.kycService.getFlagged(),
      blocked:   this.kycService.getBlocked(),
      watchlist: this.kycService.getWatchlistHits(),
    }).subscribe({
      next: ({ flagged, blocked, watchlist }) => {
        this.flagged   = flagged;
        this.blocked   = blocked;
        this.watchlist = watchlist;
        this.loading   = false;
      },
      error: () => { this.loading = false; },
    });
  }

  getRiskColor(score: number): string {
    if (score >= 80) return '#c62828';
    if (score >= 50) return '#e65100';
    return '#2e7d32';
  }
}
