import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-inscription',
  imports: [FormsModule, RouterLink],
  templateUrl: './inscription.component.html',
  styleUrl: './inscription.component.css',
})
export class InscriptionComponent {
  private readonly http   = inject(HttpClient);
  private readonly router = inject(Router);

  nom        = '';
  prenom     = '';
  mail       = '';
  motDePasse = '';
  isLoading  = signal(false);
  error      = signal<string | null>(null);
  success    = signal(false);

  onSubmit(): void {
    this.error.set(null);
    this.isLoading.set(true);
    this.http.post('http://localhost:8080/api/auth/register', {
      nom: this.nom,
      prenom: this.prenom,
      mail: this.mail,
      motDePasse: this.motDePasse,
    }).subscribe({
      next: () => {
        this.success.set(true);
        this.isLoading.set(false);
        setTimeout(() => this.router.navigate(['/connexion']), 2000);
      },
      error: (e) => {
        this.error.set(e.error?.error ?? 'Une erreur est survenue.');
        this.isLoading.set(false);
      },
    });
  }
}
