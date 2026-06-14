import { Injectable }      from '@angular/core';
import { HttpClient }      from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment }     from '../../../environments/environment';
import { ApiResponse }     from '../models/api-response.model';
import {
  Transfer,
  TransferStatus,
  CreateTransferRequest,
  PayoutRequest,
  CancelTransferRequest,
} from '../models/transfer.model';

@Injectable({ providedIn: 'root' })
export class TransferService {

  private readonly agentBase = `${environment.apiUrl}/agent/transfers`;
  private readonly adminBase = `${environment.apiUrl}/admin/transfers`;

  constructor(private http: HttpClient) {}

  // ── AGENT endpoints ─────────────────────────────────────────────────────────

  create(request: CreateTransferRequest): Observable<Transfer> {
    return this.http
      .post<ApiResponse<Transfer>>(this.agentBase, request)
      .pipe(map(res => res.data!));
  }

  payout(request: PayoutRequest): Observable<Transfer> {
    return this.http
      .post<ApiResponse<Transfer>>(`${this.agentBase}/payout`, request)
      .pipe(map(res => res.data!));
  }

  getById(id: number): Observable<Transfer> {
    return this.http
      .get<ApiResponse<Transfer>>(`${this.agentBase}/${id}`)
      .pipe(map(res => res.data!));
  }

  getByCode(code: string): Observable<Transfer> {
    return this.http
      .get<ApiResponse<Transfer>>(`${this.agentBase}/code/${code}`)
      .pipe(map(res => res.data!));
  }

  getMyTransfers(): Observable<Transfer[]> {
    return this.http
      .get<ApiResponse<Transfer[]>>(`${this.agentBase}/my`)
      .pipe(map(res => res.data!));
  }

  searchByPhone(phone: string): Observable<Transfer[]> {
    return this.http
      .get<ApiResponse<Transfer[]>>(`${this.agentBase}/search`, {
        params: { phone },
      })
      .pipe(map(res => res.data!));
  }

  cancel(id: number, request: CancelTransferRequest): Observable<string> {
    return this.http
      .patch<ApiResponse<void>>(`${this.agentBase}/${id}/cancel`, request)
      .pipe(map(res => res.message));
  }

  // ── ADMIN endpoints ─────────────────────────────────────────────────────────

  getAllAdmin(): Observable<Transfer[]> {
    return this.http
      .get<ApiResponse<Transfer[]>>(this.adminBase)
      .pipe(map(res => res.data!));
  }

  getByStatus(status: TransferStatus): Observable<Transfer[]> {
    return this.http
      .get<ApiResponse<Transfer[]>>(`${this.adminBase}/status/${status}`)
      .pipe(map(res => res.data!));
  }

  approve(id: number): Observable<string> {
    return this.http
      .patch<ApiResponse<void>>(`${this.adminBase}/${id}/approve`, {})
      .pipe(map(res => res.message));
  }
}
