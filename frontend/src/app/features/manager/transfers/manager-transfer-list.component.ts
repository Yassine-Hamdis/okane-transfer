import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule }                 from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator }   from '@angular/material/paginator';
import { MatCardModule }  from '@angular/material/card';
import { MatIconModule }  from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';

import { TransferService }         from '../../../core/services/transfer.service';
import { Transfer }                from '../../../core/models/transfer.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent }    from '../../../shared/components/status-badge/status-badge.component';
import { CurrencyFormatPipe }      from '../../../shared/pipes/currency-format.pipe';

@Component({
  selector: 'app-manager-transfer-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule,
    MatCardModule, MatIconModule,
    MatInputModule, MatFormFieldModule,
    LoadingSpinnerComponent, StatusBadgeComponent, CurrencyFormatPipe,
  ],
  template: `
    <div class="page-container">
      <div class="page-header"><h1>Transfers</h1></div>
      <mat-card>
        <div style="padding:16px 16px 0">
          <mat-form-field appearance="outline" style="width:320px">
            <mat-label>Search</mat-label>
            <input matInput (input)="applyFilter($any($event.target).value)" />
            <mat-icon matPrefix>search</mat-icon>
          </mat-form-field>
        </div>

        <app-loading-spinner *ngIf="loading" [inline]="true" />

        <div style="overflow-x:auto" *ngIf="!loading">
          <table mat-table [dataSource]="dataSource">
            <ng-container matColumnDef="code">
              <th mat-header-cell *matHeaderCellDef>Code</th>
              <td mat-cell *matCellDef="let t">
                <code style="background:#e8eaf6;color:#3949ab;padding:2px 8px;border-radius:4px;font-size:0.8rem;font-weight:700">
                  {{ t.withdrawalCode }}
                </code>
              </td>
            </ng-container>
            <ng-container matColumnDef="sender">
              <th mat-header-cell *matHeaderCellDef>Sender</th>
              <td mat-cell *matCellDef="let t">{{ t.senderFullName }}</td>
            </ng-container>
            <ng-container matColumnDef="recipient">
              <th mat-header-cell *matHeaderCellDef>Recipient</th>
              <td mat-cell *matCellDef="let t">{{ t.recipientFullName }}</td>
            </ng-container>
            <ng-container matColumnDef="amount">
              <th mat-header-cell *matHeaderCellDef>Amount</th>
              <td mat-cell *matCellDef="let t">
                {{ t.sentAmount | currencyFormat:t.sentCurrency }}
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
              <td mat-cell *matCellDef="let t">{{ t.createdAt | date:'shortDate' }}</td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="cols"></tr>
            <tr mat-row *matRowDef="let row; columns: cols;"></tr>
          </table>
        </div>
        <mat-paginator [pageSizeOptions]="[10, 25]" showFirstLastButtons />
      </mat-card>
    </div>
  `,
})
export class ManagerTransferListComponent implements OnInit {

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  cols       = ['code', 'sender', 'recipient', 'amount', 'status', 'date'];
  dataSource = new MatTableDataSource<Transfer>();
  loading    = true;

  constructor(private transferService: TransferService) {}

  ngOnInit(): void {
    this.transferService.getAllAdmin().subscribe({
      next: transfers => {
        this.dataSource.data      = transfers;
        this.dataSource.paginator = this.paginator;
        this.loading              = false;
      },
      error: () => { this.loading = false; },
    });
  }

  applyFilter(v: string): void {
    this.dataSource.filter = v.trim().toLowerCase();
  }
}
