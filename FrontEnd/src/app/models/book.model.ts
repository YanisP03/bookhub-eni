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

export interface Book {
  id: number;
  title: string;
  author: string;
  isbn: string;
  category: BookCategory;
  description: string;
  coverUrl: string;
  totalCopies: number;
  availableCopies: number;
  averageRating: number;
  ratingCount: number;
  datePublication?: string;
  idCategory?: number;
  idStatus?: number;
  nbCopies: number;
  nbAvailables: number;

}

