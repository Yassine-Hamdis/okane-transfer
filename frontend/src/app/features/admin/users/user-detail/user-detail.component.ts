import { Component, OnInit }       from '@angular/core';
import { CommonModule }            from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { MatCardModule }           from '@angular/material/card';
import { MatButtonModule }         from '@angular/material/button';
import { MatIconModule }           from '@angular/material/icon';
import { MatDividerModule }        from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule }    from '@angular/material/dialog';

import { UserService }             from '../../../../core/services/user.service';
import { User }                    from '../../../../core/models/user.model';
import { StatusBadgeComponent }    from '../../../../shared/components/status-badge/status-badge.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent }  from '../../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [
    CommonModule, RouterModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatDividerModule, MatSnackBarModule, MatDialogModule,
    StatusBadgeComponent, LoadingSpinnerComponent,
  ],
  templateUrl: './user-detail.component.html',
  styleUrls:   ['./user-detail.component.scss'],
})
export class UserDetailComponent implements OnInit {

  user:    User | null = null;
  loading  = true;

  constructor(
    private userService: UserService,
    private route:       ActivatedRoute,
    private router:      Router,
    private snackBar:    MatSnackBar,
    private dialog:      MatDialog,
  ) {}

  ngOnInit(): void {
    const id = parseInt(this.route.snapshot.paramMap.get('id')!, 10);
    this.userService.getById(id).subscribe({
      next: user => { this.user = user; this.loading = false; },
      error: () => { this.loading = false; this.router.navigate(['/admin/users']); },
    });
  }

  suspend(): void {
    if (!this.user) return;
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Suspend User', message: `Suspend ${this.user.firstName} ${this.user.lastName}?`,
        confirmText: 'Suspend', danger: true,
      },
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed || !this.user) return;
      this.userService.suspend(this.user.id).subscribe({
        next: msg => {
          this.snackBar.open(msg || 'Suspended', 'OK', { duration: 3000 });
          this.reload();
        },
        error: err => this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 }),
      });
    });
  }

  activate(): void {
    if (!this.user) return;
    this.userService.activate(this.user.id).subscribe({
      next: msg => {
        this.snackBar.open(msg || 'Activated', 'OK', { duration: 3000 });
        this.reload();
      },
      error: err => this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 }),
    });
  }

  resetPassword(): void {
    if (!this.user) return;
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Reset Password', message: `Reset password for ${this.user.email}?`,
        confirmText: 'Reset', danger: true,
      },
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed || !this.user) return;
      this.userService.resetPassword(this.user.id, { newPassword: 'Temp@1234' }).subscribe({
        next: msg => this.snackBar.open(msg || 'Password reset to Temp@1234', 'OK', { duration: 5000 }),
        error: err => this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 }),
      });
    });
  }

  private reload(): void {
    if (!this.user) return;
    this.userService.getById(this.user.id).subscribe(u => this.user = u);
  }

  getInitials(): string {
    if (!this.user) return '';
    return `${this.user.firstName[0]}${this.user.lastName[0]}`.toUpperCase();
  }

  getRoleLabel(): string {
    const map: Record<string, string> = {
      ROLE_ADMIN: 'Admin', ROLE_MANAGER: 'Manager',
      ROLE_AGENT: 'Agent', ROLE_CLIENT: 'Client',
    };
    return this.user ? (map[this.user.role] ?? this.user.role) : '';
  }
}
