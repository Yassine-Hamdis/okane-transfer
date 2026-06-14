import { Component, OnInit }       from '@angular/core';
import { CommonModule }            from '@angular/common';
import { RouterModule }            from '@angular/router';
import {
  ReactiveFormsModule, FormBuilder, FormGroup, Validators,
} from '@angular/forms';
import { MatCardModule }           from '@angular/material/card';
import { MatFormFieldModule }      from '@angular/material/form-field';
import { MatInputModule }          from '@angular/material/input';
import { MatButtonModule }         from '@angular/material/button';
import { MatIconModule }           from '@angular/material/icon';
import { MatSlideToggleModule }    from '@angular/material/slide-toggle';
import { MatDividerModule }        from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

import { MatTooltipModule } from '@angular/material/tooltip';

import { ClientService }           from '../../../core/services/client.service';
import { User }                    from '../../../core/models/user.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent }  from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSlideToggleModule,
    MatDividerModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    LoadingSpinnerComponent,
    MatTooltipModule,
  ],
  templateUrl: './profile.component.html',
  styleUrls:   ['./profile.component.scss'],
})
export class ProfileComponent implements OnInit {

  loading      = true;
  saving       = false;
  togglingTwoFa = false;
  editMode     = false;
  profile: User | null = null;
  form!: FormGroup;

  constructor(
    private clientService: ClientService,
    private fb:            FormBuilder,
    private snackBar:      MatSnackBar,
    private dialog:        MatDialog,
  ) {}

  ngOnInit(): void {
    this.clientService.getProfile().subscribe({
      next: profile => {
        this.profile = profile;
        this.buildForm(profile);
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });
  }

  private buildForm(profile: User): void {
    this.form = this.fb.group({
      firstName: [profile.firstName, [Validators.required, Validators.minLength(2)]],
      lastName:  [profile.lastName,  [Validators.required, Validators.minLength(2)]],
      phone:     [profile.phone,     Validators.required],
    });
  }

  enableEdit(): void  { this.editMode = true; }
  cancelEdit(): void  {
    this.editMode = false;
    if (this.profile) this.buildForm(this.profile);
  }

  onSave(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;

    this.clientService.updateProfile(this.form.value).subscribe({
      next: updated => {
        this.profile  = updated;
        this.saving   = false;
        this.editMode = false;

        // Update localStorage name
        localStorage.setItem('okane_full_name',
          `${updated.firstName} ${updated.lastName}`);

        this.snackBar.open('Profile updated', 'OK', { duration: 3000 });
      },
      error: err => {
        this.saving = false;
        this.snackBar.open(err.error?.message ?? 'Update failed', 'OK', { duration: 3000 });
      },
    });
  }

  toggleTwoFactor(): void {
    const current = this.profile?.twoFactorEnabled;
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title:   current ? 'Disable 2FA' : 'Enable 2FA',
        message: current
          ? 'Disabling 2FA reduces your account security. Continue?'
          : 'Enable two-factor authentication for extra security?',
        confirmText: current ? 'Disable' : 'Enable',
        danger: current,
      },
    });

    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.togglingTwoFa = true;
      this.clientService.toggleTwoFactor().subscribe({
        next: msg => {
          this.togglingTwoFa = false;
          if (this.profile) {
            this.profile = {
              ...this.profile,
              twoFactorEnabled: !this.profile.twoFactorEnabled,
            };
          }
          this.snackBar.open(msg || '2FA updated', 'OK', { duration: 3000 });
        },
        error: err => {
          this.togglingTwoFa = false;
          this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 });
        },
      });
    });
  }

  getInitials(): string {
    if (!this.profile) return '';
    return `${this.profile.firstName[0]}${this.profile.lastName[0]}`.toUpperCase();
  }
}
