import { Injectable }      from '@angular/core';
import { HttpClient }      from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment }     from '../../../environments/environment';
import { ApiResponse }     from '../models/api-response.model';
import {
  User,
  UpdateProfileRequest,
  ChangePasswordRequest,
} from '../models/user.model';
import {
  TransferSummary,
  Transfer,
  TransferTrack,
} from '../models/transfer.model';

@Injectable({ providedIn: 'root' })
export class ClientService {

  private readonly profileBase  = `${environment.apiUrl}/client/profile`;
  private readonly transferBase = `${environment.apiUrl}/client/transfers`;

  constructor(private http: HttpClient) {}

  // ── Profile ─────────────────────────────────────────────────────────────────

  getProfile(): Observable<User> {
    return this.http
      .get<ApiResponse<User>>(this.profileBase)
      .pipe(map(res => res.data!));
  }

  updateProfile(request: UpdateProfileRequest): Observable<User> {
    return this.http
      .put<ApiResponse<User>>(this.profileBase, request)
      .pipe(map(res => res.data!));
  }

  changePassword(request: ChangePasswordRequest): Observable<string> {
    return this.http
      .patch<ApiResponse<void>>(`${this.profileBase}/change-password`, request)
      .pipe(map(res => res.message));
  }

  toggleTwoFactor(): Observable<string> {
    return this.http
      .patch<ApiResponse<void>>(`${this.profileBase}/toggle-2fa`, {})
      .pipe(map(res => res.message));
  }

  // ── Transfers ────────────────────────────────────────────────────────────────

  getMyTransfers(): Observable<TransferSummary[]> {
    return this.http
      .get<ApiResponse<TransferSummary[]>>(this.transferBase)
      .pipe(map(res => res.data!));
  }

  getTransferById(id: number): Observable<Transfer> {
    return this.http
      .get<ApiResponse<Transfer>>(`${this.transferBase}/${id}`)
      .pipe(map(res => res.data!));
  }

  /**
   * Public tracking endpoint — works with or without a token.
   * The interceptor only attaches the header IF a token exists,
   * so anonymous users can call this too.
   */
  trackTransfer(code: string): Observable<TransferTrack> {
    return this.http
      .get<ApiResponse<TransferTrack>>(
        `${this.transferBase}/track/${code}`
      )
      .pipe(map(res => res.data!));
  }
}
