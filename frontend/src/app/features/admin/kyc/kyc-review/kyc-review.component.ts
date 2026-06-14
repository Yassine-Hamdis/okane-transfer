import { Component, OnInit }    from '@angular/core';
import { CommonModule }         from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule }        from '@angular/material/card';
import { MatFormFieldModule }   from '@angular/material/form-field';
import { MatInputModule }       from '@angular/material/input';
import { MatSelectModule }      from '@angular/material/select';
import { MatButtonModule }      from '@angular/material/button';
import { MatIconModule }        from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDividerModule }     from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule }       from '@angular/material/chips';

import { KycService }               from '../../../../core/services/kyc.service';
import { KycRecord, KycStatus }     from '../../../../core/models/kyc.model';
import { LoadingSpinnerComponent }  from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent }     from '../../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-kyc-review',
  standalone: true,
  imports: [
    CommonModule, RouterModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatIconModule,
    MatProgressBarModule, MatDividerModule,
    MatSnackBarModule, MatChipsModule,
    LoadingSpinnerComponent, StatusBadgeComponent,
  ],
  templateUrl: './kyc-review.component.html',
  styleUrls:   ['./kyc-review.component.scss'],
})
export class KycReviewComponent implements OnInit {

  record:  KycRecord | null = null;
  loading  = true;
  saving   = false;
  form!:   FormGroup;

  readonly statusOptions: { value: KycStatus; label: string }[] = [
    { value: 'PASSED',  label: 'Passed — No issues found' },
    { value: 'FLAGGED', label: 'Flagged — Needs monitoring' },
    { value: 'BLOCKED', label: 'Blocked — Transaction denied' },
  ];

  constructor(
    private kycService: KycService,
    private fb:         FormBuilder,
    private route:      ActivatedRoute,
    private router:     Router,
    private snackBar:   MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      status: ['PASSED', Validators.required],
      notes:  ['', Validators.required],
    });

    const id = parseInt(this.route.snapshot.paramMap.get('id')!, 10);
    this.kycService.getByTransfer(id).subscribe({
      next: record => {
        this.record  = record;
        this.form.patchValue({ status: record.status, notes: record.notes ?? '' });
        this.loading = false;
      },
      error: () => { this.loading = false; this.router.navigate(['/admin/kyc']); },
    });
  }

  getRiskColor(score: number): string {
    if (score >= 80) return 'warn';
    if (score >= 50) return 'accent';
    return 'primary';
  }

  onSubmit(): void {
    if (!this.record || this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;

    this.kycService.review(this.record.id, this.form.value).subscribe({
      next: msg => {
        this.saving = false;
        this.snackBar.open(msg || 'Review submitted', 'OK', { duration: 3000 });
        this.router.navigate(['/admin/kyc']);
      },
      error: err => {
        this.saving = false;
        this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 });
      },
    });
  }
}
