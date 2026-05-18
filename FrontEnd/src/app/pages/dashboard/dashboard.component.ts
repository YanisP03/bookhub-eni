import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProfilService, DashboardStats } from '../../services/profil.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit {
  private readonly profilService = inject(ProfilService);
  readonly auth = inject(AuthService);

  stats     = signal<DashboardStats | null>(null);
  isLoading = signal(true);
  error     = signal<string | null>(null);

  ngOnInit(): void {
    this.profilService.getDashboardStats().subscribe({
      next:  s => { this.stats.set(s); this.isLoading.set(false); },
      error: () => { this.error.set('Impossible de charger les statistiques.'); this.isLoading.set(false); },
    });
  }
}
