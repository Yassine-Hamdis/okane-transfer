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
import { MatDialog, MatDialogModule }    from '@angular/material/dialog';
import { MatTooltipModule }     from '@angular/material/tooltip';
import { forkJoin }             from 'rxjs';

import { CorridorService }          from '../../../core/services/corridor.service';
import { CountryService }           from '../../../core/services/country.service';
import { CurrencyService }          from '../../../core/services/currency.service';
import { Corridor }                 from '../../../core/models/corridor.model';
import { Country }                  from '../../../core/models/country.model';
import { Currency }                 from '../../../core/models/currency.model';
import { LoadingSpinnerComponent }  from '../../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent }     from '../../../shared/components/status-badge/status-badge.component';
import { ConfirmDialogComponent }   from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
    selector: 'app-corridor-list',
    standalone: true,
    imports: [
        CommonModule, ReactiveFormsModule,
        MatTableModule, MatPaginatorModule,
        MatCardModule, MatButtonModule, MatIconModule,
        MatInputModule, MatFormFieldModule, MatSelectModule,
        MatSnackBarModule, MatDialogModule, MatTooltipModule,
        LoadingSpinnerComponent, StatusBadgeComponent,
    ],
    templateUrl: './corridor-list.component.html',
    styleUrls:   ['./corridor-list.component.scss'],
})
export class CorridorListComponent implements OnInit {

    @ViewChild(MatPaginator) paginator!: MatPaginator;

    displayedColumns = ['corridor', 'currencies', 'status', 'createdAt', 'actions'];
    dataSource       = new MatTableDataSource<Corridor>();
    loading          = true;
    showForm         = false;
    saving           = false;
    countries:   Country[]  = [];
    currencies:  Currency[] = [];
    form!:       FormGroup;

    constructor(
        private corridorService:  CorridorService,
        private countryService:   CountryService,
        private currencyService:  CurrencyService,
        private fb:               FormBuilder,
        private snackBar:         MatSnackBar,
        private dialog:           MatDialog,
    ) {}

    ngOnInit(): void {
        this.buildForm();
        forkJoin({
            corridors:  this.corridorService.getAll(),
            countries:  this.countryService.getActive(),
            currencies: this.currencyService.getActive(),
        }).subscribe({
            next: ({ corridors, countries, currencies }) => {
                this.dataSource.data      = corridors;
                this.dataSource.paginator = this.paginator;
                this.countries            = countries;
                this.currencies           = currencies;
                this.loading              = false;
            },
            error: () => { this.loading = false; },
        });
    }

    private buildForm(): void {
        this.form = this.fb.group({
            sourceCountryId:      [null, Validators.required],
            destinationCountryId: [null, Validators.required],
            sourceCurrencyId:     [null, Validators.required],
            destinationCurrencyId:[null, Validators.required],
        });
    }

    openCreate(): void { this.form.reset(); this.showForm = true; }
    cancel(): void     { this.showForm = false; this.form.reset(); }

    onSubmit(): void {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }
        this.saving = true;
        this.corridorService.create(this.form.value).subscribe({
            next: () => {
                this.saving   = false;
                this.showForm = false;
                this.snackBar.open('Corridor created', 'OK', { duration: 3000 });
                this.reload();
            },
            error: err => {
                this.saving = false;
                this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 });
            },
        });
    }

    toggle(c: Corridor): void {
        const ref = this.dialog.open(ConfirmDialogComponent, {
            data: {
                title:       `${c.active ? 'Deactivate' : 'Activate'} Corridor`,
                message:     `${c.active ? 'Deactivate' : 'Activate'} ${c.sourceCountryName} → ${c.destinationCountryName}?`,
                confirmText: c.active ? 'Deactivate' : 'Activate',
                danger:      c.active,
            },
        });
        ref.afterClosed().subscribe(confirmed => {
            if (!confirmed) return;
            this.corridorService.toggle(c.id).subscribe({
                next: msg => {
                    this.snackBar.open(msg || 'Updated', 'OK', { duration: 3000 });
                    this.reload();
                },
                error: err => this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 }),
            });
        });
    }

    private reload(): void {
        this.corridorService.getAll().subscribe(list => {
            this.dataSource.data = list;
        });
    }

    applyFilter(v: string): void {
        this.dataSource.filter = v.trim().toLowerCase();
    }
}