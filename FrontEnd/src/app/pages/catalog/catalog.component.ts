import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { BookService } from '../../services/book.service';
import { AuthService } from '../../services/auth.service';
import { Book, CATEGORY_LABELS } from '../../models/book.model';

type SortOption = 'title' | 'author' | 'rating' | 'availability' | 'date';
type FilterAvailability = 'all' | 'available';

@Component({
  selector: 'app-catalog',
  imports: [FormsModule, RouterLink],
  templateUrl: './catalog.component.html',
  styleUrl: './catalog.component.css',
})
export class CatalogComponent implements OnInit, OnDestroy {
  private readonly bookService = inject(BookService);
  private readonly route = inject(ActivatedRoute);
  readonly auth = inject(AuthService);
  private readonly destroy$ = new Subject<void>();
  private readonly searchSubject = new Subject<string>();

  allBooks = signal<Book[]>([]);
  isLoading = signal(true);
  error = signal<string | null>(null);

  searchQuery = signal('');
  selectedCategory = signal<string>('ALL');
  filterAvailability = signal<FilterAvailability>('all');
  sortBy = signal<SortOption>('title');
  currentPage = signal(1);
  readonly pageSize = 20;

  readonly categoryOptions: { key: string; label: string }[] = [
    { key: 'ALL', label: 'Toutes les catégories' },
    ...Object.values(CATEGORY_LABELS).map(label => ({ key: label, label })),
  ];

  readonly filteredBooks = computed(() => {
    let books = this.allBooks();
    const q = this.searchQuery().trim().toLowerCase();
    if (q) books = books.filter(b => b.titre.toLowerCase().includes(q) || b.auteur.toLowerCase().includes(q) || b.isbn?.toLowerCase().includes(q));
    if (this.selectedCategory() !== 'ALL') books = books.filter(b => b.categorie?.nom === this.selectedCategory());
    if (this.filterAvailability() === 'available') books = books.filter(b => b.nbDisponibles > 0);
    const sort = this.sortBy();
    return [...books].sort((a, b) => {
      if (sort === 'title') return (a.titre ?? '').localeCompare(b.titre ?? '');
      if (sort === 'author') return (a.auteur ?? '').localeCompare(b.auteur ?? '');
      if (sort === 'rating') return (b.noteMoyenne ?? 0) - (a.noteMoyenne ?? 0);
      if (sort === 'availability') return b.nbDisponibles - a.nbDisponibles;
      if (sort === 'date') return (b.dateAjout ?? '').localeCompare(a.dateAjout ?? '');
      return 0;
    });
  });

  readonly totalPages = computed(() => Math.ceil(this.filteredBooks().length / this.pageSize));
  readonly paginatedBooks = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.filteredBooks().slice(start, start + this.pageSize);
  });
  readonly pageNumbers = computed(() => {
    const total = this.totalPages(), current = this.currentPage();
    if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1);
    const pages: (number | '...')[] = [1];
    if (current > 3) pages.push('...');
    for (let i = Math.max(2, current - 1); i <= Math.min(total - 1, current + 1); i++) pages.push(i);
    if (current < total - 2) pages.push('...');
    pages.push(total);
    return pages;
  });

  ngOnInit(): void {
    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      if (params['category']) this.selectedCategory.set(params['category']);
    });
    this.searchSubject.pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(query => { this.searchQuery.set(query); this.currentPage.set(1); });
    this.loadBooks();
  }

  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  loadBooks(): void {
    this.isLoading.set(true); this.error.set(null);
    this.bookService.getAll().pipe(takeUntil(this.destroy$)).subscribe({
      next: books => { this.allBooks.set(books); this.isLoading.set(false); },
      error: () => { this.error.set('Impossible de charger les livres. Vérifiez que le serveur est démarré.'); this.isLoading.set(false); },
    });
  }

  onSearchInput(value: string): void { this.searchSubject.next(value); }
  onCategoryChange(value: string): void { this.selectedCategory.set(value); this.currentPage.set(1); }
  onAvailabilityChange(value: string): void { this.filterAvailability.set(value as FilterAvailability); this.currentPage.set(1); }
  onSortChange(value: string): void { this.sortBy.set(value as SortOption); this.currentPage.set(1); }
  clearFilters(): void { this.searchQuery.set(''); this.selectedCategory.set('ALL'); this.filterAvailability.set('all'); this.sortBy.set('title'); this.currentPage.set(1); }
  goToPage(page: number | '...'): void { if (typeof page === 'number') { this.currentPage.set(page); window.scrollTo({ top: 0, behavior: 'smooth' }); } }
  getStars(rating: number | undefined): string[] { const n = rating ?? 0; return Array.from({ length: 5 }, (_, i) => i < Math.floor(n) ? 'full' : i < n ? 'half' : 'empty'); }
}