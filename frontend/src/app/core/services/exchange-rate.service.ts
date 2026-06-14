import { Injectable }      from '@angular/core';
import { HttpClient }      from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment }     from '../../../environments/environment';
import { ApiResponse }     from '../models/api-response.model';
import {
    ExchangeRate,
    CreateExchangeRateRequest,
} from '../models/exchange-rate.model';

@Injectable({ providedIn: 'root' })
export class ExchangeRateService {

    private readonly base = `${environment.apiUrl}/admin/exchange-rates`;

    constructor(private http: HttpClient) {}

    // ── GET ─────────────────────────────────────────────────────────────────────

    getCurrent(corridorId: number): Observable<ExchangeRate> {
        return this.http
            .get<ApiResponse<ExchangeRate>>(`${this.base}/${corridorId}/current`)
            .pipe(map(res => res.data!));
    }

    getHistory(corridorId: number): Observable<ExchangeRate[]> {
        return this.http
            .get<ApiResponse<ExchangeRate[]>>(`${this.base}/${corridorId}/history`)
            .pipe(map(res => res.data!));
    }

    // ── CREATE ──────────────────────────────────────────────────────────────────

    create(corridorId: number, request: CreateExchangeRateRequest): Observable<ExchangeRate> {
        return this.http
            .post<ApiResponse<ExchangeRate>>(`${this.base}/${corridorId}`, request)
            .pipe(map(res => res.data!));
    }
}