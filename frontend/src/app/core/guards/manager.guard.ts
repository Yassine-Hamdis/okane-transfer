import { inject }              from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService }         from '../services/auth.service';

/**
 * Protects /manager/** routes.
 * ROLE_ADMIN and ROLE_MANAGER may enter.
 */
export const managerGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router      = inject(Router);

  if (!authService.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  if (authService.hasRole('ROLE_ADMIN', 'ROLE_MANAGER')) {
    return true;
  }

  authService.redirectByRole();
  return false;
};
