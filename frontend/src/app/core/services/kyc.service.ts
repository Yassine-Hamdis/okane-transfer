import { Injectable }      from '@angular/core';
import { HttpClient }      from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment }     from '../../../environments/environment';
import { ApiResponse }     from '../models/api-response.model';
import {
  KycRecord,
  KycReviewRequest,
} from '../models/kyc.model';

@Injectable({ providedIn: 'root' })
export class KycService {

  private readonly base = `${environment.apiUrl}/admin/kyc`;

  constructor(private http: HttpClient) {}

  // ── GET ─────────────────────────────────────────────────────────────────────

  getFlagged(): Observable<KycRecord[]> {
    return this.http
      .get<ApiResponse<KycRecord[]>>(`${this.base}/flagged`)
      .pipe(map(res => res.data!));
  }

  getBlocked(): Observable<KycRecord[]> {
    return this.http
      .get<ApiResponse<KycRecord[]>>(`${this.base}/blocked`)
      .pipe(map(res => res.data!));
  }

  getWatchlistHits(): Observable<KycRecord[]> {
    return this.http
      .get<ApiResponse<KycRecord[]>>(`${this.base}/watchlist-hits`)
      .pipe(map(res => res.data!));
  }

  getSuspicion(): Observable<KycRecord[]> {
    return this.http
      .get<ApiResponse<KycRecord[]>>(`${this.base}/suspicion`)
      .pipe(map(res => res.data!));
  }

  getByTransfer(transferId: number): Observable<KycRecord> {
    return this.http
      .get<ApiResponse<KycRecord>>(`${this.base}/transfer/${transferId}`)
      .pipe(map(res => res.data!));
  }

  // ── PATCH ───────────────────────────────────────────────────────────────────

  review(id: number, request: KycReviewRequest): Observable<string> {
    return this.http
      .patch<ApiResponse<void>>(`${this.base}/${id}/review`, request)
      .pipe(map(res => res.message));
  }
}
