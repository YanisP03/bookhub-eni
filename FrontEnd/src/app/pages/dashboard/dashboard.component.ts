import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProfilService, DashboardStats } from '../../services/profil.service';
import { AvisService, AvisDTO } from '../../services/avis.service';
import { EmpruntService } from '../../services/emprunt.service';
import { AuthService } from '../../services/auth.service';
import { Emprunt, Reservation } from '../../models/emprunt.model';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit {
  private readonly profilService  = inject(ProfilService);
  private readonly avisService    = inject(AvisService);
  private readonly empruntService = inject(EmpruntService);
  readonly auth = inject(AuthService);

  stats            = signal<DashboardStats | null>(null);
  avisEnAttente    = signal<AvisDTO[]>([]);
  demandesEmprunt  = signal<Emprunt[]>([]);
  fileAttente      = signal<Reservation[]>([]);
  isLoading        = signal(true);
  error            = signal<string | null>(null);
  moderMsg         = signal<string | null>(null);
  empruntMsg       = signal<string | null>(null);
  fileAttenteMsg   = signal<string | null>(null);

  // Formulaire création bibliothécaire
  biblio = { nom: '', prenom: '', mail: '', motDePasse: '' };
  biblioMsg  = signal<string | null>(null);
  biblioErr  = signal<string | null>(null);
  biblioOpen = signal(false);

  // Promotion d'un compte existant
  promouvoirMail = '';
  promouvoirMsg  = signal<string | null>(null);
  promouvoirErr  = signal<string | null>(null);

  ngOnInit(): void {
    this.profilService.getDashboardStats().subscribe({
      next:  s => { this.stats.set(s); this.isLoading.set(false); },
      error: () => { this.error.set('Impossible de charger les statistiques.'); this.isLoading.set(false); },
    });
    this.loadModeration();
    this.loadDemandes();
    this.loadFileAttente();
  }

  loadModeration(): void {
    this.avisService.getAvisEnAttente().subscribe({ next: list => this.avisEnAttente.set(list) });
  }

  loadDemandes(): void {
    this.empruntService.getDemandesEnAttente().subscribe({ next: list => this.demandesEmprunt.set(list) });
  }

  loadFileAttente(): void {
    this.empruntService.getFileAttente().subscribe({ next: list => this.fileAttente.set(list) });
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

  validerEmprunt(id: number): void {
    this.empruntService.validerEmprunt(id).subscribe({
      next: () => { this.empruntMsg.set('✅ Emprunt validé.'); this.loadDemandes(); setTimeout(() => this.empruntMsg.set(null), 3000); },
      error: (e: any) => { this.empruntMsg.set('⚠️ ' + (e.error ?? 'Erreur.')); setTimeout(() => this.empruntMsg.set(null), 4000); },
    });
  }

  refuserEmprunt(id: number): void {
    this.empruntService.refuserEmprunt(id).subscribe({
      next: () => { this.empruntMsg.set('🗑 Demande refusée.'); this.loadDemandes(); setTimeout(() => this.empruntMsg.set(null), 3000); },
      error: (e: any) => { this.empruntMsg.set('⚠️ ' + (e.error ?? 'Erreur.')); setTimeout(() => this.empruntMsg.set(null), 4000); },
    });
  }

  promouvoir(): void {
    this.promouvoirMsg.set(null); this.promouvoirErr.set(null);
    this.profilService.promouvoirBibliothecaire(this.promouvoirMail).subscribe({
      next: (u: any) => {
        this.promouvoirMsg.set(`✅ ${u.prenom} ${u.nom} est maintenant bibliothécaire.`);
        this.promouvoirMail = '';
        setTimeout(() => this.promouvoirMsg.set(null), 4000);
      },
      error: (e: any) => this.promouvoirErr.set(e.error?.error ?? 'Utilisateur introuvable ou erreur serveur.'),
    });
  }

  ajouterBibliothecaire(): void {
    this.biblioMsg.set(null); this.biblioErr.set(null);
    this.profilService.creerBibliothecaire(this.biblio).subscribe({
      next: () => {
        this.biblioMsg.set(`✅ Bibliothécaire ${this.biblio.prenom} ${this.biblio.nom} créé.`);
        this.biblio = { nom: '', prenom: '', mail: '', motDePasse: '' };
        this.biblioOpen.set(false);
        setTimeout(() => this.biblioMsg.set(null), 4000);
      },
      error: (e: any) => this.biblioErr.set(e.error?.error ?? 'Erreur lors de la création.'),
    });
  }

  getStars(n: number): string[] {
    return Array.from({ length: 5 }, (_, i) => i < n ? 'full' : 'empty');
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }
}
