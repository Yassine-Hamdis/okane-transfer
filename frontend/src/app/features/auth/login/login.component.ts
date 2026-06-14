import { Component, OnInit }        from '@angular/core';
import { CommonModule }             from '@angular/common';
import { RouterModule, Router }     from '@angular/router';
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
import { MatCheckboxModule }        from '@angular/material/checkbox';
import { HttpErrorResponse }        from '@angular/common/http';

import { AuthService }              from '../../../core/services/auth.service';
import { LoginResponse }            from '../../../core/models/user.model';

@Component({
  selector: 'app-login',
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
    MatCheckboxModule,
  ],
  templateUrl: './login.component.html',
  styleUrls:   ['./login.component.scss'],
})
export class LoginComponent implements OnInit {

  form!: FormGroup;
  loading       = false;
  errorMessage  = '';
  hidePassword  = true;

  // Stored temporarily when 2FA is required
  private pendingEmail = '';

  constructor(
    private fb:          FormBuilder,
    private authService: AuthService,
    private router:      Router,
  ) {}

  ngOnInit(): void {
    // If already logged in → redirect to dashboard
    if (this.authService.isLoggedIn()) {
      this.authService.redirectByRole();
      return;
    }

    this.form = this.fb.group({
      email:    ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  get email()    { return this.form.get('email')!; }
  get password() { return this.form.get('password')!; }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading      = true;
    this.errorMessage = '';

    const { email, password } = this.form.value;
    this.pendingEmail = email;

    this.authService.login({ email, password }).subscribe({
      next: (response: LoginResponse) => {
        this.loading = false;
        this.handleResponse(response);
      },
      error: (err: HttpErrorResponse) => {
        this.loading      = false;
        this.errorMessage = err.error?.message ?? 'Login failed. Please try again.';
      },
    });
  }

  private handleResponse(response: LoginResponse): void {
    const outcome = this.authService.handleLoginResponse(response);

    switch (outcome) {
      case 'two-factor':
        // Navigate to 2FA page, pass email via state
        sessionStorage.setItem('okane_pending_email', this.pendingEmail);
        this.router.navigate(['/verify-2fa'], {
          state: { email: this.pendingEmail },
        });
        break;

      case 'change-password':
        // Session is saved, force password change
        sessionStorage.setItem('okane_force_pw', 'true');
        this.router.navigate(['/change-password'], {
          state: { forced: true },
        });
        break;

      case 'dashboard':
        // redirectByRole() already called inside handleLoginResponse
        break;
    }
  }
}
