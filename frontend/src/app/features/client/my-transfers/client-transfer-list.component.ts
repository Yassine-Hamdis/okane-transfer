import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule }                 from '@angular/common';
import { RouterModule }                 from '@angular/router';
import { FormsModule }                  from '@angular/forms';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator }   from '@angular/material/paginator';
import { MatCardModule }     from '@angular/material/card';
import { MatButtonModule }   from '@angular/material/button';
import { MatIconModule }     from '@angular/material/icon';
import { MatInputModule }    from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTooltipModule }  from '@angular/material/tooltip';

import { ClientService }           from '../../../core/services/client.service';
import { TransferSummary }         from '../../../core/models/transfer.model';
import { StatusBadgeComponent }    from '../../../shared/components/status-badge/status-badge.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { CurrencyFormatPipe }      from '../../../shared/pipes/currency-format.pipe';

@Component({
  selector: 'app-client-transfer-list',
  standalone: true,
  imports: [
    CommonModule, RouterModule, FormsModule,
    MatTableModule, MatPaginatorModule,
    MatCardModule, MatButtonModule,
    MatIconModule, MatInputModule, MatFormFieldModule,
    MatTooltipModule,
    StatusBadgeComponent, LoadingSpinnerComponent, CurrencyFormatPipe,
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>My Transfers</h1>
        <a mat-stroked-button routerLink="/client/track">
          <mat-icon>track_changes</mat-icon> Track a Transfer
        </a>
      </div>

      <mat-card>
        <div class="table-filters">
          <mat-form-field appearance="outline" class="search-field">
            <mat-label>Search transfers</mat-label>
            <input matInput (input)="applyFilter($any($event.target).value)" />
            <mat-icon matPrefix>search</mat-icon>
          </mat-form-field>
        </div>

        <app-loading-spinner *ngIf="loading" [inline]="true" />

        <div class="table-wrapper" *ngIf="!loading">
          <table mat-table [dataSource]="dataSource">

            <ng-container matColumnDef="code">
              <th mat-header-cell *matHeaderCellDef>Code</th>
              <td mat-cell *matCellDef="let t">
                <code class="code-chip">{{ t.withdrawalCode }}</code>
              </td>
            </ng-container>

            <ng-container matColumnDef="recipient">
              <th mat-header-cell *matHeaderCellDef>Recipient</th>
              <td mat-cell *matCellDef="let t">{{ t.recipientFullName }}</td>
            </ng-container>

            <ng-container matColumnDef="type">
              <th mat-header-cell *matHeaderCellDef>Type</th>
              <td mat-cell *matCellDef="let t">
                <span class="type-chip type-{{ t.transferType.toLowerCase() }}">
                  {{ t.transferType }}
                </span>
              </td>
            </ng-container>

            <ng-container matColumnDef="sent">
              <th mat-header-cell *matHeaderCellDef>Sent</th>
              <td mat-cell *matCellDef="let t">
                {{ t.sentAmount | currencyFormat:t.sentCurrency }}
              </td>
            </ng-container>

            <ng-container matColumnDef="received">
              <th mat-header-cell *matHeaderCellDef>Received</th>
              <td mat-cell *matCellDef="let t">
                {{ t.receivedAmount | currencyFormat:t.receivedCurrency }}
              </td>
            </ng-container>

            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let t">
                <app-status-badge [status]="t.status" />
              </td>
            </ng-container>

            <ng-container matColumnDef="date">
              <th mat-header-cell *matHeaderCellDef>Date</th>
              <td mat-cell *matCellDef="let t">{{ t.createdAt | date:'mediumDate' }}</td>
            </ng-container>

            <ng-container matColumnDef="track">
              <th mat-header-cell *matHeaderCellDef></th>
              <td mat-cell *matCellDef="let t">
                <a mat-icon-button
                   [routerLink]="['/track', t.withdrawalCode]"
                   matTooltip="Track this transfer">
                  <mat-icon>track_changes</mat-icon>
                </a>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns; sticky: true"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>

            <tr class="mat-row" *matNoDataRow>
              <td class="mat-cell"
                  [attr.colspan]="displayedColumns.length"
                  style="text-align:center;padding:48px;color:var(--okane-text-light)">
                No transfers found
              </td>
            </tr>
          </table>
        </div>

        <mat-paginator [pageSizeOptions]="[10, 25]" showFirstLastButtons />
      </mat-card>
    </div>
  `,
  styles: [`
    .table-filters { padding: 16px 16px 0; }
    .search-field  { width: 320px; }
    .table-wrapper { overflow-x: auto; }
    .code-chip {
      background: #e8eaf6; color: var(--okane-primary);
      padding: 2px 8px; border-radius: 4px; font-size: 0.8rem; font-weight: 700;
    }
    .type-chip {
      padding: 3px 10px; border-radius: 12px;
      font-size: 0.72rem; font-weight: 700; text-transform: uppercase;
    }
    .type-standard     { background: #e8eaf6; color: #3949ab; }
    .type-express      { background: #fff3e0; color: #e65100; }
    .type-mobile_money { background: #e8f5e9; color: #2e7d32; }
  `]
})
export class ClientTransferListComponent implements OnInit {

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  displayedColumns = ['code', 'recipient', 'type', 'sent', 'received', 'status', 'date', 'track'];
  dataSource       = new MatTableDataSource<TransferSummary>();
  loading          = true;

  constructor(private clientService: ClientService) {}

  ngOnInit(): void {
    this.clientService.getMyTransfers().subscribe({
      next: transfers => {
        this.dataSource.data      = transfers;
        this.dataSource.paginator = this.paginator;
        this.loading              = false;
      },
      error: () => { this.loading = false; },
    });
  }

  applyFilter(value: string): void {
    this.dataSource.filter = value.trim().toLowerCase();
  }
}
