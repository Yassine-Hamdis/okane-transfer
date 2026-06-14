import { Injectable }      from '@angular/core';
import { HttpClient }      from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment }     from '../../../environments/environment';
import { ApiResponse }     from '../models/api-response.model';
import {
    FeeGrid,
    CreateFeeGridRequest,
    UpdateFeeGridRequest,
    FeeSimulateRequest,
    FeeSimulationResponse,
} from '../models/fee-grid.model';

@Injectable({ providedIn: 'root' })
export class FeeGridService {

    private readonly adminBase = `${environment.apiUrl}/admin/fee-grids`;
    private readonly feesBase  = `${environment.apiUrl}/fees`;

    constructor(private http: HttpClient) {}

    // ── GET ─────────────────────────────────────────────────────────────────────

    getAll(): Observable<FeeGrid[]> {
        return this.http
            .get<ApiResponse<FeeGrid[]>>(this.adminBase)
            .pipe(map(res => res.data!));
    }

    getById(id: number): Observable<FeeGrid> {
        return this.http
            .get<ApiResponse<FeeGrid>>(`${this.adminBase}/${id}`)
            .pipe(map(res => res.data!));
    }

    getByCorridor(corridorId: number): Observable<FeeGrid[]> {
        return this.http
            .get<ApiResponse<FeeGrid[]>>(`${this.adminBase}/corridor/${corridorId}`)
            .pipe(map(res => res.data!));
    }

    // ── CREATE / UPDATE ─────────────────────────────────────────────────────────

    create(request: CreateFeeGridRequest): Observable<FeeGrid> {
        return this.http
            .post<ApiResponse<FeeGrid>>(this.adminBase, request)
            .pipe(map(res => res.data!));
    }

    update(id: number, request: UpdateFeeGridRequest): Observable<FeeGrid> {
        return this.http
            .put<ApiResponse<FeeGrid>>(`${this.adminBase}/${id}`, request)
            .pipe(map(res => res.data!));
    }

    // ── PATCH (void operations) ─────────────────────────────────────────────────

    toggle(id: number): Observable<string> {
        return this.http
            .patch<ApiResponse<void>>(`${this.adminBase}/${id}/toggle`, {})
            .pipe(map(res => res.message));
    }

    // ── FEE SIMULATION (Admin + Manager + Agent) ────────────────────────────────

    simulate(request: FeeSimulateRequest): Observable<FeeSimulationResponse> {
        return this.http
            .post<ApiResponse<FeeSimulationResponse>>(`${this.feesBase}/simulate`, request)
            .pipe(map(res => res.data!));
    }
}