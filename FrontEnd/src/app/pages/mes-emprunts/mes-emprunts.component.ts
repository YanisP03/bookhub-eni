import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
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
  isLoading    = signal(true);
  error        = signal<string | null>(null);
  actionError  = signal<string | null>(null);
  actionOk     = signal<string | null>(null);

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.isLoading.set(true);
    this.empruntService.getMesEmprunts().subscribe({
      next: e => { this.emprunts.set(e); this.isLoading.set(false); },
      error: () => { this.error.set('Impossible de charger vos emprunts.'); this.isLoading.set(false); },
    });
    this.empruntService.getMesReservations().subscribe({
      next: r => this.reservations.set(r),
    });
  }

  rendre(empruntId: number): void {
    this.actionError.set(null); this.actionOk.set(null);
    this.empruntService.rendre(empruntId).subscribe({
      next: () => { this.actionOk.set('Livre rendu avec succès !'); this.charger(); },
      error: (e) => this.actionError.set(e.error ?? 'Erreur lors du retour.'),
    });
  }

  annulerReservation(reservationId: number): void {
    this.actionError.set(null); this.actionOk.set(null);
    this.empruntService.annulerReservation(reservationId).subscribe({
      next: () => { this.actionOk.set('Réservation annulée.'); this.charger(); },
      error: (e) => this.actionError.set(e.error ?? 'Erreur lors de l\'annulation.'),
    });
  }

  isEnRetard(emprunt: Emprunt): boolean {
    return !emprunt.dateRetour && new Date() > new Date(emprunt.dateRetourPrevue);
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }
}
