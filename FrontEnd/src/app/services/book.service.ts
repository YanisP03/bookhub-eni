import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Book, BookCategory, Categorie, Statut } from '../models/book.model';
import { LivreDto } from '../models/livre.dto';

@Injectable({ providedIn: 'root' })
export class BookService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/livres';
  private readonly baseUrl = 'http://localhost:8080/api';

  getCategories(): Observable<Categorie[]> {
    return this.http.get<Categorie[]>(`${this.baseUrl}/categories`);
  }

  getStatuts(): Observable<Statut[]> {
    return this.http.get<Statut[]>(`${this.baseUrl}/statuts`);
  }

  getAll(): Observable<Book[]> {
    return this.http.get<Book[]>(this.apiUrl);
  }

  getById(id: number): Observable<Book> {
    return this.http.get<Book>(`${this.apiUrl}/${id}`);
  }

  search(query: string): Observable<Book[]> {
    const params = new HttpParams().set('query', query);
    return this.http.get<Book[]>(`${this.apiUrl}/search`, { params });
  }

  getByCategory(category: BookCategory): Observable<Book[]> {
    return this.http.get<Book[]>(`${this.apiUrl}/category/${category}`);
  }

  getAvailable(): Observable<Book[]> {
    return this.http.get<Book[]>(`${this.apiUrl}/available`);
  }

  create(book: Omit<Book, 'id'>): Observable<Book> {
    return this.http.post<Book>(this.apiUrl, book);
  }

  update(id: number, book: Book): Observable<Book> {
    return this.http.put<Book>(`${this.apiUrl}/${id}`, book);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getLivreDetails(id: number): Observable<LivreDto> {
    return this.http.get<LivreDto>(`${this.apiUrl}/${id}`);
  }
}
