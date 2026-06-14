import { Component, OnInit }    from '@angular/core';
import { CommonModule }         from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { MatCardModule }            from '@angular/material/card';
import { MatFormFieldModule }       from '@angular/material/form-field';
import { MatInputModule }           from '@angular/material/input';
import { MatButtonModule }          from '@angular/material/button';
import { MatIconModule }            from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { HttpErrorResponse }        from '@angular/common/http';

import { ClientService }            from '../../../core/services/client.service';
import { AuthService }              from '../../../core/services/auth.service';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const newPassword     = control.get('newPassword');
  const confirmPassword = control.get('confirmPassword');
  if (!newPassword || !confirmPassword) return null;
  return newPassword.value === confirmPassword.value
    ? null
    : { passwordMismatch: true };
}

@Component({
  selector: 'app-change-password',
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
    MatProgressSpinnerModule,
  ],
  templateUrl: './change-password.component.html',
  styleUrls:   ['./change-password.component.scss'],
})
export class ChangePasswordComponent implements OnInit {

  form!:         FormGroup;
  loading        = false;
  errorMessage   = '';
  successMessage = '';

  /** true → user MUST change password before proceeding */
  isForced = false;

  hideCurrentPw  = true;
  hideNewPw      = true;
  hideConfirmPw  = true;

  constructor(
    private fb:            FormBuilder,
    private clientService: ClientService,
    private authService:   AuthService,
    private router:        Router,
  ) {}

  ngOnInit(): void {
    // Detect forced mode from router state
    const nav   = this.router.getCurrentNavigation();
    const state = nav?.extras?.state as { forced?: boolean } | undefined;
    this.isForced = state?.forced === true || sessionStorage.getItem('okane_force_pw') === 'true' ;

    if (this.isForced) {
      sessionStorage.setItem('okane_force_pw', 'true');
    }

    this.form = this.fb.group(
      {
        currentPassword: ['', Validators.required],
        newPassword: ['', [
          Validators.required,
          Validators.minLength(8),
          Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).+$/),
        ]],
        confirmPassword: ['', Validators.required],
      },
      { validators: passwordMatchValidator }
    );
  }

  get currentPassword() { return this.form.get('currentPassword')!; }
  get newPassword()     { return this.form.get('newPassword')!; }
  get confirmPassword() { return this.form.get('confirmPassword')!; }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading      = true;
    this.errorMessage = '';

    const { currentPassword, newPassword } = this.form.value;

    this.clientService
      .changePassword({ currentPassword, newPassword })
      .subscribe({
        next: (message: string) => {
          sessionStorage.removeItem('okane_force_pw'); // clean up
          this.loading        = false;
          this.successMessage = message || 'Password changed successfully!';

          setTimeout(() => {
            if (this.isForced) {
              // After forced change, redirect to dashboard
              this.authService.redirectByRole();
            } else {
              this.router.navigate(['/client/profile']);
            }
          }, 1800);
        },
        error: (err: HttpErrorResponse) => {
          this.loading      = false;
          this.errorMessage = err.error?.message ?? 'Failed to change password.';
        },
      });
  }

  skipIfNotForced(): void {
    if (!this.isForced) {
      this.router.navigate(['/client/profile']);
    }
  }
}
