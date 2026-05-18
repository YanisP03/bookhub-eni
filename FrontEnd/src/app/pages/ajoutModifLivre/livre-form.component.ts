import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BookService } from '../../services/book.service';
import { Book } from '../../models/book.model';

@Component({
  selector: 'app-livre-form',
  imports: [RouterLink, FormsModule],
  templateUrl: './livre-form.component.html',
  styleUrl: './livre-form.component.css',
})
export class LivreFormComponent implements OnInit {
  private readonly livreService = inject(BookService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  isEditMode = signal(false);
  isLoading = signal(false);
  isSaving = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

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

  readonly categories: { id: number; label: string }[] = [
    { id: 1, label: 'Roman' },
    { id: 2, label: 'Science-Fiction' },
    { id: 3, label: 'Fantasy' },
    { id: 4, label: 'Policier' },
    { id: 5, label: 'Biographie' },
    { id: 6, label: 'Manga' },
  ];

  readonly statuts: { id: number; label: string }[] = [
    { id: 1, label: 'Disponible' },
    { id: 2, label: 'Indisponible' },
    { id: 3, label: 'Archivé' },
  ];

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
      this.isLoading.set(true);
      this.livreService.getById(+id).subscribe({
        next: livre => {
          this.livre = livre;
          this.isLoading.set(false);
        },
        error: () => {
          this.errorMessage.set('Impossible de charger le livre.');
          this.isLoading.set(false);
        },
      });
    }
  }

  onSubmit(): void {
    this.isSaving.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const obs = this.isEditMode()
      ? this.livreService.update(this.livre.id, this.livre)
      : this.livreService.create(this.livre);

    obs.subscribe({
      next: () => {
        this.isSaving.set(false);
        this.successMessage.set(
          this.isEditMode() ? 'Livre modifié avec succès !' : 'Livre ajouté avec succès !'
        );
        setTimeout(() => this.router.navigate(['/catalogue']), 1500);
      },
      error: () => {
        this.isSaving.set(false);
        this.errorMessage.set('Une erreur est survenue. Veuillez réessayer.');
      },
    });
  }
}
