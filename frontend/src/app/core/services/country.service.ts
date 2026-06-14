import { Injectable }      from '@angular/core';
import { HttpClient }      from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment }     from '../../../environments/environment';
import { ApiResponse }     from '../models/api-response.model';
import {
    Country,
    CreateCountryRequest,
    UpdateCountryRequest,
} from '../models/country.model';

@Injectable({ providedIn: 'root' })
export class CountryService {

    private readonly base = `${environment.apiUrl}/admin/countries`;

    constructor(private http: HttpClient) {}

    // ── GET ─────────────────────────────────────────────────────────────────────

    getAll(): Observable<Country[]> {
        return this.http
            .get<ApiResponse<Country[]>>(this.base)
            .pipe(map(res => res.data!));
    }

    getActive(): Observable<Country[]> {
        return this.http
            .get<ApiResponse<Country[]>>(`${this.base}/active`)
            .pipe(map(res => res.data!));
    }

    getSending(): Observable<Country[]> {
        return this.http
            .get<ApiResponse<Country[]>>(`${this.base}/sending`)
            .pipe(map(res => res.data!));
    }

    getReceiving(): Observable<Country[]> {
        return this.http
            .get<ApiResponse<Country[]>>(`${this.base}/receiving`)
            .pipe(map(res => res.data!));
    }

    getById(id: number): Observable<Country> {
        return this.http
            .get<ApiResponse<Country>>(`${this.base}/${id}`)
            .pipe(map(res => res.data!));
    }

    // ── CREATE / UPDATE ─────────────────────────────────────────────────────────

    create(request: CreateCountryRequest): Observable<Country> {
        return this.http
            .post<ApiResponse<Country>>(this.base, request)
            .pipe(map(res => res.data!));
    }

    update(id: number, request: UpdateCountryRequest): Observable<Country> {
        return this.http
            .put<ApiResponse<Country>>(`${this.base}/${id}`, request)
            .pipe(map(res => res.data!));
    }

    // ── PATCH (void operations) ─────────────────────────────────────────────────

    toggle(id: number): Observable<string> {
        return this.http
            .patch<ApiResponse<void>>(`${this.base}/${id}/toggle`, {})
            .pipe(map(res => res.message));
    }
}