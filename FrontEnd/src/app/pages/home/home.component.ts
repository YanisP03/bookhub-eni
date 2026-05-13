import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BookService } from '../../services/book.service';
import { Book, BookCategory, CATEGORY_LABELS } from '../../models/book.model';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent implements OnInit {
  private readonly bookService = inject(BookService);

  featuredBooks = signal<Book[]>([]);
  isLoading = signal(true);

  readonly categories: { key: BookCategory; label: string; icon: string }[] = [
    { key: 'ROMAN', label: CATEGORY_LABELS['ROMAN'], icon: '📖' },
    { key: 'SCIENCE_FICTION', label: CATEGORY_LABELS['SCIENCE_FICTION'], icon: '🚀' },
    { key: 'FANTASY', label: CATEGORY_LABELS['FANTASY'], icon: '🧙' },
    { key: 'POLICIER', label: CATEGORY_LABELS['POLICIER'], icon: '🔍' },
    { key: 'BIOGRAPHIE', label: CATEGORY_LABELS['BIOGRAPHIE'], icon: '👤' },
    { key: 'MANGA', label: CATEGORY_LABELS['MANGA'], icon: '🎌' },
  ];

  ngOnInit(): void {
    this.bookService.getAll().subscribe({
      next: books => { this.featuredBooks.set(books.slice(0, 6)); this.isLoading.set(false); },
      error: () => { this.isLoading.set(false); },
    });
  }

  getStars(rating: number): string[] {
    return Array.from({ length: 5 }, (_, i) =>
      i < Math.floor(rating) ? 'full' : i < rating ? 'half' : 'empty'
    );
  }
}