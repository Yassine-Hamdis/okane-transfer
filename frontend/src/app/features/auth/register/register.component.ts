import { Component, OnInit }    from '@angular/core';
import { CommonModule }         from '@angular/common';
import { RouterModule }         from '@angular/router';
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
import { MatStepperModule }         from '@angular/material/stepper';
import { HttpErrorResponse }        from '@angular/common/http';
import { Router }                   from '@angular/router';

import { AuthService }              from '../../../core/services/auth.service';

// Custom validator: passwords must match
function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password        = control.get('password');
  const confirmPassword = control.get('confirmPassword');
  if (!password || !confirmPassword) return null;
  return password.value === confirmPassword.value
    ? null
    : { passwordMismatch: true };
}

@Component({
  selector: 'app-register',
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
    MatStepperModule,
  ],
  templateUrl: './register.component.html',
  styleUrls:   ['./register.component.scss'],
})
export class RegisterComponent implements OnInit {

  form!:         FormGroup;
  loading        = false;
  errorMessage   = '';
  successMessage = '';
  hidePassword   = true;
  hideConfirm    = true;

  constructor(
    private fb:          FormBuilder,
    private authService: AuthService,
    private router:      Router,
  ) {}

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      this.authService.redirectByRole();
      return;
    }

    this.form = this.fb.group(
      {
        firstName:       ['', [Validators.required, Validators.minLength(2)]],
        lastName:        ['', [Validators.required, Validators.minLength(2)]],
        email:           ['', [Validators.required, Validators.email]],
        phone:           ['', [Validators.required, Validators.pattern(/^\+?[0-9\s\-]{7,15}$/)]],
        password:        ['', [Validators.required, Validators.minLength(8),
          Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).+$/)]],
        confirmPassword: ['', Validators.required],
      },
      { validators: passwordMatchValidator }
    );
  }

  // Field getters
  get firstName()       { return this.form.get('firstName')!; }
  get lastName()        { return this.form.get('lastName')!; }
  get email()           { return this.form.get('email')!; }
  get phone()           { return this.form.get('phone')!; }
  get password()        { return this.form.get('password')!; }
  get confirmPassword() { return this.form.get('confirmPassword')!; }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading      = true;
    this.errorMessage = '';

    const { firstName, lastName, email, phone, password } = this.form.value;

    this.authService.register({ firstName, lastName, email, phone, password })
      .subscribe({
        next: (message: string) => {
          this.loading        = false;
          this.successMessage = message || 'Account created! You can now sign in.';
          setTimeout(() => this.router.navigate(['/login']), 2500);
        },
        error: (err: HttpErrorResponse) => {
          this.loading = false;
          // Handle field-level validation errors from backend
          const fieldErrors = err.error?.data as Record<string, string> | undefined;
          if (fieldErrors) {
            this.errorMessage = Object.values(fieldErrors).join('. ');
          } else {
            this.errorMessage = err.error?.message ?? 'Registration failed.';
          }
        },
      });
  }
}
