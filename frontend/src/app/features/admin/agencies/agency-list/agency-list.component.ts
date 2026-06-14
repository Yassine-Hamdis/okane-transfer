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
import { MatSnackBar, MatSnackBarModule }  from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule }      from '@angular/material/dialog';
import { MatTooltipModule }  from '@angular/material/tooltip';

import { AgencyService }           from '../../../../core/services/agency.service';
import { Agency }                  from '../../../../core/models/agency.model';
import { StatusBadgeComponent }    from '../../../../shared/components/status-badge/status-badge.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent }  from '../../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-agency-list',
  standalone: true,
  imports: [
    CommonModule, RouterModule, FormsModule,
    MatTableModule, MatPaginatorModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatInputModule, MatFormFieldModule,
    MatSnackBarModule, MatDialogModule, MatTooltipModule,
    StatusBadgeComponent, LoadingSpinnerComponent,
  ],
  templateUrl: './agency-list.component.html',
  styleUrls:   ['./agency-list.component.scss'],
})
export class AgencyListComponent implements OnInit {

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  displayedColumns = ['name', 'city', 'country', 'manager', 'dailyLimit', 'status', 'actions'];
  dataSource       = new MatTableDataSource<Agency>();
  loading          = true;

  constructor(
    private agencyService: AgencyService,
    private snackBar:      MatSnackBar,
    private dialog:        MatDialog,
  ) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.agencyService.getAll().subscribe({
      next: list => {
        this.dataSource.data      = list;
        this.dataSource.paginator = this.paginator;
        this.loading              = false;
      },
      error: () => { this.loading = false; },
    });
  }

  applyFilter(v: string): void {
    this.dataSource.filter = v.trim().toLowerCase();
  }

  suspend(a: Agency): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Suspend Agency', message: `Suspend ${a.name}?`, confirmText: 'Suspend', danger: true },
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.agencyService.suspend(a.id).subscribe({
        next: msg => { this.snackBar.open(msg || 'Suspended', 'OK', { duration: 3000 }); this.load(); },
        error: err => this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 }),
      });
    });
  }

  activate(a: Agency): void {
    this.agencyService.activate(a.id).subscribe({
      next: msg => { this.snackBar.open(msg || 'Activated', 'OK', { duration: 3000 }); this.load(); },
      error: err => this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 }),
    });
  }
}
