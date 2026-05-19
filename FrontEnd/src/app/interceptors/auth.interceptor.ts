import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getToken();

  console.log(`[Interceptor] ${req.method} ${req.url}`);
  console.log(`[Interceptor] Token présent: ${!!token}`);
  if (token) {
    console.log(`[Interceptor] Token (20 premiers cars): ${token.substring(0, 20)}...`);
  }

  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError(err => {
      console.log(`[Interceptor] Erreur ${err.status} sur ${req.method} ${req.url}`);
      if (err.status === 401 && auth.isLoggedIn()) {
        auth.logout();
      }
      return throwError(() => err);
    })
  );
};
