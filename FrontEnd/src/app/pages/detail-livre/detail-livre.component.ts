import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BookService } from '../../services/book.service';
import { LivreDto } from '../../models/livre.dto';
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

  // 1. On transforme la variable en un Signal initialisé à undefined
  livre$!: Observable<LivreDto>;

  ngOnInit(): void {
    this.livre$ = this.route.paramMap.pipe(
      switchMap(params => {
        const id = Number(params.get('id'));
        return this.bookService.getLivreDetails(id);
      })
    );
  }

  emprunter(id: number): void {
    console.log('Emprunter livre', id);
  }

  reserver(id: number): void {
    console.log('Réserver livre', id);
  }
}
