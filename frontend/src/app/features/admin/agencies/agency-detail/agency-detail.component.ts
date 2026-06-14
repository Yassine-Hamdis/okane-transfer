import { Component, OnInit }       from '@angular/core';
import { CommonModule }            from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule }           from '@angular/material/card';
import { MatButtonModule }         from '@angular/material/button';
import { MatIconModule }           from '@angular/material/icon';
import { MatFormFieldModule }      from '@angular/material/form-field';
import { MatSelectModule }         from '@angular/material/select';
import { MatDividerModule }        from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule }    from '@angular/material/dialog';
import { forkJoin }                from 'rxjs';

import { AgencyService }           from '../../../../core/services/agency.service';
import { UserService }             from '../../../../core/services/user.service';
import { Agency }                  from '../../../../core/models/agency.model';
import { User }                    from '../../../../core/models/user.model';
import { StatusBadgeComponent }    from '../../../../shared/components/status-badge/status-badge.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent }  from '../../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-agency-detail',
  standalone: true,
  imports: [
    CommonModule, RouterModule, ReactiveFormsModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatFormFieldModule, MatSelectModule, MatDividerModule,
    MatSnackBarModule, MatDialogModule,
    StatusBadgeComponent, LoadingSpinnerComponent,
  ],
  templateUrl: './agency-detail.component.html',
  styleUrls:   ['./agency-detail.component.scss'],
})
export class AgencyDetailComponent implements OnInit {

  agency:   Agency | null = null;
  managers: User[]        = [];
  loading   = true;
  saving    = false;
  managerForm!: FormGroup;

  constructor(
    private agencyService: AgencyService,
    private userService:   UserService,
    private route:         ActivatedRoute,
    private router:        Router,
    private fb:            FormBuilder,
    private snackBar:      MatSnackBar,
    private dialog:        MatDialog,
  ) {}

  ngOnInit(): void {
    this.managerForm = this.fb.group({ managerId: [null, Validators.required] });

    const id = parseInt(this.route.snapshot.paramMap.get('id')!, 10);
    forkJoin({
      agency:   this.agencyService.getById(id),
      managers: this.userService.getByRole('ROLE_MANAGER'),
    }).subscribe({
      next: ({ agency, managers }) => {
        this.agency   = agency;
        this.managers = managers;
        if (agency.managerId) {
          this.managerForm.patchValue({ managerId: agency.managerId });
        }
        this.loading = false;
      },
      error: () => { this.loading = false; this.router.navigate(['/admin/agencies']); },
    });
  }

  assignManager(): void {
    if (this.managerForm.invalid || !this.agency) return;
    this.saving = true;
    this.agencyService.assignManager(this.agency.id, this.managerForm.value).subscribe({
      next: msg => {
        this.saving = false;
        this.snackBar.open(msg || 'Manager assigned', 'OK', { duration: 3000 });
        this.reload();
      },
      error: err => {
        this.saving = false;
        this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 });
      },
    });
  }

  toggleStatus(): void {
    if (!this.agency) return;
    const isActive = this.agency.active;
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title:       `${isActive ? 'Suspend' : 'Activate'} Agency`,
        message:     `${isActive ? 'Suspend' : 'Activate'} ${this.agency.name}?`,
        confirmText: isActive ? 'Suspend' : 'Activate',
        danger:      isActive,
      },
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed || !this.agency) return;
      const obs = isActive
        ? this.agencyService.suspend(this.agency.id)
        : this.agencyService.activate(this.agency.id);
      obs.subscribe({
        next: msg => {
          this.snackBar.open(msg || 'Updated', 'OK', { duration: 3000 });
          this.reload();
        },
        error: err => this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 }),
      });
    });
  }

  private reload(): void {
    if (!this.agency) return;
    this.agencyService.getById(this.agency.id).subscribe(a => this.agency = a);
  }
}
