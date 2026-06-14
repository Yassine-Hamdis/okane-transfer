import { Injectable }      from '@angular/core';
import { HttpClient }      from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment }     from '../../../environments/environment';
import { ApiResponse }     from '../models/api-response.model';
import {
  CashRegisterResponse,
  CashOperationResponse,
  CloseRegisterRequest,
  DiscrepancyRequest,
} from '../models/cash.model';

@Injectable({ providedIn: 'root' })
export class CashService {

  private readonly base = `${environment.apiUrl}/agent/cash/my-register`;

  constructor(private http: HttpClient) {}

  // ── GET ─────────────────────────────────────────────────────────────────────

  getMyRegister(): Observable<CashRegisterResponse> {
    return this.http
      .get<ApiResponse<CashRegisterResponse>>(this.base)
      .pipe(map(res => res.data!));
  }

  getTodayOperations(): Observable<CashOperationResponse[]> {
    return this.http
      .get<ApiResponse<CashOperationResponse[]>>(`${this.base}/operations/today`)
      .pipe(map(res => res.data!));
  }

  getAllOperations(): Observable<CashOperationResponse[]> {
    return this.http
      .get<ApiResponse<CashOperationResponse[]>>(`${this.base}/operations`)
      .pipe(map(res => res.data!));
  }

  // ── POST (void operations) ──────────────────────────────────────────────────

  closeRegister(request: CloseRegisterRequest): Observable<string> {
    return this.http
      .post<ApiResponse<void>>(`${this.base}/close`, request)
      .pipe(map(res => res.message));
  }

  reportDiscrepancy(request: DiscrepancyRequest): Observable<string> {
    return this.http
      .post<ApiResponse<void>>(`${this.base}/discrepancy`, request)
      .pipe(map(res => res.message));
  }
}
