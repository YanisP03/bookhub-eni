import { inject } from '@angular/core';
import { Routes, Router } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { AuthService } from './services/auth.service';

const adminGuard = () => {
  const auth = inject(AuthService);
  if (auth.isAdmin()) return true;
  inject(Router).navigate(['/']);
  return false;
};

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home/home.component').then(m => m.HomeComponent),
  },
  {
    path: 'catalogue',
    loadComponent: () => import('./pages/catalog/catalog.component').then(m => m.CatalogComponent),
  },
  {
    path: 'catalogue/:id',
    loadComponent: () => import('./pages/livre-detail/livre-detail.component').then(m => m.LivreDetailComponent),
  },
  {
    path: 'mes-emprunts',
    loadComponent: () => import('./pages/mes-emprunts/mes-emprunts.component').then(m => m.MesEmpruntsComponent),
    canActivate: [authGuard],
  },
  {
    path: 'profil',
    loadComponent: () => import('./pages/profil/profil.component').then(m => m.ProfilComponent),
    canActivate: [authGuard],
  },
  {
    path: 'admin/dashboard',
    loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard, adminGuard],
  },
  {
    path: 'livres/nouveau',
    loadComponent: () => import('./pages/ajoutModifLivre/livre-form.component').then(m => m.LivreFormComponent),
    canActivate: [authGuard, adminGuard],
  },
  {
    path: 'catalogue/:id/modifier',
    loadComponent: () => import('./pages/ajoutModifLivre/livre-form.component').then(m => m.LivreFormComponent),
    canActivate: [authGuard, adminGuard],
  },
  {
    path: 'connexion',
    loadComponent: () => import('./pages/connexion/connexion.component').then(m => m.ConnexionComponent),
  },
  {
    path: 'inscription',
    loadComponent: () => import('./pages/inscription/inscription.component').then(m => m.InscriptionComponent),
  },
  { path: '**', redirectTo: '' },
];
