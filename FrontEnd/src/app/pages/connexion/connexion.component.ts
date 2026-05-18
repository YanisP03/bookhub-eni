import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-connexion',
  imports: [FormsModule, RouterLink],
  templateUrl: './connexion.component.html',
  styleUrl: './connexion.component.css',
})
export class ConnexionComponent {
  private readonly auth   = inject(AuthService);
  private readonly router = inject(Router);

  mail       = '';
  motDePasse = '';
  isLoading  = signal(false);
  error      = signal<string | null>(null);

  onSubmit(): void {
    this.error.set(null);
    this.isLoading.set(true);
    this.auth.login({ mail: this.mail, motDePasse: this.motDePasse }).subscribe({
      next: () => this.router.navigate(['/mes-emprunts']),
      error: () => { this.error.set('Email ou mot de passe incorrect.'); this.isLoading.set(false); },
    });
  }
}
