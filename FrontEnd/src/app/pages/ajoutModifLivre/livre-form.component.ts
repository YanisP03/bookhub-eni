import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { BookService } from '../../services/book.service';
import { Book, Categorie, Statut } from '../../models/book.model';

// Libellés des statuts valides pour un livre
const LIVRE_STATUTS = ['DISPONIBLE', 'EMPRUNTE'];

@Component({
  selector: 'app-livre-form',
  imports: [RouterLink, FormsModule],
  templateUrl: './livre-form.component.html',
  styleUrl: './livre-form.component.css',
})
export class LivreFormComponent implements OnInit {
  private readonly livreService = inject(BookService);
  private readonly route        = inject(ActivatedRoute);
  private readonly router       = inject(Router);

  isEditMode     = signal(false);
  isLoading      = signal(true);
  isSaving       = signal(false);
  isUploading    = signal(false);
  errorMessage   = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  categories = signal<Categorie[]>([]);
  statuts    = signal<Statut[]>([]);

  livre: Book = {
    id: 0,
    titre: '',
    auteur: '',
    isbn: '',
    description: '',
    couverture: '',
    datePublication: '',
    nbExemplaires: 1,
    nbDisponibles: 1,
    categorie: { id: undefined },
    statut: { id: undefined },
  };

  ngOnInit(): void {
    const livreId = this.route.snapshot.paramMap.get('id');

    // Chargement catégories + statuts en parallèle
    forkJoin({
      cats: this.livreService.getCategories(),
      stats: this.livreService.getStatuts(),
    }).subscribe({
      next: ({ cats, stats }) => {
        this.categories.set(cats);
        // Seuls DISPONIBLE et EMPRUNTE sont valides pour un livre
        this.statuts.set(stats.filter(s => s.libelle && LIVRE_STATUTS.includes(s.libelle)));

        if (livreId) {
          this.isEditMode.set(true);
          this.livreService.getById(+livreId).subscribe({
            next: livre => { this.livre = livre; this.isLoading.set(false); },
            error: () => { this.errorMessage.set('Impossible de charger le livre.'); this.isLoading.set(false); },
          });
        } else {
          this.isLoading.set(false);
        }
      },
      error: () => {
        this.errorMessage.set('Impossible de charger les données du formulaire.');
        this.isLoading.set(false);
      },
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    this.isUploading.set(true);
    this.errorMessage.set(null);
    this.livreService.uploadCouverture(input.files[0]).subscribe({
      next: res => { this.livre.couverture = res.url; this.isUploading.set(false); },
      error: (e: any) => { this.errorMessage.set(e.error?.error ?? 'Erreur lors du téléchargement.'); this.isUploading.set(false); },
    });
  }

  onSubmit(): void {
    this.isSaving.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const obs = this.isEditMode()
      ? this.livreService.update(this.livre.id, this.livre)
      : this.livreService.create(this.livre); // le backend force id=null pour l'INSERT

    obs.subscribe({
      next: () => {
        this.isSaving.set(false);
        this.successMessage.set(this.isEditMode() ? 'Livre modifié avec succès !' : 'Livre ajouté avec succès !');
        setTimeout(() => this.router.navigate(['/catalogue']), 1500);
      },
      error: (e: any) => {
        this.isSaving.set(false);
        this.errorMessage.set(e.error?.error ?? 'Une erreur est survenue. Veuillez réessayer.');
      },
    });
  }
}
