export interface LoginRequest {
  mail: string;
  motDePasse: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  mail: string;
  nom: string;
  prenom: string;
  role: string;
}

export interface CurrentUser {
  mail: string;
  nom: string;
  prenom: string;
  role: string;
}
