import { Component, OnInit }        from '@angular/core';
import { CommonModule }             from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import {
  ReactiveFormsModule, FormBuilder, FormGroup, Validators,
} from '@angular/forms';
import { MatCardModule }        from '@angular/material/card';
import { MatFormFieldModule }   from '@angular/material/form-field';
import { MatInputModule }       from '@angular/material/input';
import { MatSelectModule }      from '@angular/material/select';
import { MatButtonModule }      from '@angular/material/button';
import { MatIconModule }        from '@angular/material/icon';
import { MatCheckboxModule }    from '@angular/material/checkbox';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

import { UserService }          from '../../../../core/services/user.service';
import { AgencyService }        from '../../../../core/services/agency.service';
import { Agency }               from '../../../../core/models/agency.model';
import { UserRole }             from '../../../../core/models/user.model';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [
    CommonModule, RouterModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatIconModule,
    MatCheckboxModule, MatSnackBarModule, MatProgressSpinnerModule,
    LoadingSpinnerComponent,
  ],
  templateUrl: './user-form.component.html',
  styleUrls:   ['./user-form.component.scss'],
})
export class UserFormComponent implements OnInit {

  form!:      FormGroup;
  loading     = false;
  saving      = false;
  isEditMode  = false;
  userId:     number | null = null;
  agencies:   Agency[] = [];
  hidePassword = true;

  readonly roles: { value: UserRole; label: string }[] = [
    { value: 'ROLE_ADMIN',   label: 'Admin' },
    { value: 'ROLE_MANAGER', label: 'Manager' },
    { value: 'ROLE_AGENT',   label: 'Agent' },
    { value: 'ROLE_CLIENT',  label: 'Client' },
  ];

  constructor(
    private fb:            FormBuilder,
    private userService:   UserService,
    private agencyService: AgencyService,
    private router:        Router,
    private route:         ActivatedRoute,
    private snackBar:      MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.buildForm();
    this.loadAgencies();

    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'new') {
      this.isEditMode = true;
      this.userId     = parseInt(id, 10);
      this.loadUser(this.userId);
    }
  }

  private buildForm(): void {
    this.form = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName:  ['', [Validators.required, Validators.minLength(2)]],
      email:     ['', [Validators.required, Validators.email]],
      phone:     ['', [Validators.required]],
      role:      ['ROLE_CLIENT', Validators.required],
      agencyId:  [null],
      password:  [''],
    });

    // Require password on create
    if (!this.isEditMode) {
      this.form.get('password')!.setValidators([
        Validators.required, Validators.minLength(8),
      ]);
    }
  }

  private loadAgencies(): void {
    this.agencyService.getActive().subscribe(
      agencies => this.agencies = agencies
    );
  }

  private loadUser(id: number): void {
    this.loading = true;
    this.userService.getById(id).subscribe({
      next: user => {
        this.form.patchValue({
          firstName: user.firstName,
          lastName:  user.lastName,
          email:     user.email,
          phone:     user.phone,
          role:      user.role,
          agencyId:  user.agencyId,
        });
        this.loading = false;
      },
      error: () => { this.loading = false; this.router.navigate(['/admin/users']); },
    });
  }

  get firstName() { return this.form.get('firstName')!; }
  get lastName()  { return this.form.get('lastName')!; }
  get email()     { return this.form.get('email')!; }
  get phone()     { return this.form.get('phone')!; }
  get role()      { return this.form.get('role')!; }

  needsAgency(): boolean {
    return ['ROLE_MANAGER', 'ROLE_AGENT'].includes(this.form.value.role);
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    const v = this.form.value;

    if (this.isEditMode && this.userId) {
      this.userService.update(this.userId, {
        firstName: v.firstName, lastName: v.lastName,
        email: v.email, phone: v.phone,
        role: v.role, agencyId: v.agencyId,
      }).subscribe({
        next: () => {
          this.saving = false;
          this.snackBar.open('User updated', 'OK', { duration: 3000 });
          this.router.navigate(['/admin/users']);
        },
        error: err => {
          this.saving = false;
          this.snackBar.open(err.error?.message ?? 'Update failed', 'OK', { duration: 3000 });
        },
      });
    } else {
      this.userService.create({
        firstName: v.firstName, lastName: v.lastName,
        email: v.email, phone: v.phone,
        role: v.role, agencyId: v.agencyId,
        password: v.password,
      }).subscribe({
        next: () => {
          this.saving = false;
          this.snackBar.open('User created', 'OK', { duration: 3000 });
          this.router.navigate(['/admin/users']);
        },
        error: err => {
          this.saving = false;
          this.snackBar.open(err.error?.message ?? 'Create failed', 'OK', { duration: 3000 });
        },
      });
    }
  }
}
