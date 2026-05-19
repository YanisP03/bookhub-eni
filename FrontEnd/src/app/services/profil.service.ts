import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UtilisateurProfil {
  id: number;
  nom: string;
  prenom: string;
  mail: string;
  telephone?: string;
  roleLibelle: string;
}

export interface DashboardStats {
  totalLivres: number;
  livresDisponibles: number;
  livresEmpruntes: number;
  totalUtilisateurs: number;
  empruntsEnCours: number;
  empruntsEnRetard: number;
  reservationsEnAttente: number;
}

@Injectable({ providedIn: 'root' })
export class ProfilService {
  private readonly http = inject(HttpClient);
  private readonly base = 'http://localhost:8080/api';

  getProfil(): Observable<UtilisateurProfil> {
    return this.http.get<UtilisateurProfil>(`${this.base}/utilisateurs/profil`);
  }

  updateProfil(data: { nom: string; prenom: string; telephone: string }): Observable<UtilisateurProfil> {
    return this.http.put<UtilisateurProfil>(`${this.base}/utilisateurs/profil`, data);
  }

  changerMotDePasse(ancienMotDePasse: string, nouveauMotDePasse: string): Observable<unknown> {
    return this.http.put(`${this.base}/utilisateurs/mot-de-passe`, { ancienMotDePasse, nouveauMotDePasse });
  }

  supprimerCompte(motDePasse: string): Observable<unknown> {
    return this.http.delete(`${this.base}/utilisateurs/compte`, { body: { motDePasse } });
  }

  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.base}/dashboard/stats`);
  }

  creerBibliothecaire(data: { nom: string; prenom: string; mail: string; motDePasse: string }): Observable<unknown> {
    return this.http.post(`${this.base}/utilisateurs/bibliothecaire`, data);
  }

  promouvoirBibliothecaire(mail: string): Observable<unknown> {
    return this.http.put(`${this.base}/utilisateurs/bibliothecaire/promouvoir`, { mail });
  }
}
