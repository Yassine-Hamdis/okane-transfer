import { Injectable }      from '@angular/core';
import { HttpClient }      from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment }     from '../../../environments/environment';
import { ApiResponse }     from '../models/api-response.model';
import {
    Corridor,
    CreateCorridorRequest,
} from '../models/corridor.model';

@Injectable({ providedIn: 'root' })
export class CorridorService {

    private readonly base = `${environment.apiUrl}/admin/corridors`;

    constructor(private http: HttpClient) {}

    // ── GET ─────────────────────────────────────────────────────────────────────

    getAll(): Observable<Corridor[]> {
        return this.http
            .get<ApiResponse<Corridor[]>>(this.base)
            .pipe(map(res => res.data!));
    }

    getActive(): Observable<Corridor[]> {
        return this.http
            .get<ApiResponse<Corridor[]>>(`${this.base}/active`)
            .pipe(map(res => res.data!));
    }

    getById(id: number): Observable<Corridor> {
        return this.http
            .get<ApiResponse<Corridor>>(`${this.base}/${id}`)
            .pipe(map(res => res.data!));
    }

    getBySourceCountry(countryId: number): Observable<Corridor[]> {
        return this.http
            .get<ApiResponse<Corridor[]>>(`${this.base}/source/${countryId}`)
            .pipe(map(res => res.data!));
    }

    getByDestinationCountry(countryId: number): Observable<Corridor[]> {
        return this.http
            .get<ApiResponse<Corridor[]>>(`${this.base}/destination/${countryId}`)
            .pipe(map(res => res.data!));
    }

    // ── CREATE ──────────────────────────────────────────────────────────────────

    create(request: CreateCorridorRequest): Observable<Corridor> {
        return this.http
            .post<ApiResponse<Corridor>>(this.base, request)
            .pipe(map(res => res.data!));
    }

    // ── PATCH (void operations) ─────────────────────────────────────────────────

    toggle(id: number): Observable<string> {
        return this.http
            .patch<ApiResponse<void>>(`${this.base}/${id}/toggle`, {})
            .pipe(map(res => res.message));
    }
}