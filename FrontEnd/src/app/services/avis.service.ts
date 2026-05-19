import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AvisDTO {
  id: number;
  pseudo: string;
  note: number;
  commentaire?: string;
  datePublication: string;
  statut: string;
  nomLivre?: string;
  idLivre?: number;
  nomUtilisateur?: string;
}

@Injectable({ providedIn: 'root' })
export class AvisService {
  private readonly http = inject(HttpClient);
  private readonly base = 'http://localhost:8080/api/avis';

  getAvisLivre(livreId: number): Observable<AvisDTO[]> {
    return this.http.get<AvisDTO[]>(`${this.base}/livre/${livreId}`);
  }

  posterAvis(idLivre: number, note: number, commentaire: string): Observable<AvisDTO> {
    return this.http.post<AvisDTO>(this.base, { idLivre, note, commentaire });
  }

  getMesAvis(): Observable<AvisDTO[]> {
    return this.http.get<AvisDTO[]>(`${this.base}/mes-avis`);
  }

  getAvisEnAttente(): Observable<AvisDTO[]> {
    return this.http.get<AvisDTO[]>(`${this.base}/moderation`);
  }

  approuver(id: number): Observable<AvisDTO> {
    return this.http.patch<AvisDTO>(`${this.base}/${id}/moderer?decision=APPROUVE`, {});
  }

  rejeter(id: number): Observable<AvisDTO> {
    return this.http.patch<AvisDTO>(`${this.base}/${id}/moderer?decision=REJETE`, {});
  }
}
