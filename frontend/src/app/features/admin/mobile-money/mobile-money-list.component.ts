import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule }                 from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator }   from '@angular/material/paginator';
import { MatCardModule }        from '@angular/material/card';
import { MatButtonModule }      from '@angular/material/button';
import { MatIconModule }        from '@angular/material/icon';
import { MatInputModule }       from '@angular/material/input';
import { MatFormFieldModule }   from '@angular/material/form-field';
import { MatTabsModule }        from '@angular/material/tabs';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule }    from '@angular/material/dialog';
import { MatTooltipModule }     from '@angular/material/tooltip';
import { forkJoin }             from 'rxjs';

import { MobileMoneyService }       from '../../../core/services/mobile-money.service';
import { MobileMoneyResponse }      from '../../../core/models/mobile-money.model';
import { LoadingSpinnerComponent }  from '../../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent }     from '../../../shared/components/status-badge/status-badge.component';
import { ConfirmDialogComponent }   from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-mobile-money-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatInputModule, MatFormFieldModule, MatTabsModule,
    MatSnackBarModule, MatDialogModule, MatTooltipModule,
    LoadingSpinnerComponent, StatusBadgeComponent,
  ],
  templateUrl: './mobile-money-list.component.html',
  styleUrls:   ['./mobile-money-list.component.scss'],
})
export class MobileMoneyListComponent implements OnInit {

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  displayedColumns = [
    'withdrawalCode', 'operator', 'walletPhone',
    'status', 'reference', 'sentAt', 'reconciledAt', 'actions',
  ];

  pending: MobileMoneyResponse[] = [];
  sent:    MobileMoneyResponse[] = [];
  loading  = true;

  constructor(
    private mmService: MobileMoneyService,
    private snackBar:  MatSnackBar,
    private dialog:    MatDialog,
  ) {}

  ngOnInit(): void {
    forkJoin({
      pending: this.mmService.getPending(),
      sent:    this.mmService.getSent(),
    }).subscribe({
      next: ({ pending, sent }) => {
        this.pending = pending;
        this.sent    = sent;
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });
  }

  reconcile(mm: MobileMoneyResponse): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title:       'Reconcile Payment',
        message:     `Mark mobile money payment for transfer ${mm.withdrawalCode} as reconciled?`,
        confirmText: 'Reconcile',
      },
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.mmService.reconcile(mm.id).subscribe({
        next: msg => {
          this.snackBar.open(msg || 'Reconciled', 'OK', { duration: 3000 });
          this.reload();
        },
        error: err =>
          this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 }),
      });
    });
  }

  private reload(): void {
    forkJoin({
      pending: this.mmService.getPending(),
      sent:    this.mmService.getSent(),
    }).subscribe(({ pending, sent }) => {
      this.pending = pending;
      this.sent    = sent;
    });
  }

  getOperatorColor(op: string): string {
    const map: Record<string, string> = {
      ORANGE_MONEY: '#ff6600',
      WAVE:         '#1a73e8',
      M_PESA:       '#4caf50',
    };
    return map[op] ?? '#757575';
  }
}
