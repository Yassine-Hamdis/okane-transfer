import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule }                 from '@angular/common';
import { RouterModule }                 from '@angular/router';
import { FormsModule }                  from '@angular/forms';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { MatSortModule, MatSort }       from '@angular/material/sort';
import { MatCardModule }                from '@angular/material/card';
import { MatButtonModule }              from '@angular/material/button';
import { MatIconModule }                from '@angular/material/icon';
import { MatInputModule }               from '@angular/material/input';
import { MatFormFieldModule }           from '@angular/material/form-field';
import { MatSelectModule }              from '@angular/material/select';
import { MatMenuModule }                from '@angular/material/menu';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule }   from '@angular/material/dialog';
import { MatTooltipModule }             from '@angular/material/tooltip';
import { MatChipsModule }               from '@angular/material/chips';

import { UserService }                  from '../../../../core/services/user.service';
import { User, UserRole }               from '../../../../core/models/user.model';
import { StatusBadgeComponent }         from '../../../../shared/components/status-badge/status-badge.component';
import { LoadingSpinnerComponent }      from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent }       from '../../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    CommonModule, RouterModule, FormsModule,
    MatTableModule, MatPaginatorModule, MatSortModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatInputModule, MatFormFieldModule, MatSelectModule,
    MatMenuModule, MatSnackBarModule, MatDialogModule,
    MatTooltipModule, MatChipsModule,
    StatusBadgeComponent, LoadingSpinnerComponent,
  ],
  templateUrl: './user-list.component.html',
  styleUrls:   ['./user-list.component.scss'],
})
export class UserListComponent implements OnInit {

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort)      sort!:      MatSort;

  displayedColumns = ['name', 'email', 'phone', 'role', 'agency', 'status', 'createdAt', 'actions'];
  dataSource       = new MatTableDataSource<User>();
  loading          = true;
  searchValue      = '';
  roleFilter: UserRole | '' = '';

  readonly roleOptions: { value: UserRole | ''; label: string }[] = [
    { value: '',             label: 'All Roles' },
    { value: 'ROLE_ADMIN',   label: 'Admin' },
    { value: 'ROLE_MANAGER', label: 'Manager' },
    { value: 'ROLE_AGENT',   label: 'Agent' },
    { value: 'ROLE_CLIENT',  label: 'Client' },
  ];

  constructor(
    private userService: UserService,
    private snackBar:    MatSnackBar,
    private dialog:      MatDialog,
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.userService.getAll().subscribe({
      next: users => {
        this.dataSource.data      = users;
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort      = this.sort;
        this.dataSource.filterPredicate = this.buildFilter();
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });
  }

  applySearch(value: string): void {
    this.searchValue = value;
    this.applyFilters();
  }

  applyRoleFilter(): void {
    this.applyFilters();
  }

  private applyFilters(): void {
    // Combine search + role into a JSON string the filterPredicate can parse
    this.dataSource.filter = JSON.stringify({
      search: this.searchValue.toLowerCase(),
      role:   this.roleFilter,
    });
  }

  private buildFilter() {
    return (user: User, filter: string): boolean => {
      const f = JSON.parse(filter);
      const matchSearch = !f.search
        || user.firstName.toLowerCase().includes(f.search)
        || user.lastName.toLowerCase().includes(f.search)
        || user.email.toLowerCase().includes(f.search)
        || user.phone.includes(f.search);
      const matchRole = !f.role || user.role === f.role;
      return matchSearch && matchRole;
    };
  }

  suspend(user: User): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title:       'Suspend User',
        message:     `Suspend ${user.firstName} ${user.lastName}? They will lose access immediately.`,
        confirmText: 'Suspend',
        danger:      true,
      },
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.userService.suspend(user.id).subscribe({
        next: msg => {
          this.snackBar.open(msg || 'User suspended', 'OK', { duration: 3000 });
          this.loadUsers();
        },
        error: err => this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 }),
      });
    });
  }

  activate(user: User): void {
    this.userService.activate(user.id).subscribe({
      next: msg => {
        this.snackBar.open(msg || 'User activated', 'OK', { duration: 3000 });
        this.loadUsers();
      },
      error: err => this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 }),
    });
  }

  resetPassword(user: User): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title:       'Reset Password',
        message:     `Reset password for ${user.firstName} ${user.lastName}? They will be prompted to set a new one.`,
        confirmText: 'Reset',
        danger:      true,
      },
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      const newPassword = 'Temp@1234'; // default temp password
      this.userService.resetPassword(user.id, { newPassword }).subscribe({
        next: msg => this.snackBar.open(msg || 'Password reset', 'OK', { duration: 3000 }),
        error: err => this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 }),
      });
    });
  }

  getRoleLabel(role: UserRole): string {
    const map: Record<string, string> = {
      ROLE_ADMIN: 'Admin', ROLE_MANAGER: 'Manager',
      ROLE_AGENT: 'Agent', ROLE_CLIENT: 'Client',
    };
    return map[role] ?? role;
  }

  getRoleColor(role: UserRole): string {
    const map: Record<string, string> = {
      ROLE_ADMIN: '#c62828', ROLE_MANAGER: '#0277bd',
      ROLE_AGENT: '#2e7d32', ROLE_CLIENT: '#6a1b9a',
    };
    return map[role] ?? '#757575';
  }
}
