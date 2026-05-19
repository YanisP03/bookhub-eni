import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getToken();

  if (token) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  return next(req).pipe(
    catchError(err => {
      // Token expiré ou invalide → déconnexion automatique
      if ((err.status === 401 || err.status === 403) && auth.isLoggedIn()) {
        auth.logout(); // efface localStorage + navigue vers /
      }
      return throwError(() => err);
    })
  );
};
