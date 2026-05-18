import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BookService } from '../../services/book.service';
import { EmpruntService } from '../../services/emprunt.service';
import { AuthService } from '../../services/auth.service';
import { AvisService, AvisDTO } from '../../services/avis.service';
import { Book } from '../../models/book.model';

@Component({
  selector: 'app-livre-detail',
  imports: [RouterLink, FormsModule],
  templateUrl: './livre-detail.component.html',
  styleUrl: './livre-detail.component.css',
})
export class LivreDetailComponent implements OnInit {
  private readonly route          = inject(ActivatedRoute);
  private readonly router         = inject(Router);
  private readonly bookService    = inject(BookService);
  private readonly empruntService = inject(EmpruntService);
  private readonly avisService    = inject(AvisService);
  readonly auth = inject(AuthService);

  livre         = signal<Book | null>(null);
  isLoading     = signal(true);
  error         = signal<string | null>(null);
  actionMsg     = signal<string | null>(null);
  actionErr     = signal<string | null>(null);
  isActing      = signal(false);
  confirmDelete = signal(false);

  // Avis
  avisList      = signal<AvisDTO[]>([]);
  newNote       = signal(0);
  newCommentaire = '';
  avisMsg       = signal<string | null>(null);
  avisErr       = signal<string | null>(null);
  isSavingAvis  = signal(false);
  showAvisForm  = signal(false);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.bookService.getById(id).subscribe({
      next:  b  => { this.livre.set(b); this.isLoading.set(false); this.loadAvis(id); },
      error: () => { this.error.set('Livre introuvable.'); this.isLoading.set(false); },
    });
  }

  loadAvis(livreId: number): void {
    this.avisService.getAvisLivre(livreId).subscribe({
      next: list => this.avisList.set(list),
    });
  }

  emprunter(): void {
    const id = this.livre()?.id;
    if (!id) return;
    this.isActing.set(true); this.actionMsg.set(null); this.actionErr.set(null);
    this.empruntService.emprunter(id).subscribe({
      next: () => {
        this.actionMsg.set('Emprunt enregistré ! Retour prévu dans 14 jours.');
        this.bookService.getById(id).subscribe(b => this.livre.set(b));
        this.isActing.set(false);
      },
      error: (e: any) => { this.actionErr.set(e.error ?? 'Erreur lors de l\'emprunt.'); this.isActing.set(false); },
    });
  }

  reserver(): void {
    const id = this.livre()?.id;
    if (!id) return;
    this.isActing.set(true); this.actionMsg.set(null); this.actionErr.set(null);
    this.empruntService.reserver(id).subscribe({
      next: r => {
        this.actionMsg.set(`Réservation enregistrée — vous êtes n°${r.positionFileAttente} dans la file.`);
        this.isActing.set(false);
      },
      error: (e: any) => { this.actionErr.set(e.error ?? 'Erreur lors de la réservation.'); this.isActing.set(false); },
    });
  }

  supprimerLivre(): void {
    const id = this.livre()?.id;
    if (!id) return;
    this.bookService.delete(id).subscribe({
      next: () => this.router.navigate(['/catalogue']),
      error: (e: any) => this.actionErr.set(e.error ?? 'Erreur lors de la suppression.'),
    });
  }

  setNote(n: number): void { this.newNote.set(n); }

  soumettreAvis(): void {
    const livreId = this.livre()?.id;
    if (!livreId || this.newNote() === 0) return;
    this.isSavingAvis.set(true); this.avisMsg.set(null); this.avisErr.set(null);
    this.avisService.posterAvis(livreId, this.newNote(), this.newCommentaire).subscribe({
      next: () => {
        this.avisMsg.set('Merci ! Votre avis est en attente de modération.');
        this.newNote.set(0); this.newCommentaire = '';
        this.showAvisForm.set(false);
        this.isSavingAvis.set(false);
      },
      error: (e: any) => {
        this.avisErr.set(e.error?.error ?? 'Erreur lors de l\'envoi.');
        this.isSavingAvis.set(false);
      },
    });
  }

  getStars(note: number | undefined): string[] {
    const n = note ?? 0;
    return Array.from({ length: 5 }, (_, i) => i < Math.floor(n) ? 'full' : i < n ? 'half' : 'empty');
  }

  formatDate(iso: string | undefined): string {
    if (!iso) return '—';
    return new Date(iso).toLocaleDateString('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' });
  }
}
