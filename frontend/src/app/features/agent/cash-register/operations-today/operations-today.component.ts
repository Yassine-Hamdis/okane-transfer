import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule }                 from '@angular/common';
import { RouterModule }                 from '@angular/router';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator }   from '@angular/material/paginator';
import { MatCardModule }   from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule }   from '@angular/material/icon';

import { CashService }             from '../../../../core/services/cash.service';
import { CashOperationResponse }   from '../../../../core/models/cash.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { CurrencyFormatPipe }      from '../../../../shared/pipes/currency-format.pipe';

@Component({
  selector: 'app-operations-today',
  standalone: true,
  imports: [
    CommonModule, RouterModule,
    MatTableModule, MatPaginatorModule,
    MatCardModule, MatButtonModule, MatIconModule,
    LoadingSpinnerComponent, CurrencyFormatPipe,
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <div class="header-left">
          <a mat-icon-button routerLink="/agent/cash">
            <mat-icon>arrow_back</mat-icon>
          </a>
          <h1>Today's Operations</h1>
        </div>
      </div>

      <mat-card>
        <app-loading-spinner *ngIf="loading" [inline]="true" />

        <div class="table-wrapper" *ngIf="!loading">
          <table mat-table [dataSource]="dataSource">

            <ng-container matColumnDef="type">
              <th mat-header-cell *matHeaderCellDef>Type</th>
              <td mat-cell *matCellDef="let o">
                <span class="op-type" [class]="'op-' + o.type.toLowerCase()">
                  {{ o.type }}
                </span>
              </td>
            </ng-container>

            <ng-container matColumnDef="amount">
              <th mat-header-cell *matHeaderCellDef>Amount</th>
              <td mat-cell *matCellDef="let o">
                {{ o.amount | currencyFormat:o.currencyCode }}
              </td>
            </ng-container>

            <ng-container matColumnDef="balance">
              <th mat-header-cell *matHeaderCellDef>Balance After</th>
              <td mat-cell *matCellDef="let o">
                {{ o.balanceAfter | currencyFormat:o.currencyCode }}
              </td>
            </ng-container>

            <ng-container matColumnDef="code">
              <th mat-header-cell *matHeaderCellDef>Transfer Code</th>
              <td mat-cell *matCellDef="let o">
                <code *ngIf="o.withdrawalCode" class="code-chip">{{ o.withdrawalCode }}</code>
                <span *ngIf="!o.withdrawalCode" class="text-muted">—</span>
              </td>
            </ng-container>

            <ng-container matColumnDef="note">
              <th mat-header-cell *matHeaderCellDef>Note</th>
              <td mat-cell *matCellDef="let o">{{ o.note ?? '—' }}</td>
            </ng-container>

            <ng-container matColumnDef="time">
              <th mat-header-cell *matHeaderCellDef>Time</th>
              <td mat-cell *matCellDef="let o">{{ o.createdAt | date:'shortTime' }}</td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns; sticky: true"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>

            <tr class="mat-row" *matNoDataRow>
              <td class="mat-cell" [attr.colspan]="displayedColumns.length"
                  style="text-align:center;padding:40px;color:var(--okane-text-light)">
                No operations today
              </td>
            </tr>
          </table>
        </div>

        <mat-paginator [pageSizeOptions]="[10, 25]" showFirstLastButtons />
      </mat-card>
    </div>
  `,
  styles: [`
    .header-left { display: flex; align-items: center; gap: 8px; }
    .table-wrapper { overflow-x: auto; }
    .op-type {
      padding: 3px 10px; border-radius: 12px;
      font-size: 0.72rem; font-weight: 700; text-transform: uppercase;
    }
    .op-envoi        { background: #e8eaf6; color: #3949ab; }
    .op-retrait      { background: #e8f5e9; color: #2e7d32; }
    .op-annulation   { background: #ffebee; color: #c62828; }
    .op-cloture_caisse { background: #f5f5f5; color: #757575; }
    .code-chip {
      background: #e8eaf6; color: var(--okane-primary);
      padding: 2px 6px; border-radius: 4px; font-size: 0.78rem; font-weight: 700;
    }
  `]
})
export class OperationsTodayComponent implements OnInit {

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  displayedColumns = ['type', 'amount', 'balance', 'code', 'note', 'time'];
  dataSource       = new MatTableDataSource<CashOperationResponse>();
  loading          = true;

  constructor(private cashService: CashService) {}

  ngOnInit(): void {
    this.cashService.getTodayOperations().subscribe({
      next: ops => {
        this.dataSource.data      = ops;
        this.dataSource.paginator = this.paginator;
        this.loading              = false;
      },
      error: () => { this.loading = false; },
    });
  }
}
