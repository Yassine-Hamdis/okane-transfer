import { inject }       from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService }  from '../services/auth.service';

/**
 * Protects routes that require any authenticated user.
 * Used for /client/** routes.
 * Redirects to /login if no token found in localStorage.
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router      = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  return router.createUrlTree(['/login']);
};
