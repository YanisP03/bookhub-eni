import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { CurrentUser, LoginRequest, LoginResponse } from '../models/auth.model';

const TOKEN_KEY = 'bh_token';
const USER_KEY  = 'bh_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http   = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiUrl = 'http://localhost:8080/api/auth';

  private readonly _user = signal<CurrentUser | null>(this.loadUser());

  readonly currentUser = this._user.asReadonly();
  readonly isLoggedIn  = computed(() => this._user() !== null);
  readonly isAdmin     = computed(() => this._user()?.role === 'ROLE_ADMIN');

  login(credentials: LoginRequest) {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(res => {
        localStorage.setItem(TOKEN_KEY, res.token);
        const user: CurrentUser = { email: res.email, role: res.role };
        localStorage.setItem(USER_KEY, JSON.stringify(user));
        this._user.set(user);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this._user.set(null);
    this.router.navigate(['/']);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  private loadUser(): CurrentUser | null {
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }
}
