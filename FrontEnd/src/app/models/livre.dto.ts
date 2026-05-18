export interface LivreDto {
  id: number;
  titre: string;
  auteur: string;
  isbn: string;
  description: string;
  nomCategorie: string;
  noteMoyenne: number;
  nbDisponibles: number;
  commentaires: AvisDto[]; // On lie avec le petit DTO des avis
}

export interface AvisDto {
  pseudo: string;
  note: number;
  commentaire: string;
}
