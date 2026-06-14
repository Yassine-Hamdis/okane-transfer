import { Component, OnInit }    from '@angular/core';
import { CommonModule }         from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { MatCardModule }            from '@angular/material/card';
import { MatFormFieldModule }       from '@angular/material/form-field';
import { MatInputModule }           from '@angular/material/input';
import { MatButtonModule }          from '@angular/material/button';
import { MatIconModule }            from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { HttpErrorResponse }        from '@angular/common/http';

import { AuthService }              from '../../../core/services/auth.service';

@Component({
  selector: 'app-verify-two-factor',
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
  templateUrl: './verify-two-factor.component.html',
  styleUrls:   ['./verify-two-factor.component.scss'],
})
export class VerifyTwoFactorComponent implements OnInit {

  form!:        FormGroup;
  loading       = false;
  errorMessage  = '';
  email         = '';

  constructor(
    private fb:          FormBuilder,
    private authService: AuthService,
    private router:      Router,
  ) {}

  ngOnInit(): void {
    const nav   = this.router.getCurrentNavigation();
    const state = nav?.extras?.state as { email?: string } | undefined;

    // Try navigation state first, fall back to sessionStorage
    this.email = state?.email
      ?? sessionStorage.getItem('okane_pending_email')
      ?? '';

    if (!this.email) {
      this.router.navigate(['/login']);
      return;
    }

    // Persist for soft refresh within same tab
    sessionStorage.setItem('okane_pending_email', this.email);

    this.form = this.fb.group({
      otpCode: ['', [
        Validators.required,
        Validators.minLength(6),
        Validators.maxLength(6),
        Validators.pattern(/^[0-9]{6}$/),
      ]],
    });
  }

  get otpCode() { return this.form.get('otpCode')!; }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading      = true;
    this.errorMessage = '';

    this.authService
      .verifyTwoFactor({ email: this.email, otpCode: this.form.value.otpCode })
      .subscribe({
        next: response => {
          this.loading = false;
          sessionStorage.removeItem('okane_pending_email'); // clean up
          this.authService.saveSession(response);

          if (response.mustChangePassword) {
            this.router.navigate(['/change-password'], {
              state: { forced: true },
            });
          } else {
            this.authService.redirectByRole();
          }
        },
        error: (err: HttpErrorResponse) => {
          this.loading      = false;
          this.errorMessage = err.error?.message ?? 'Invalid or expired OTP code.';
          this.form.reset();
        },
      });
  }

  goBack(): void {
    this.router.navigate(['/login']);
  }
}
