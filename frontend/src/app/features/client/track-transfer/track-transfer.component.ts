import { Component, OnInit }       from '@angular/core';
import { CommonModule }            from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import {
  ReactiveFormsModule, FormBuilder, FormGroup, Validators,
} from '@angular/forms';
import { MatCardModule }           from '@angular/material/card';
import { MatFormFieldModule }      from '@angular/material/form-field';
import { MatInputModule }          from '@angular/material/input';
import { MatButtonModule }         from '@angular/material/button';
import { MatIconModule }           from '@angular/material/icon';
import { MatDividerModule }        from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatStepperModule }        from '@angular/material/stepper';
import { HttpErrorResponse }       from '@angular/common/http';

import { ClientService }           from '../../../core/services/client.service';
import { TransferTrack }           from '../../../core/models/transfer.model';
import { StatusBadgeComponent }    from '../../../shared/components/status-badge/status-badge.component';
import { CurrencyFormatPipe }      from '../../../shared/pipes/currency-format.pipe';

@Component({
  selector: 'app-track-transfer',
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
    MatDividerModule,
    MatProgressSpinnerModule,
    MatStepperModule,
    StatusBadgeComponent,
    CurrencyFormatPipe,
  ],
  templateUrl: './track-transfer.component.html',
  styleUrls:   ['./track-transfer.component.scss'],
})
export class TrackTransferComponent implements OnInit {

  form!:        FormGroup;
  tracking      = false;
  errorMessage  = '';
  result: TransferTrack | null = null;

  constructor(
    private fb:            FormBuilder,
    private clientService: ClientService,
    private route:         ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      code: ['', [Validators.required, Validators.minLength(4)]],
    });

    // If route has :code param (from /track/:code), auto-track
    const codeParam = this.route.snapshot.paramMap.get('code');
    if (codeParam && codeParam !== 'search') {
      this.form.patchValue({ code: codeParam });
      this.track();
    }
  }

  track(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.tracking     = true;
    this.errorMessage = '';
    this.result       = null;

    const code = this.form.value.code.trim().toUpperCase();

    this.clientService.trackTransfer(code).subscribe({
      next: result => {
        this.result   = result;
        this.tracking = false;
      },
      error: (err: HttpErrorResponse) => {
        this.tracking     = false;
        this.errorMessage = err.error?.message ?? 'Transfer not found. Please check the code.';
      },
    });
  }

  reset(): void {
    this.result       = null;
    this.errorMessage = '';
    this.form.reset();
  }

  getStatusIcon(status: string): string {
    const map: Record<string, string> = {
      EN_ATTENTE: 'schedule',
      PAYE:       'check_circle',
      ANNULE:     'cancel',
      EXPIRE:     'timer_off',
    };
    return map[status] ?? 'help_outline';
  }

  getStatusColor(status: string): string {
    const map: Record<string, string> = {
      EN_ATTENTE: '#0277bd',
      PAYE:       '#2e7d32',
      ANNULE:     '#c62828',
      EXPIRE:     '#757575',
    };
    return map[status] ?? '#424242';
  }

  isExpired(expiresAt: string): boolean {
    return new Date(expiresAt) < new Date();
  }
}
