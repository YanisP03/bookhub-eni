import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ProfilService, UtilisateurProfil } from '../../services/profil.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-profil',
  imports: [FormsModule],
  templateUrl: './profil.component.html',
  styleUrl: './profil.component.css',
})
export class ProfilComponent implements OnInit {
  private readonly profilService = inject(ProfilService);
  readonly auth = inject(AuthService);

  profil      = signal<UtilisateurProfil | null>(null);
  isLoading   = signal(true);
  isSaving    = signal(false);
  successMsg  = signal<string | null>(null);
  errorMsg    = signal<string | null>(null);

  // Formulaire profil
  nom        = '';
  prenom     = '';
  telephone  = '';

  // Formulaire mot de passe
  ancienMdp  = '';
  nouveauMdp = '';
  showMdpForm = signal(false);
  mdpSuccess  = signal<string | null>(null);
  mdpError    = signal<string | null>(null);
  isMdpSaving = signal(false);

  // Suppression compte
  showDeleteConfirm = signal(false);
  deleteMdp  = '';
  isDeleting = signal(false);
  deleteError = signal<string | null>(null);

  ngOnInit(): void {
    this.profilService.getProfil().subscribe({
      next: p => {
        this.profil.set(p);
        this.nom = p.nom;
        this.prenom = p.prenom;
        this.telephone = p.telephone ?? '';
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  saveProfil(): void {
    this.isSaving.set(true); this.successMsg.set(null); this.errorMsg.set(null);
    this.profilService.updateProfil({ nom: this.nom, prenom: this.prenom, telephone: this.telephone }).subscribe({
      next: p => { this.profil.set(p); this.successMsg.set('Profil mis à jour !'); this.isSaving.set(false); },
      error: () => { this.errorMsg.set('Erreur lors de la mise à jour.'); this.isSaving.set(false); },
    });
  }

  saveMdp(): void {
    this.isMdpSaving.set(true); this.mdpSuccess.set(null); this.mdpError.set(null);
    this.profilService.changerMotDePasse(this.ancienMdp, this.nouveauMdp).subscribe({
      next: () => {
        this.mdpSuccess.set('Mot de passe modifié avec succès !');
        this.ancienMdp = ''; this.nouveauMdp = '';
        this.isMdpSaving.set(false);
      },
      error: (e) => { this.mdpError.set(e.error?.error ?? 'Erreur.'); this.isMdpSaving.set(false); },
    });
  }

  deleteCompte(): void {
    this.isDeleting.set(true); this.deleteError.set(null);
    this.profilService.supprimerCompte(this.deleteMdp).subscribe({
      next: () => this.auth.logout(),
      error: (e) => { this.deleteError.set(e.error?.error ?? 'Mot de passe incorrect.'); this.isDeleting.set(false); },
    });
  }
}
