import { Injectable }      from '@angular/core';
import { HttpClient }      from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment }     from '../../../environments/environment';
import { ApiResponse }     from '../models/api-response.model';
import { AuditLog, AuditRangeParams } from '../models/audit.model';

@Injectable({ providedIn: 'root' })
export class AuditService {

  private readonly base = `${environment.apiUrl}/admin/audit`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<AuditLog[]> {
    return this.http
      .get<ApiResponse<AuditLog[]>>(this.base)
      .pipe(map(res => res.data!));
  }

  getByUser(userId: number): Observable<AuditLog[]> {
    return this.http
      .get<ApiResponse<AuditLog[]>>(`${this.base}/user/${userId}`)
      .pipe(map(res => res.data!));
  }

  getByEntity(entityType: string, entityId: number): Observable<AuditLog[]> {
    return this.http
      .get<ApiResponse<AuditLog[]>>(
        `${this.base}/entity/${entityType}/${entityId}`
      )
      .pipe(map(res => res.data!));
  }

  getByRange(params: AuditRangeParams): Observable<AuditLog[]> {
    return this.http
      .get<ApiResponse<AuditLog[]>>(`${this.base}/range`, {
        params: { from: params.from, to: params.to },
      })
      .pipe(map(res => res.data!));
  }

  getByAction(action: string): Observable<AuditLog[]> {
    return this.http
      .get<ApiResponse<AuditLog[]>>(`${this.base}/action/${action}`)
      .pipe(map(res => res.data!));
  }
}
