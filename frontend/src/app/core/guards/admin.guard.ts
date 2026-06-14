import { inject }              from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService }         from '../services/auth.service';

/**
 * Protects /admin/** routes.
 * Only ROLE_ADMIN may enter.
 * Redirects to /login if not authenticated, /login if wrong role.
 */
export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router      = inject(Router);

  if (!authService.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  if (authService.hasRole('ROLE_ADMIN')) {
    return true;
  }

  // Authenticated but wrong role → send to their own dashboard
  authService.redirectByRole();
  return false;
};
