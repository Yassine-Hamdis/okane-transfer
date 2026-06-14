import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule }                 from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator }   from '@angular/material/paginator';
import { MatCardModule }   from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule }   from '@angular/material/icon';
import { MatInputModule }  from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCheckboxModule } from '@angular/material/checkbox';

import { CountryService }           from '../../../core/services/country.service';
import { Country }                  from '../../../core/models/country.model';
import { LoadingSpinnerComponent }  from '../../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent }     from '../../../shared/components/status-badge/status-badge.component';
import { ConfirmDialogComponent }   from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
    selector: 'app-country-list',
    standalone: true,
    imports: [
        CommonModule, FormsModule, ReactiveFormsModule,
        MatTableModule, MatPaginatorModule, MatCardModule,
        MatButtonModule, MatIconModule, MatInputModule,
        MatFormFieldModule, MatSlideToggleModule, MatDialogModule,
        MatSnackBarModule, MatTooltipModule, MatCheckboxModule,
        LoadingSpinnerComponent, StatusBadgeComponent,
    ],
    templateUrl: './country-list.component.html',
    styleUrls:   ['./country-list.component.scss'],
})
export class CountryListComponent implements OnInit {

    @ViewChild(MatPaginator) paginator!: MatPaginator;

    displayedColumns = ['name', 'code', 'sending', 'receiving', 'status', 'actions'];
    dataSource       = new MatTableDataSource<Country>();
    loading          = true;
    showForm         = false;
    editingId:       number | null = null;
    saving           = false;
    form!:           FormGroup;

    constructor(
        private countryService: CountryService,
        private fb:             FormBuilder,
        private snackBar:       MatSnackBar,
        private dialog:         MatDialog,
    ) {}

    ngOnInit(): void {
        this.buildForm();
        this.loadCountries();
    }

    private buildForm(): void {
        this.form = this.fb.group({
            name:            ['', Validators.required],
            code:            ['', [Validators.required, Validators.maxLength(3)]],
            allowsSending:   [true],
            allowsReceiving: [true],
        });
    }

    loadCountries(): void {
        this.loading = true;
        this.countryService.getAll().subscribe({
            next: countries => {
                this.dataSource.data      = countries;
                this.dataSource.paginator = this.paginator;
                this.loading              = false;
            },
            error: () => { this.loading = false; },
        });
    }

    openCreateForm(): void {
        this.editingId = null;
        this.form.reset({ allowsSending: true, allowsReceiving: true });
        this.showForm  = true;
    }

    openEditForm(country: Country): void {
        this.editingId = country.id;
        this.form.patchValue(country);
        this.showForm  = true;
    }

    cancelForm(): void {
        this.showForm  = false;
        this.editingId = null;
        this.form.reset();
    }

    onSubmit(): void {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }
        this.saving = true;
        const v = this.form.value;

        const obs = this.editingId
            ? this.countryService.update(this.editingId, v)
            : this.countryService.create(v);

        obs.subscribe({
            next: () => {
                this.saving   = false;
                this.showForm = false;
                this.snackBar.open(
                    this.editingId ? 'Country updated' : 'Country created', 'OK', { duration: 3000 }
                );
                this.loadCountries();
            },
            error: err => {
                this.saving = false;
                this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 });
            },
        });
    }

    toggle(country: Country): void {
        const action = country.active ? 'deactivate' : 'activate';
        const ref = this.dialog.open(ConfirmDialogComponent, {
            data: {
                title:       `${country.active ? 'Deactivate' : 'Activate'} Country`,
                message:     `Are you sure you want to ${action} ${country.name}?`,
                confirmText: country.active ? 'Deactivate' : 'Activate',
                danger:      country.active,
            },
        });
        ref.afterClosed().subscribe(confirmed => {
            if (!confirmed) return;
            this.countryService.toggle(country.id).subscribe({
                next: msg => {
                    this.snackBar.open(msg || 'Updated', 'OK', { duration: 3000 });
                    this.loadCountries();
                },
                error: err => this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 }),
            });
        });
    }

    applyFilter(value: string): void {
        this.dataSource.filter = value.trim().toLowerCase();
    }
}