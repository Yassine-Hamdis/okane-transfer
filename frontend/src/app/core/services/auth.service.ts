import { Injectable }        from '@angular/core';
import { HttpClient }        from '@angular/common/http';
import { Router }            from '@angular/router';
import { Observable, map }   from 'rxjs';

import { environment }       from '../../../environments/environment';
import { ApiResponse }       from '../models/api-response.model';
import {
  LoginRequest,
  LoginResponse,
  TwoFactorRequest,
  RegisterRequest,
  UserRole,
} from '../models/user.model';

// ── localStorage keys (single source of truth) ────────────────────────────────
export const STORAGE_KEYS = {
  TOKEN:     'okane_token',
  ROLE:      'okane_role',
  USER_ID:   'okane_user_id',
  FULL_NAME: 'okane_full_name',
} as const;

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly base = `${environment.apiUrl}/auth`;

  constructor(
    private http:   HttpClient,
    private router: Router,
  ) {}

  // ── API calls ───────────────────────────────────────────────────────────────

  /**
   * Step 1 of login flow.
   * Returns LoginResponse — caller checks requiresTwoFactor / mustChangePassword.
   */
  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<ApiResponse<LoginResponse>>(`${this.base}/login`, request)
      .pipe(map(res => res.data!));
  }

  /**
   * Step 2 of login flow — only called when requiresTwoFactor === true.
   */
  verifyTwoFactor(request: TwoFactorRequest): Observable<LoginResponse> {
    return this.http
      .post<ApiResponse<LoginResponse>>(`${this.base}/verify-2fa`, request)
      .pipe(map(res => res.data!));
  }

  /**
   * Public registration — returns the success message string.
   */
  register(request: RegisterRequest): Observable<string> {
    return this.http
      .post<ApiResponse<void>>(`${this.base}/register`, request)
      .pipe(map(res => res.message));
  }

  // ── Session helpers ─────────────────────────────────────────────────────────

  /**
   * Persist LoginResponse into localStorage so the session
   * survives a page refresh.
   */
  saveSession(response: LoginResponse): void {
    if (response.accessToken) {
      localStorage.setItem(STORAGE_KEYS.TOKEN, response.accessToken);
    }
    localStorage.setItem(STORAGE_KEYS.ROLE,      response.role);
    localStorage.setItem(STORAGE_KEYS.USER_ID,   String(response.userId));
    localStorage.setItem(STORAGE_KEYS.FULL_NAME, response.fullName);
  }

  /**
   * Clear session and navigate to login.
   * Called by the interceptor on 401, or by the user clicking logout.
   */
  logout(): void {
    localStorage.removeItem(STORAGE_KEYS.TOKEN);
    localStorage.removeItem(STORAGE_KEYS.ROLE);
    localStorage.removeItem(STORAGE_KEYS.USER_ID);
    localStorage.removeItem(STORAGE_KEYS.FULL_NAME);
    this.router.navigate(['/login']);
  }

  // ── Token / role getters (reads from localStorage → survives reload) ─────────

  getToken(): string | null {
    return localStorage.getItem(STORAGE_KEYS.TOKEN);
  }

  getRole(): UserRole | null {
    return localStorage.getItem(STORAGE_KEYS.ROLE) as UserRole | null;
  }

  getUserId(): number | null {
    const id = localStorage.getItem(STORAGE_KEYS.USER_ID);
    return id ? parseInt(id, 10) : null;
  }

  getFullName(): string | null {
    return localStorage.getItem(STORAGE_KEYS.FULL_NAME);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  hasRole(...roles: UserRole[]): boolean {
    const current = this.getRole();
    return current !== null && roles.includes(current);
  }

  // ── Post-login navigation ───────────────────────────────────────────────────

  /**
   * Called after a successful login (token stored).
   * Redirects the user to their role-specific dashboard.
   */
  redirectByRole(): void {
    const role = this.getRole();
    switch (role) {
      case 'ROLE_ADMIN':   this.router.navigate(['/admin/dashboard']);   break;
      case 'ROLE_MANAGER': this.router.navigate(['/manager/dashboard']); break;
      case 'ROLE_AGENT':   this.router.navigate(['/agent/dashboard']);   break;
      case 'ROLE_CLIENT':  this.router.navigate(['/client/dashboard']);  break;
      default:             this.router.navigate(['/login']);
    }
  }

  /**
   * Full login flow orchestration.
   * Call this from the LoginComponent after receiving LoginResponse.
   *
   * Returns:
   *   'two-factor'       → show OTP screen
   *   'change-password'  → force password change
   *   'dashboard'        → session saved, navigated
   */
  handleLoginResponse(response: LoginResponse): 'two-factor' | 'change-password' | 'dashboard' {
    if (response.requiresTwoFactor) {
      // Don't save session yet — no token available
      return 'two-factor';
    }

    this.saveSession(response);

    if (response.mustChangePassword) {
      return 'change-password';
    }

    this.redirectByRole();
    return 'dashboard';
  }
}
