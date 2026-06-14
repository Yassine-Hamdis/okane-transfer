import { Injectable }      from '@angular/core';
import { HttpClient }      from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment }     from '../../../environments/environment';
import { ApiResponse }     from '../models/api-response.model';
import {
  User,
  CreateUserRequest,
  UpdateUserRequest,
  ResetPasswordRequest,
  UserRole,
} from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {

  private readonly base = `${environment.apiUrl}/admin/users`;

  constructor(private http: HttpClient) {}

  // ── GET ─────────────────────────────────────────────────────────────────────

  getAll(): Observable<User[]> {
    return this.http
      .get<ApiResponse<User[]>>(this.base)
      .pipe(map(res => res.data!));
  }

  getById(id: number): Observable<User> {
    return this.http
      .get<ApiResponse<User>>(`${this.base}/${id}`)
      .pipe(map(res => res.data!));
  }

  getByAgency(agencyId: number): Observable<User[]> {
    return this.http
      .get<ApiResponse<User[]>>(`${this.base}/agency/${agencyId}`)
      .pipe(map(res => res.data!));
  }

  getByRole(role: UserRole): Observable<User[]> {
    return this.http
      .get<ApiResponse<User[]>>(`${this.base}/role/${role}`)
      .pipe(map(res => res.data!));
  }

  // ── CREATE / UPDATE ─────────────────────────────────────────────────────────

  create(request: CreateUserRequest): Observable<User> {
    return this.http
      .post<ApiResponse<User>>(this.base, request)
      .pipe(map(res => res.data!));
  }

  update(id: number, request: UpdateUserRequest): Observable<User> {
    return this.http
      .put<ApiResponse<User>>(`${this.base}/${id}`, request)
      .pipe(map(res => res.data!));
  }

  // ── PATCH (void operations) ─────────────────────────────────────────────────

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

  resetPassword(id: number, request: ResetPasswordRequest): Observable<string> {
    return this.http
      .patch<ApiResponse<void>>(`${this.base}/${id}/reset-password`, request)
      .pipe(map(res => res.message));
  }
}
