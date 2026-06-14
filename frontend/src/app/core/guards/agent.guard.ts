import { inject }              from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService }         from '../services/auth.service';

/**
 * Protects /agent/** routes.
 * ROLE_ADMIN, ROLE_MANAGER, and ROLE_AGENT may enter.
 */
export const agentGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router      = inject(Router);

  if (!authService.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  if (authService.hasRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_AGENT')) {
    return true;
  }

  authService.redirectByRole();
  return false;
};
