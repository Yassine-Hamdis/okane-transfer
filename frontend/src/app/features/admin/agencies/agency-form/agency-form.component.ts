import { Component, OnInit }       from '@angular/core';
import { CommonModule }            from '@angular/common';
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
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { forkJoin }             from 'rxjs';

import { AgencyService }           from '../../../../core/services/agency.service';
import { CountryService }          from '../../../../core/services/country.service';
import { UserService }             from '../../../../core/services/user.service';
import { Country }                 from '../../../../core/models/country.model';
import { User }                    from '../../../../core/models/user.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-agency-form',
  standalone: true,
  imports: [
    CommonModule, RouterModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatIconModule,
    MatSnackBarModule, LoadingSpinnerComponent,
  ],
  templateUrl: './agency-form.component.html',
  styleUrls:   ['./agency-form.component.scss'],
})
export class AgencyFormComponent implements OnInit {

  form!:      FormGroup;
  loading     = true;
  saving      = false;
  isEditMode  = false;
  agencyId:   number | null = null;
  countries:  Country[] = [];
  managers:   User[]    = [];

  constructor(
    private fb:            FormBuilder,
    private agencyService: AgencyService,
    private countryService: CountryService,
    private userService:   UserService,
    private router:        Router,
    private route:         ActivatedRoute,
    private snackBar:      MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name:       ['', Validators.required],
      address:    ['', Validators.required],
      city:       ['', Validators.required],
      countryId:  [null, Validators.required],
      dailyLimit: [10000, [Validators.required, Validators.min(1)]],
    });

    const id = this.route.snapshot.paramMap.get('id');
    this.isEditMode = id !== null && id !== 'new';
    if (this.isEditMode) this.agencyId = parseInt(id!, 10);

    forkJoin({
      countries: this.countryService.getActive(),
      managers:  this.userService.getByRole('ROLE_MANAGER'),
    }).subscribe(({ countries, managers }) => {
      this.countries = countries;
      this.managers  = managers;

      if (this.isEditMode && this.agencyId) {
        this.agencyService.getById(this.agencyId).subscribe(agency => {
          this.form.patchValue({
            name: agency.name, address: agency.address,
            city: agency.city, dailyLimit: agency.dailyLimit,
          });
          this.loading = false;
        });
      } else {
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    const v     = this.form.value;

    const obs = this.isEditMode && this.agencyId
      ? this.agencyService.update(this.agencyId, v)
      : this.agencyService.create(v);

    obs.subscribe({
      next: () => {
        this.saving = false;
        this.snackBar.open(this.isEditMode ? 'Agency updated' : 'Agency created', 'OK', { duration: 3000 });
        this.router.navigate(['/admin/agencies']);
      },
      error: err => {
        this.saving = false;
        this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 });
      },
    });
  }
}
