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

import { FeeGridService }           from '../../../core/services/fee-grid.service';
import { CorridorService }          from '../../../core/services/corridor.service';
import { FeeGrid, TransferType, CreateFeeGridRequest } from '../../../core/models/fee-grid.model';
import { Corridor }                 from '../../../core/models/corridor.model';
import { LoadingSpinnerComponent }  from '../../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent }     from '../../../shared/components/status-badge/status-badge.component';
import { ConfirmDialogComponent }   from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { CurrencyFormatPipe }       from '../../../shared/pipes/currency-format.pipe';

@Component({
    selector: 'app-fee-grid-list',
    standalone: true,
    imports: [
        CommonModule, ReactiveFormsModule,
        MatTableModule, MatPaginatorModule,
        MatCardModule, MatButtonModule, MatIconModule,
        MatInputModule, MatFormFieldModule, MatSelectModule,
        MatSnackBarModule, MatDialogModule, MatTooltipModule,
        LoadingSpinnerComponent, StatusBadgeComponent,
    ],
    templateUrl: './fee-grid-list.component.html',
    styleUrls:   ['./fee-grid-list.component.scss'],
})
export class FeeGridListComponent implements OnInit {

    @ViewChild(MatPaginator) paginator!: MatPaginator;

    displayedColumns = [
        'corridor', 'range', 'feeFixed', 'feePercentage',
        'type', 'shares', 'status', 'actions',
    ];
    dataSource   = new MatTableDataSource<FeeGrid>();
    loading      = true;
    corridors:   Corridor[] = [];
    showForm     = false;
    editingId:   number | null = null;
    saving       = false;
    form!:       FormGroup;

    readonly transferTypes: TransferType[] = ['STANDARD', 'EXPRESS', 'MOBILE_MONEY'];

    constructor(
        private feeGridService:  FeeGridService,
        private corridorService: CorridorService,
        private fb:              FormBuilder,
        private snackBar:        MatSnackBar,
        private dialog:          MatDialog,
    ) {}

    ngOnInit(): void {
        this.buildForm();
        forkJoin({
            grids:     this.feeGridService.getAll(),
            corridors: this.corridorService.getActive(),
        }).subscribe({
            next: ({ grids, corridors }) => {
                this.dataSource.data      = grids;
                this.dataSource.paginator = this.paginator;
                this.corridors            = corridors;
                this.loading              = false;
            },
            error: () => { this.loading = false; },
        });
    }

    private buildForm(): void {
        this.form = this.fb.group({
            corridorId:             [null, Validators.required],
            minAmount:              [0,    [Validators.required, Validators.min(0)]],
            maxAmount:              [null, [Validators.required, Validators.min(0)]],
            feeFixed:               [0,    [Validators.required, Validators.min(0)]],
            feePercentage:          [0,    [Validators.required, Validators.min(0), Validators.max(100)]],
            transferType:           ['STANDARD', Validators.required],
            agencySharePercentage:  [50,   [Validators.required, Validators.min(0), Validators.max(100)]],
            centralSharePercentage: [50,   [Validators.required, Validators.min(0), Validators.max(100)]],
        });
    }

    openCreate(): void {
        this.editingId = null;
        this.form.reset({
            minAmount: 0, feeFixed: 0, feePercentage: 0,
            transferType: 'STANDARD',
            agencySharePercentage: 50, centralSharePercentage: 50,
        });
        this.showForm = true;
    }

    openEdit(g: FeeGrid): void {
        this.editingId = g.id;
        this.form.patchValue(g);
        this.showForm  = true;
    }

    cancel(): void { this.showForm = false; this.editingId = null; }

    onSubmit(): void {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }
        this.saving = true;
        const v     = this.form.value;

        const obs = this.editingId
            ? this.feeGridService.update(this.editingId, v)
            : this.feeGridService.create(v as CreateFeeGridRequest);

        obs.subscribe({
            next: () => {
                this.saving   = false;
                this.showForm = false;
                this.snackBar.open(this.editingId ? 'Fee grid updated' : 'Fee grid created', 'OK', { duration: 3000 });
                this.reload();
            },
            error: err => {
                this.saving = false;
                this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 });
            },
        });
    }

    toggle(g: FeeGrid): void {
        const ref = this.dialog.open(ConfirmDialogComponent, {
            data: {
                title:       `${g.active ? 'Deactivate' : 'Activate'} Fee Grid`,
                message:     `${g.active ? 'Deactivate' : 'Activate'} this fee grid?`,
                confirmText: g.active ? 'Deactivate' : 'Activate',
                danger:      g.active,
            },
        });
        ref.afterClosed().subscribe(confirmed => {
            if (!confirmed) return;
            this.feeGridService.toggle(g.id).subscribe({
                next: msg => {
                    this.snackBar.open(msg || 'Updated', 'OK', { duration: 3000 });
                    this.reload();
                },
                error: err => this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 }),
            });
        });
    }

    private reload(): void {
        this.feeGridService.getAll().subscribe(grids => {
            this.dataSource.data = grids;
        });
    }

    applyFilter(v: string): void {
        this.dataSource.filter = v.trim().toLowerCase();
    }
}