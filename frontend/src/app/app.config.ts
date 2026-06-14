import { ApplicationConfig }    from '@angular/core';
import { provideRouter }        from '@angular/router';
import { provideAnimations }    from '@angular/platform-browser/animations';
import {
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import { Chart, registerables } from 'chart.js';

import { routes }               from './app.routes';
import { authInterceptorFn }    from './core/interceptors/auth.interceptor';

// Register ALL Chart.js components globally
Chart.register(...registerables);

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimations(),
    provideHttpClient(withInterceptors([authInterceptorFn])),
  ],
};
