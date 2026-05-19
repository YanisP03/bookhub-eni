import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EmpruntService } from '../../services/emprunt.service';
import { AvisService, AvisDTO } from '../../services/avis.service';
import { AuthService } from '../../services/auth.service';
import { Emprunt, Reservation } from '../../models/emprunt.model';

@Component({
  selector: 'app-dashboard-lecteur',
  imports: [RouterLink],
  templateUrl: './dashboard-lecteur.component.html',
  styleUrl: './dashboard-lecteur.component.css',
})
export class DashboardLecteurComponent implements OnInit {
  private readonly empruntService = inject(EmpruntService);
  private readonly avisService    = inject(AvisService);
  readonly auth = inject(AuthService);

  emprunts     = signal<Emprunt[]>([]);
  reservations = signal<Reservation[]>([]);
  mesAvis      = signal<AvisDTO[]>([]);
  isLoading    = signal(true);

  readonly empruntsActifs    = computed(() => this.emprunts().filter(e => !e.dateRetour));
  readonly empruntsEnRetard  = computed(() => this.empruntsActifs().filter(e => !!e.dateRetourPrevue && new Date() > new Date(e.dateRetourPrevue)));
  readonly reservationsActives = computed(() => this.reservations().filter(r => r.statut.libelle === 'EN_ATTENTE' || r.statut.libelle === 'NOTIFIEE'));
  readonly historiqueCount   = computed(() => this.emprunts().filter(e => e.dateRetour).length);

  ngOnInit(): void {
    this.empruntService.getMesEmprunts().subscribe({
      next: e => { this.emprunts.set(e); this.isLoading.set(false); },
      error: () => this.isLoading.set(false),
    });
    this.empruntService.getMesReservations().subscribe({ next: r => this.reservations.set(r) });
    this.avisService.getMesAvis().subscribe({ next: a => this.mesAvis.set(a) });
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }
}
