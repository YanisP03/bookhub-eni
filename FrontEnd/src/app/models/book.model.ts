export type BookCategory =
  | 'ROMAN'
  | 'SCIENCE_FICTION'
  | 'FANTASY'
  | 'BIOGRAPHIE'
  | 'HISTOIRE'
  | 'SCIENCES'
  | 'JEUNESSE'
  | 'POLICIER'
  | 'MANGA'
  | 'AUTRE';

export const CATEGORY_LABELS: Record<BookCategory, string> = {
  ROMAN: 'Roman',
  SCIENCE_FICTION: 'Science-Fiction',
  FANTASY: 'Fantasy',
  BIOGRAPHIE: 'Biographie',
  HISTOIRE: 'Histoire',
  SCIENCES: 'Sciences',
  JEUNESSE: 'Jeunesse',
  POLICIER: 'Policier',
  MANGA: 'Manga',
  AUTRE: 'Autre',
};

export interface Categorie {
  id?: number;
  nom?: string;
  description?: string;
}

export interface Statut {
  id?: number;
  libelle?: string;
}

export interface Book {
  id: number;
  titre: string;
  auteur: string;
  isbn?: string;
  description?: string;
  couverture?: string;
  datePublication?: string;
  nbExemplaires: number;
  nbDisponibles: number;
  noteMoyenne?: number;
  dateAjout?: string;
  categorie?: Categorie;
  statut?: Statut;
}