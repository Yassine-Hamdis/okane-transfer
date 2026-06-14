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
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule }    from '@angular/material/dialog';
import { MatTooltipModule }     from '@angular/material/tooltip';

import { CurrencyService }          from '../../../core/services/currency.service';
import { Currency }                 from '../../../core/models/currency.model';
import { LoadingSpinnerComponent }  from '../../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent }     from '../../../shared/components/status-badge/status-badge.component';
import { ConfirmDialogComponent }   from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
    selector: 'app-currency-list',
    standalone: true,
    imports: [
        CommonModule, ReactiveFormsModule,
        MatTableModule, MatPaginatorModule,
        MatCardModule, MatButtonModule, MatIconModule,
        MatInputModule, MatFormFieldModule,
        MatSnackBarModule, MatDialogModule, MatTooltipModule,
        LoadingSpinnerComponent, StatusBadgeComponent,
    ],
    templateUrl: './currency-list.component.html',
    styleUrls:   ['./currency-list.component.scss'],
})
export class CurrencyListComponent implements OnInit {

    @ViewChild(MatPaginator) paginator!: MatPaginator;

    displayedColumns = ['code', 'name', 'symbol', 'status', 'actions'];
    dataSource       = new MatTableDataSource<Currency>();
    loading          = true;
    showForm         = false;
    editingId: number | null = null;
    saving           = false;
    form!:           FormGroup;

    constructor(
        private currencyService: CurrencyService,
        private fb:              FormBuilder,
        private snackBar:        MatSnackBar,
        private dialog:          MatDialog,
    ) {}

    ngOnInit(): void {
        this.buildForm();
        this.load();
    }

    private buildForm(): void {
        this.form = this.fb.group({
            code:   ['', [Validators.required, Validators.maxLength(5)]],
            name:   ['', Validators.required],
            symbol: ['', Validators.required],
        });
    }

    load(): void {
        this.loading = true;
        this.currencyService.getAll().subscribe({
            next: list => {
                this.dataSource.data      = list;
                this.dataSource.paginator = this.paginator;
                this.loading              = false;
            },
            error: () => { this.loading = false; },
        });
    }

    openCreate(): void {
        this.editingId = null;
        this.form.reset();
        this.showForm  = true;
    }

    openEdit(c: Currency): void {
        this.editingId = c.id;
        this.form.patchValue({ code: c.code, name: c.name, symbol: c.symbol });
        this.showForm  = true;
    }

    cancel(): void {
        this.showForm  = false;
        this.editingId = null;
        this.form.reset();
    }

    onSubmit(): void {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }
        this.saving = true;
        const v     = this.form.value;
        const obs   = this.editingId
            ? this.currencyService.update(this.editingId, v)
            : this.currencyService.create(v);

        obs.subscribe({
            next: () => {
                this.saving   = false;
                this.showForm = false;
                this.snackBar.open(this.editingId ? 'Currency updated' : 'Currency created', 'OK', { duration: 3000 });
                this.load();
            },
            error: err => {
                this.saving = false;
                this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 });
            },
        });
    }

    toggle(c: Currency): void {
        const ref = this.dialog.open(ConfirmDialogComponent, {
            data: {
                title:       `${c.active ? 'Deactivate' : 'Activate'} Currency`,
                message:     `${c.active ? 'Deactivate' : 'Activate'} ${c.name}?`,
                confirmText: c.active ? 'Deactivate' : 'Activate',
                danger:      c.active,
            },
        });
        ref.afterClosed().subscribe(confirmed => {
            if (!confirmed) return;
            this.currencyService.toggle(c.id).subscribe({
                next: msg => {
                    this.snackBar.open(msg || 'Updated', 'OK', { duration: 3000 });
                    this.load();
                },
                error: err => this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 }),
            });
        });
    }

    applyFilter(v: string): void {
        this.dataSource.filter = v.trim().toLowerCase();
    }
}