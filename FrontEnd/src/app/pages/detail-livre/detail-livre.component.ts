import {Component, inject, OnInit, signal} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BookService } from '../../services/book.service';
import { LivreDto } from '../../models/livre.dto';
import { Observable, switchMap } from 'rxjs';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-detail-livre',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './detail-livre.component.html',
  styleUrls: ['./detail-livre.component.css']
})
export class DetailLivreComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly bookService = inject(BookService);

  // 1. On transforme la variable en un Signal initialisé à undefined
  livre$!: Observable<LivreDto>;

  ngOnInit(): void {
    // switchMap écoute l'ID de l'URL et le transforme automatiquement en appel Back-end
    this.livre$ = this.route.paramMap.pipe(
      switchMap(params => {
        const id = Number(params.get('id'));
        return this.bookService.getLivreDetails(id);
      })
    );
  }

  emprunter(id: number | undefined) {
    console.log('Action : Emprunter le livre', id);
  }

  reserver(id: number | undefined) {
    console.log('Action : Réserver le livre', id);
  }
}
