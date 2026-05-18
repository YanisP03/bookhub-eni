export interface LoginRequest {
  mail: string;
  motDePasse: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  email: string;
  role: string;
}

export interface CurrentUser {
  email: string;
  role: string;
}
