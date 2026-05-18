import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BookService } from '../../services/book.service';
import { EmpruntService } from '../../services/emprunt.service';
import { AuthService } from '../../services/auth.service';
import { Book } from '../../models/book.model';

@Component({
  selector: 'app-livre-detail',
  imports: [RouterLink],
  templateUrl: './livre-detail.component.html',
  styleUrl: './livre-detail.component.css',
})
export class LivreDetailComponent implements OnInit {
  private readonly route        = inject(ActivatedRoute);
  private readonly bookService  = inject(BookService);
  private readonly empruntService = inject(EmpruntService);
  readonly auth = inject(AuthService);

  livre      = signal<Book | null>(null);
  isLoading  = signal(true);
  error      = signal<string | null>(null);
  actionMsg  = signal<string | null>(null);
  actionErr  = signal<string | null>(null);
  isActing   = signal(false);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.bookService.getById(id).subscribe({
      next:  b  => { this.livre.set(b); this.isLoading.set(false); },
      error: () => { this.error.set('Livre introuvable.'); this.isLoading.set(false); },
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
      error: e => { this.actionErr.set(e.error ?? 'Erreur lors de l\'emprunt.'); this.isActing.set(false); },
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
      error: e => { this.actionErr.set(e.error ?? 'Erreur lors de la réservation.'); this.isActing.set(false); },
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
