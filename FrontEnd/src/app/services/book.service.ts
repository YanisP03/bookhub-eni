import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Book, BookCategory } from '../models/book.model';

@Injectable({ providedIn: 'root' })
export class BookService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/books';

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
}
