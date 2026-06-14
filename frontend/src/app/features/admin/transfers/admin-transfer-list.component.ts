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
import { MatSelectModule }   from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule }    from '@angular/material/dialog';
import { MatTooltipModule }  from '@angular/material/tooltip';
import { MatTabsModule }     from '@angular/material/tabs';

import { TransferService }          from '../../../core/services/transfer.service';
import { Transfer, TransferStatus } from '../../../core/models/transfer.model';
import { StatusBadgeComponent }     from '../../../shared/components/status-badge/status-badge.component';
import { LoadingSpinnerComponent }  from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent }   from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { CurrencyFormatPipe }       from '../../../shared/pipes/currency-format.pipe';

@Component({
  selector: 'app-admin-transfer-list',
  standalone: true,
  imports: [
    CommonModule, RouterModule, FormsModule,
    MatTableModule, MatPaginatorModule, MatCardModule,
    MatButtonModule, MatIconModule, MatInputModule,
    MatFormFieldModule, MatSelectModule,
    MatSnackBarModule, MatDialogModule, MatTooltipModule,
    MatTabsModule,
    StatusBadgeComponent, LoadingSpinnerComponent, CurrencyFormatPipe,
  ],
  templateUrl: './admin-transfer-list.component.html',
  styleUrls:   ['./admin-transfer-list.component.scss'],
})
export class AdminTransferListComponent implements OnInit {

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  displayedColumns = [
    'code', 'sender', 'recipient', 'amount',
    'type', 'status', 'agency', 'date', 'actions',
  ];

  dataSource   = new MatTableDataSource<Transfer>();
  loading      = true;
  searchValue  = '';
  activeTab    = 0;

  readonly tabs: { label: string; status?: TransferStatus }[] = [
    { label: 'All' },
    { label: 'Pending',   status: 'EN_ATTENTE' },
    { label: 'Paid',      status: 'PAYE' },
    { label: 'Cancelled', status: 'ANNULE' },
    { label: 'Expired',   status: 'EXPIRE' },
  ];

  constructor(
    private transferService: TransferService,
    private snackBar:        MatSnackBar,
    private dialog:          MatDialog,
  ) {}

  ngOnInit(): void {
    this.loadTransfers();
  }

  loadTransfers(): void {
    this.loading = true;
    const tab    = this.tabs[this.activeTab];
    const obs    = tab.status
      ? this.transferService.getByStatus(tab.status)
      : this.transferService.getAllAdmin();

    obs.subscribe({
      next: transfers => {
        this.dataSource.data      = transfers;
        this.dataSource.paginator = this.paginator;
        this.dataSource.filter    = this.searchValue.toLowerCase();
        this.loading              = false;
      },
      error: () => { this.loading = false; },
    });
  }

  onTabChange(index: number): void {
    this.activeTab  = index;
    this.searchValue = '';
    this.loadTransfers();
  }

  applySearch(value: string): void {
    this.searchValue       = value;
    this.dataSource.filter = value.trim().toLowerCase();
  }

  approve(transfer: Transfer): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title:   'Approve Transfer',
        message: `Approve transfer ${transfer.withdrawalCode}?`,
        confirmText: 'Approve',
      },
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.transferService.approve(transfer.id).subscribe({
        next: msg => {
          this.snackBar.open(msg || 'Transfer approved', 'OK', { duration: 3000 });
          this.loadTransfers();
        },
        error: err => this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 }),
      });
    });
  }
}
