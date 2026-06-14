import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule }                 from '@angular/common';
import { FormsModule }                  from '@angular/forms';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator }   from '@angular/material/paginator';
import { MatCardModule }     from '@angular/material/card';
import { MatIconModule }     from '@angular/material/icon';
import { MatInputModule }    from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule }   from '@angular/material/button';
import { MatTooltipModule }  from '@angular/material/tooltip';

import { AuditService }            from '../../../core/services/audit.service';
import { AuditLog }                from '../../../core/models/audit.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-audit-log-list',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatTableModule, MatPaginatorModule, MatCardModule,
    MatIconModule, MatInputModule, MatFormFieldModule,
    MatButtonModule, MatTooltipModule,
    LoadingSpinnerComponent,
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Audit Logs</h1>
      </div>

      <mat-card>
        <div class="table-filters">
          <mat-form-field appearance="outline" class="search-field">
            <mat-label>Search logs</mat-label>
            <input matInput (input)="applyFilter($any($event.target).value)" />
            <mat-icon matPrefix>search</mat-icon>
          </mat-form-field>
        </div>

        <app-loading-spinner *ngIf="loading" [inline]="true" />

        <div class="table-wrapper" *ngIf="!loading">
          <table mat-table [dataSource]="dataSource">

            <ng-container matColumnDef="action">
              <th mat-header-cell *matHeaderCellDef>Action</th>
              <td mat-cell *matCellDef="let l">
                <span class="action-chip">{{ l.action }}</span>
              </td>
            </ng-container>

            <ng-container matColumnDef="user">
              <th mat-header-cell *matHeaderCellDef>User</th>
              <td mat-cell *matCellDef="let l">{{ l.userEmail ?? '—' }}</td>
            </ng-container>

            <ng-container matColumnDef="entity">
              <th mat-header-cell *matHeaderCellDef>Entity</th>
              <td mat-cell *matCellDef="let l">
                {{ l.entityType ?? '—' }}
                <span *ngIf="l.entityId"> #{{ l.entityId }}</span>
              </td>
            </ng-container>

            <ng-container matColumnDef="details">
              <th mat-header-cell *matHeaderCellDef>Details</th>
              <td mat-cell *matCellDef="let l">
                <span class="details-text" [matTooltip]="l.details ?? ''">
                  {{ l.details | slice:0:60 }}{{ l.details && l.details.length > 60 ? '...' : '' }}
                </span>
              </td>
            </ng-container>

            <ng-container matColumnDef="ip">
              <th mat-header-cell *matHeaderCellDef>IP Address</th>
              <td mat-cell *matCellDef="let l">
                <code>{{ l.ipAddress ?? '—' }}</code>
              </td>
            </ng-container>

            <ng-container matColumnDef="date">
              <th mat-header-cell *matHeaderCellDef>Date</th>
              <td mat-cell *matCellDef="let l">{{ l.createdAt | date:'medium' }}</td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns; sticky: true"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
          </table>
        </div>
        <mat-paginator [pageSizeOptions]="[25, 50, 100]" showFirstLastButtons />
      </mat-card>
    </div>
  `,
  styles: [`
    .table-filters { padding: 16px 16px 0; }
    .search-field  { width: 320px; }
    .table-wrapper { overflow-x: auto; }
    .action-chip {
      background: #e8eaf6; color: var(--okane-primary);
      padding: 2px 8px; border-radius: 4px;
      font-size: 0.75rem; font-weight: 700;
    }
    .details-text { font-size: 0.85rem; color: var(--okane-text-light); }
    code { font-size: 0.8rem; }
  `]
})
export class AuditLogListComponent implements OnInit {

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  displayedColumns = ['action', 'user', 'entity', 'details', 'ip', 'date'];
  dataSource       = new MatTableDataSource<AuditLog>();
  loading          = true;

  constructor(private auditService: AuditService) {}

  ngOnInit(): void {
    this.auditService.getAll().subscribe({
      next: logs => {
        this.dataSource.data      = logs;
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
