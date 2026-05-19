import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BookService } from '../../services/book.service';
import { Book } from '../../models/book.model';
import { Observable, switchMap } from 'rxjs';
import { AsyncPipe } from '@angular/common';

@Component({
  selector: 'app-detail-livre',
  standalone: true,
  imports: [AsyncPipe],
  templateUrl: './detail-livre.component.html',
  styleUrl: './detail-livre.component.css'
})
export class DetailLivreComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly bookService = inject(BookService);

  livre$!: Observable<Book>;

  ngOnInit(): void {
    this.livre$ = this.route.paramMap.pipe(
      switchMap(params => this.bookService.getById(Number(params.get('id'))))
    );
  }

  emprunter(id: number): void {
    console.log('Emprunter livre', id);
  }

  reserver(id: number): void {
    console.log('Réserver livre', id);
  }
}
