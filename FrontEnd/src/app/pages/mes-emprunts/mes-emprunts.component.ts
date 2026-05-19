import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { EmpruntService } from '../../services/emprunt.service';
import { AuthService } from '../../services/auth.service';
import { Emprunt, Reservation } from '../../models/emprunt.model';

@Component({
  selector: 'app-mes-emprunts',
  imports: [RouterLink],
  templateUrl: './mes-emprunts.component.html',
  styleUrl: './mes-emprunts.component.css',
})
export class MesEmpruntsComponent implements OnInit {
  private readonly empruntService = inject(EmpruntService);
  readonly auth = inject(AuthService);

  emprunts     = signal<Emprunt[]>([]);
  reservations = signal<Reservation[]>([]);
  positions    = signal<Map<number, number>>(new Map()); // empruntId → position
  isLoading    = signal(true);
  error        = signal<string | null>(null);
  actionError  = signal<string | null>(null);
  actionOk     = signal<string | null>(null);

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.isLoading.set(true);
    forkJoin({
      emprunts:     this.empruntService.getMesEmprunts().pipe(catchError(() => of([]))),
      reservations: this.empruntService.getMesReservations().pipe(catchError(() => of([]))),
      demandes:     this.empruntService.getDemandesEnAttente().pipe(catchError(() => of([]))),
    }).subscribe(({ emprunts, reservations, demandes }) => {
      this.emprunts.set(emprunts);
      this.reservations.set(reservations);
      this.isLoading.set(false);

      // Calculer la position de chaque demande de l'utilisateur dans la file
      const map = new Map<number, number>();
      const mesDemandeIds = new Set(
        emprunts.filter(e => e.statut.libelle === 'DEMANDE').map(e => e.id)
      );
      // Pour chaque livre avec une demande, trouver la position dans la file globale
      emprunts.filter(e => e.statut.libelle === 'DEMANDE').forEach(maD => {
        const fileGlobale = demandes
          .filter(d => d.livre.id === maD.livre.id)
          .sort((a, b) => new Date(a.dateEmprunt).getTime() - new Date(b.dateEmprunt).getTime());
        const pos = fileGlobale.findIndex(d => d.id === maD.id) + 1;
        if (pos > 0) map.set(maD.id, pos);
      });
      this.positions.set(map);
    });
  }

  getPosition(empruntId: number): number {
    return this.positions().get(empruntId) ?? 0;
  }

  rendre(empruntId: number): void {
    this.actionError.set(null); this.actionOk.set(null);
    this.empruntService.rendre(empruntId).subscribe({
      next: () => { this.actionOk.set('Livre rendu avec succès !'); this.charger(); },
      error: (e: any) => this.actionError.set(e.error ?? 'Erreur lors du retour.'),
    });
  }

  annulerReservation(reservationId: number): void {
    this.actionError.set(null); this.actionOk.set(null);
    this.empruntService.annulerReservation(reservationId).subscribe({
      next: () => { this.actionOk.set('Réservation annulée.'); this.charger(); },
      error: (e: any) => this.actionError.set(e.error ?? 'Erreur lors de l\'annulation.'),
    });
  }

  isEnRetard(emprunt: Emprunt): boolean {
    if (!emprunt.dateRetourPrevue || emprunt.dateRetour) return false;
    return new Date() > new Date(emprunt.dateRetourPrevue);
  }

  formatDate(iso: string | undefined): string {
    if (!iso) return '—';
    return new Date(iso).toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }
}
