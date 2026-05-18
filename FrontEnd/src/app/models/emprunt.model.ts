import { Book } from './book.model';

export interface Emprunt {
  id: number;
  dateEmprunt: string;
  dateRetourPrevue: string;
  dateRetour?: string;
  joursRetard: number;
  livre: Book;
  statut: { id: number; libelle: string };
}

export interface Reservation {
  id: number;
  dateReservation: string;
  positionFileAttente: number;
  livre: Book;
  statut: { id: number; libelle: string };
}
