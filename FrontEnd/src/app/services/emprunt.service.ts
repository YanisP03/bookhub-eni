import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Emprunt, Reservation } from '../models/emprunt.model';

@Injectable({ providedIn: 'root' })
export class EmpruntService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api';

  getMesEmprunts(): Observable<Emprunt[]> {
    return this.http.get<Emprunt[]>(`${this.baseUrl}/emprunts/mes-emprunts`);
  }

  /** Lecteur : soumet une demande d'emprunt */
  emprunter(livreId: number): Observable<Emprunt> {
    return this.http.post<Emprunt>(`${this.baseUrl}/emprunts/${livreId}`, {});
  }

  rendre(empruntId: number): Observable<Emprunt> {
    return this.http.put<Emprunt>(`${this.baseUrl}/emprunts/${empruntId}/retour`, {});
  }

  // ── Admin ──────────────────────────────────────────────────────────────────

  getDemandesEnAttente(): Observable<Emprunt[]> {
    return this.http.get<Emprunt[]>(`${this.baseUrl}/emprunts/demandes`);
  }

  validerEmprunt(empruntId: number): Observable<Emprunt> {
    return this.http.put<Emprunt>(`${this.baseUrl}/emprunts/${empruntId}/valider`, {});
  }

  refuserEmprunt(empruntId: number): Observable<Emprunt> {
    return this.http.put<Emprunt>(`${this.baseUrl}/emprunts/${empruntId}/refuser`, {});
  }

  getFileAttente(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.baseUrl}/reservations/file-attente`);
  }

  // ── Réservations ───────────────────────────────────────────────────────────

  getMesReservations(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.baseUrl}/reservations/mes-reservations`);
  }

  reserver(livreId: number): Observable<Reservation> {
    return this.http.post<Reservation>(`${this.baseUrl}/reservations/${livreId}`, {});
  }

  annulerReservation(reservationId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/reservations/${reservationId}`);
  }
}
