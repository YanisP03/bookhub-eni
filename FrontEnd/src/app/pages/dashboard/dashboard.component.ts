import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProfilService, DashboardStats } from '../../services/profil.service';
import { AvisService, AvisDTO } from '../../services/avis.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit {
  private readonly profilService = inject(ProfilService);
  private readonly avisService   = inject(AvisService);
  readonly auth = inject(AuthService);

  stats        = signal<DashboardStats | null>(null);
  avisEnAttente = signal<AvisDTO[]>([]);
  isLoading    = signal(true);
  error        = signal<string | null>(null);
  moderMsg     = signal<string | null>(null);

  ngOnInit(): void {
    this.profilService.getDashboardStats().subscribe({
      next:  s => { this.stats.set(s); this.isLoading.set(false); },
      error: () => { this.error.set('Impossible de charger les statistiques.'); this.isLoading.set(false); },
    });
    this.loadModeration();
  }

  loadModeration(): void {
    this.avisService.getAvisEnAttente().subscribe({
      next: list => this.avisEnAttente.set(list),
    });
  }

  approuver(id: number): void {
    this.avisService.approuver(id).subscribe({
      next: () => { this.moderMsg.set('✅ Avis approuvé.'); this.loadModeration(); setTimeout(() => this.moderMsg.set(null), 3000); },
    });
  }

  rejeter(id: number): void {
    this.avisService.rejeter(id).subscribe({
      next: () => { this.moderMsg.set('🗑 Avis rejeté.'); this.loadModeration(); setTimeout(() => this.moderMsg.set(null), 3000); },
    });
  }

  getStars(n: number): string[] {
    return Array.from({ length: 5 }, (_, i) => i < n ? 'full' : 'empty');
  }
}
