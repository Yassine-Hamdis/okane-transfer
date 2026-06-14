import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject }                               from '@angular/core';
import { Router }                               from '@angular/router';
import { catchError, throwError }               from 'rxjs';
import { AuthService }                          from '../services/auth.service';

export const authInterceptorFn: HttpInterceptorFn = (req, next) => {

  const authService = inject(AuthService);
  const router      = inject(Router);
  const token       = authService.getToken();

  // ── 1. Clone the request and attach the Bearer token (if present) ──────────
  const authReq = token
    ? req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    })
    : req;

  // ── 2. Forward and intercept error responses ───────────────────────────────
  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {

      if (error.status === 401) {
        // Token expired or invalid → wipe session and redirect
        authService.logout();
      }

      // Re-throw so individual services / components can also handle errors
      // error.error → ApiErrorResponse shape:
      // { success: false, message: string, data?: Record<string,string> }
      return throwError(() => error);
    }),
  );
};
