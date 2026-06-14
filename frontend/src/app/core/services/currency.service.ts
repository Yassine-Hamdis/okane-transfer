import { Injectable }      from '@angular/core';
import { HttpClient }      from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment }     from '../../../environments/environment';
import { ApiResponse }     from '../models/api-response.model';
import {
    Currency,
    CreateCurrencyRequest,
    UpdateCurrencyRequest,
} from '../models/currency.model';

@Injectable({ providedIn: 'root' })
export class CurrencyService {

    private readonly base = `${environment.apiUrl}/admin/currencies`;

    constructor(private http: HttpClient) {}

    // ── GET ─────────────────────────────────────────────────────────────────────

    getAll(): Observable<Currency[]> {
        return this.http
            .get<ApiResponse<Currency[]>>(this.base)
            .pipe(map(res => res.data!));
    }

    getActive(): Observable<Currency[]> {
        return this.http
            .get<ApiResponse<Currency[]>>(`${this.base}/active`)
            .pipe(map(res => res.data!));
    }

    getById(id: number): Observable<Currency> {
        return this.http
            .get<ApiResponse<Currency>>(`${this.base}/${id}`)
            .pipe(map(res => res.data!));
    }

    // ── CREATE / UPDATE ─────────────────────────────────────────────────────────

    create(request: CreateCurrencyRequest): Observable<Currency> {
        return this.http
            .post<ApiResponse<Currency>>(this.base, request)
            .pipe(map(res => res.data!));
    }

    update(id: number, request: UpdateCurrencyRequest): Observable<Currency> {
        return this.http
            .put<ApiResponse<Currency>>(`${this.base}/${id}`, request)
            .pipe(map(res => res.data!));
    }

    // ── PATCH (void operations) ─────────────────────────────────────────────────

    toggle(id: number): Observable<string> {
        return this.http
            .patch<ApiResponse<void>>(`${this.base}/${id}/toggle`, {})
            .pipe(map(res => res.message));
    }
}