import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule }                 from '@angular/common';
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

import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { TransferService }         from '../../../core/services/transfer.service';
import { Transfer }                from '../../../core/models/transfer.model';
import { StatusBadgeComponent }    from '../../../shared/components/status-badge/status-badge.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent }  from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { CurrencyFormatPipe }      from '../../../shared/pipes/currency-format.pipe';

@Component({
  selector: 'app-agent-transfer-list',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatTableModule, MatPaginatorModule,
    MatCardModule, MatButtonModule,
    MatIconModule, MatInputModule, MatFormFieldModule,
    MatSnackBarModule, MatDialogModule, MatTooltipModule,
    StatusBadgeComponent, LoadingSpinnerComponent, CurrencyFormatPipe,
    MatProgressSpinnerModule,
  ],
  templateUrl: './agent-transfer-list.component.html',
  styleUrls:   ['./agent-transfer-list.component.scss'],
})
export class AgentTransferListComponent implements OnInit {

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  displayedColumns = [
    'code', 'sender', 'recipient', 'amount', 'type', 'status', 'date', 'actions'
  ];
  dataSource   = new MatTableDataSource<Transfer>();
  loading      = true;
  phoneSearch  = '';
  searching    = false;

  constructor(
    private transferService: TransferService,
    private snackBar:        MatSnackBar,
    private dialog:          MatDialog,
  ) {}

  ngOnInit(): void {
    this.loadMyTransfers();
  }

  loadMyTransfers(): void {
    this.loading = true;
    this.transferService.getMyTransfers().subscribe({
      next: transfers => {
        this.dataSource.data      = transfers;
        this.dataSource.paginator = this.paginator;
        this.loading              = false;
      },
      error: () => { this.loading = false; },
    });
  }

  searchByPhone(): void {
    if (!this.phoneSearch.trim()) {
      this.loadMyTransfers();
      return;
    }
    this.searching = true;
    this.transferService.searchByPhone(this.phoneSearch.trim()).subscribe({
      next: transfers => {
        this.dataSource.data = transfers;
        this.searching       = false;
      },
      error: err => {
        this.searching = false;
        this.snackBar.open(err.error?.message ?? 'Search failed', 'OK', { duration: 3000 });
      },
    });
  }

  clearSearch(): void {
    this.phoneSearch = '';
    this.loadMyTransfers();
  }

  applyFilter(value: string): void {
    this.dataSource.filter = value.trim().toLowerCase();
  }

  cancel(transfer: Transfer): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title:   'Cancel Transfer',
        message: `Cancel transfer ${transfer.withdrawalCode}? This cannot be undone.`,
        confirmText: 'Cancel Transfer',
        danger:  true,
      },
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.transferService.cancel(transfer.id, { reason: 'Cancelled by agent' })
        .subscribe({
          next: msg => {
            this.snackBar.open(msg || 'Transfer cancelled', 'OK', { duration: 3000 });
            this.loadMyTransfers();
          },
          error: err =>
            this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 }),
        });
    });
  }
}
