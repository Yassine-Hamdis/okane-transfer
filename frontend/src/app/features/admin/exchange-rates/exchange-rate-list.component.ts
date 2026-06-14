import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule }                 from '@angular/common';
import {
    ReactiveFormsModule, FormBuilder, FormGroup, Validators,
} from '@angular/forms';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator }   from '@angular/material/paginator';
import { MatCardModule }        from '@angular/material/card';
import { MatButtonModule }      from '@angular/material/button';
import { MatIconModule }        from '@angular/material/icon';
import { MatInputModule }       from '@angular/material/input';
import { MatFormFieldModule }   from '@angular/material/form-field';
import { MatSelectModule }      from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule }     from '@angular/material/tooltip';
import { MatChipsModule }       from '@angular/material/chips';

import { ExchangeRateService }      from '../../../core/services/exchange-rate.service';
import { CorridorService }          from '../../../core/services/corridor.service';
import { ExchangeRate }             from '../../../core/models/exchange-rate.model';
import { Corridor }                 from '../../../core/models/corridor.model';
import { LoadingSpinnerComponent }  from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
    selector: 'app-exchange-rate-list',
    standalone: true,
    imports: [
        CommonModule, ReactiveFormsModule,
        MatTableModule, MatPaginatorModule,
        MatCardModule, MatButtonModule, MatIconModule,
        MatInputModule, MatFormFieldModule, MatSelectModule,
        MatSnackBarModule, MatTooltipModule, MatChipsModule,
        LoadingSpinnerComponent,
    ],
    templateUrl: './exchange-rate-list.component.html',
    styleUrls:   ['./exchange-rate-list.component.scss'],
})
export class ExchangeRateListComponent implements OnInit {

    @ViewChild(MatPaginator) paginator!: MatPaginator;

    displayedColumns = ['corridor', 'rate', 'source', 'current', 'updatedBy', 'recordedAt'];
    dataSource       = new MatTableDataSource<ExchangeRate>();
    loading          = true;
    corridors:       Corridor[] = [];
    selectedCorridor: number | null = null;
    showForm         = false;
    saving           = false;
    form!:           FormGroup;

    constructor(
        private rateService:     ExchangeRateService,
        private corridorService: CorridorService,
        private fb:              FormBuilder,
        private snackBar:        MatSnackBar,
    ) {}

    ngOnInit(): void {
        this.form = this.fb.group({
            corridorId: [null, Validators.required],
            rate:       [null, [Validators.required, Validators.min(0.000001)]],
        });

        this.corridorService.getActive().subscribe(corridors => {
            this.corridors = corridors;
            this.loading   = false;
        });
    }

    loadHistory(): void {
        if (!this.selectedCorridor) return;
        this.loading = true;
        this.rateService.getHistory(this.selectedCorridor).subscribe({
            next: rates => {
                this.dataSource.data      = rates;
                this.dataSource.paginator = this.paginator;
                this.loading              = false;
            },
            error: () => { this.loading = false; },
        });
    }

    onCorridorChange(corridorId: number): void {
        this.selectedCorridor = corridorId;
        this.loadHistory();
    }

    openCreate(): void { this.showForm = true; }
    cancel(): void     { this.showForm = false; this.form.patchValue({ rate: null }); }

    onSubmit(): void {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }
        this.saving = true;
        const { corridorId, rate } = this.form.value;
        this.rateService.create(corridorId, { rate }).subscribe({
            next: () => {
                this.saving   = false;
                this.showForm = false;
                this.snackBar.open('Exchange rate updated', 'OK', { duration: 3000 });
                this.selectedCorridor = corridorId;
                this.loadHistory();
            },
            error: err => {
                this.saving = false;
                this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 });
            },
        });
    }
}