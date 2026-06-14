import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule }                 from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator }   from '@angular/material/paginator';
import { MatCardModule }     from '@angular/material/card';
import { MatIconModule }     from '@angular/material/icon';
import { MatInputModule }    from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule }   from '@angular/material/button';

import { UserService }             from '../../../core/services/user.service';
import { User }                    from '../../../core/models/user.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent }    from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-agent-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule,
    MatCardModule, MatIconModule,
    MatInputModule, MatFormFieldModule, MatButtonModule,
    LoadingSpinnerComponent, StatusBadgeComponent,
  ],
  template: `
    <div class="page-container">
      <div class="page-header"><h1>Agents</h1></div>

      <mat-card>
        <div style="padding:16px 16px 0">
          <mat-form-field appearance="outline" style="width:320px">
            <mat-label>Search agents</mat-label>
            <input matInput (input)="applyFilter($any($event.target).value)" />
            <mat-icon matPrefix>search</mat-icon>
          </mat-form-field>
        </div>

        <app-loading-spinner *ngIf="loading" [inline]="true" />

        <div style="overflow-x:auto" *ngIf="!loading">
          <table mat-table [dataSource]="dataSource">
            <ng-container matColumnDef="name">
              <th mat-header-cell *matHeaderCellDef>Name</th>
              <td mat-cell *matCellDef="let u">{{ u.firstName }} {{ u.lastName }}</td>
            </ng-container>
            <ng-container matColumnDef="email">
              <th mat-header-cell *matHeaderCellDef>Email</th>
              <td mat-cell *matCellDef="let u">{{ u.email }}</td>
            </ng-container>
            <ng-container matColumnDef="phone">
              <th mat-header-cell *matHeaderCellDef>Phone</th>
              <td mat-cell *matCellDef="let u">{{ u.phone }}</td>
            </ng-container>
            <ng-container matColumnDef="agency">
              <th mat-header-cell *matHeaderCellDef>Agency</th>
              <td mat-cell *matCellDef="let u">{{ u.agencyName ?? '—' }}</td>
            </ng-container>
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let u">
                <app-status-badge [status]="u.active ? 'ACTIVE' : 'INACTIVE'" />
              </td>
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
export class AgentListComponent implements OnInit {

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  cols       = ['name', 'email', 'phone', 'agency', 'status'];
  dataSource = new MatTableDataSource<User>();
  loading    = true;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.userService.getByRole('ROLE_AGENT').subscribe({
      next: agents => {
        this.dataSource.data      = agents;
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
