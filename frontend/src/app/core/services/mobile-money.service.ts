import { Injectable }      from '@angular/core';
import { HttpClient }      from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment }     from '../../../environments/environment';
import { ApiResponse }     from '../models/api-response.model';
import {
  MobileMoneyResponse,
  CreateMobileMoneyRequest,
} from '../models/mobile-money.model';

@Injectable({ providedIn: 'root' })
export class MobileMoneyService {

  private readonly agentBase = `${environment.apiUrl}/agent/mobile-money`;
  private readonly adminBase = `${environment.apiUrl}/admin/mobile-money`;

  constructor(private http: HttpClient) {}

  // ── AGENT endpoints ─────────────────────────────────────────────────────────

  create(request: CreateMobileMoneyRequest): Observable<MobileMoneyResponse> {
    return this.http
      .post<ApiResponse<MobileMoneyResponse>>(this.agentBase, request)
      .pipe(map(res => res.data!));
  }

  getByTransfer(transferId: number): Observable<MobileMoneyResponse> {
    return this.http
      .get<ApiResponse<MobileMoneyResponse>>(
        `${this.agentBase}/transfer/${transferId}`
      )
      .pipe(map(res => res.data!));
  }

  // ── ADMIN endpoints ─────────────────────────────────────────────────────────

  getPending(): Observable<MobileMoneyResponse[]> {
    return this.http
      .get<ApiResponse<MobileMoneyResponse[]>>(`${this.adminBase}/pending`)
      .pipe(map(res => res.data!));
  }

  getSent(): Observable<MobileMoneyResponse[]> {
    return this.http
      .get<ApiResponse<MobileMoneyResponse[]>>(`${this.adminBase}/sent`)
      .pipe(map(res => res.data!));
  }

  reconcile(id: number): Observable<string> {
    return this.http
      .patch<ApiResponse<void>>(`${this.adminBase}/${id}/reconcile`, {})
      .pipe(map(res => res.message));
  }
}
