import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { LivreService } from '../../services/book.service';
import { Livre, Categorie } from '../../models/book.model';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent implements OnInit, OnDestroy {
  private readonly livreService = inject(LivreService);
  private readonly destroy$ = new Subject<void>();

  featuredLivres = signal<Livre[]>([]);
  categories = signal<Categorie[]>([]);
  isLoading = signal(true);

  private readonly categoryIcons: Record<string, string> = {
    'Roman': '📖', 'Science-fiction': '🚀', 'Fantasy': '🧙', 'Policier': '🔍',
    'Biographie': '👤', 'Manga': '🎌', 'Histoire': '🏛️', 'Jeunesse': '🎈',
    'Développement personnel': '💡', 'Informatique': '💻',
  };

  getCatIcon(nom: string): string {
    return this.categoryIcons[nom] ?? '📚';
  }

  ngOnInit(): void {
    this.livreService.getAll().pipe(takeUntil(this.destroy$)).subscribe({
      next: livres => { this.featuredLivres.set(livres.slice(0, 6)); this.isLoading.set(false); },
      error: () => { this.isLoading.set(false); },
    });
    this.livreService.getCategories().pipe(takeUntil(this.destroy$))
      .subscribe(cats => this.categories.set(cats));
  }

  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  getStars(note: number | undefined): string[] {
    const n = note ?? 0;
    return Array.from({ length: 5 }, (_, i) => i < Math.floor(n) ? 'full' : i < n ? 'half' : 'empty');
  }
}