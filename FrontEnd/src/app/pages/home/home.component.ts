import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { BookService } from '../../services/book.service';
import { AuthService } from '../../services/auth.service';
import { Book } from '../../models/book.model';

interface CategoryItem { key: string; label: string; icon: string; }

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent implements OnInit, OnDestroy {
  private readonly bookService = inject(BookService);
  readonly auth = inject(AuthService);
  private readonly destroy$ = new Subject<void>();

  featuredLivres  = signal<Book[]>([]);
  populaires      = signal<Book[]>([]);
  mieuxNotes      = signal<Book[]>([]);
  isLoading       = signal(true);

  readonly categories = signal<CategoryItem[]>([
    { key: 'Roman',          label: 'Roman',          icon: '📖' },
    { key: 'Science-Fiction',label: 'Science-Fiction', icon: '🚀' },
    { key: 'Fantasy',        label: 'Fantasy',         icon: '🧙' },
    { key: 'Policier',       label: 'Policier',        icon: '🔍' },
    { key: 'Biographie',     label: 'Biographie',      icon: '👤' },
    { key: 'Manga',          label: 'Manga',           icon: '🎌' },
    { key: 'Histoire',       label: 'Histoire',        icon: '🏛️' },
    { key: 'Jeunesse',       label: 'Jeunesse',        icon: '🎈' },
    { key: 'Sciences',       label: 'Sciences',        icon: '🔬' },
    { key: 'Autre',          label: 'Autre',           icon: '📚' },
  ]);

  ngOnInit(): void {
    this.bookService.getPopulaires(5).pipe(takeUntil(this.destroy$)).subscribe({
      next: livres => { this.populaires.set(livres); this.isLoading.set(false); },
      error: () => { this.isLoading.set(false); },
    });
    this.bookService.getMieuxNotes(4).pipe(takeUntil(this.destroy$)).subscribe({
      next: livres => this.mieuxNotes.set(livres),
    });
    this.bookService.getAll().pipe(takeUntil(this.destroy$)).subscribe({
      next: livres => this.featuredLivres.set(livres.slice(0, 6)),
    });
  }

  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  getStars(note: number | undefined): string[] {
    const n = note ?? 0;
    return Array.from({ length: 5 }, (_, i) => i < Math.floor(n) ? 'full' : i < n ? 'half' : 'empty');
  }
}
