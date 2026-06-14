import { Injectable }      from '@angular/core';
import { HttpClient }      from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment }     from '../../../environments/environment';
import { ApiResponse }     from '../models/api-response.model';
import {
  Agency,
  CreateAgencyRequest,
  UpdateAgencyRequest,
  AssignManagerRequest,
} from '../models/agency.model';

@Injectable({ providedIn: 'root' })
export class AgencyService {

  private readonly base = `${environment.apiUrl}/admin/agencies`;

  constructor(private http: HttpClient) {}

  // ── GET ─────────────────────────────────────────────────────────────────────

  getAll(): Observable<Agency[]> {
    return this.http
      .get<ApiResponse<Agency[]>>(this.base)
      .pipe(map(res => res.data!));
  }

  getActive(): Observable<Agency[]> {
    return this.http
      .get<ApiResponse<Agency[]>>(`${this.base}/active`)
      .pipe(map(res => res.data!));
  }

  getById(id: number): Observable<Agency> {
    return this.http
      .get<ApiResponse<Agency>>(`${this.base}/${id}`)
      .pipe(map(res => res.data!));
  }

  getByCountry(countryId: number): Observable<Agency[]> {
    return this.http
      .get<ApiResponse<Agency[]>>(`${this.base}/country/${countryId}`)
      .pipe(map(res => res.data!));
  }

  // ── CREATE / UPDATE ─────────────────────────────────────────────────────────

  create(request: CreateAgencyRequest): Observable<Agency> {
    return this.http
      .post<ApiResponse<Agency>>(this.base, request)
      .pipe(map(res => res.data!));
  }

  update(id: number, request: UpdateAgencyRequest): Observable<Agency> {
    return this.http
      .put<ApiResponse<Agency>>(`${this.base}/${id}`, request)
      .pipe(map(res => res.data!));
  }

  // ── PATCH (void operations) ─────────────────────────────────────────────────

  assignManager(id: number, request: AssignManagerRequest): Observable<string> {
    return this.http
      .patch<ApiResponse<void>>(`${this.base}/${id}/assign-manager`, request)
      .pipe(map(res => res.message));
  }

  suspend(id: number): Observable<string> {
    return this.http
      .patch<ApiResponse<void>>(`${this.base}/${id}/suspend`, {})
      .pipe(map(res => res.message));
  }

  activate(id: number): Observable<string> {
    return this.http
      .patch<ApiResponse<void>>(`${this.base}/${id}/activate`, {})
      .pipe(map(res => res.message));
  }
}
