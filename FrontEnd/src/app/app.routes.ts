import { inject } from '@angular/core';
import { Routes, Router } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { AuthService } from './services/auth.service';
import { DetailLivreComponent } from './pages/detail-livre/detail-livre.component';
import { CatalogComponent } from './pages/catalog/catalog.component';

const adminGuard = () => {
  const auth = inject(AuthService);
  if (auth.isStaff()) return true;
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
    path: 'livre/:id',
    loadComponent: () => import('./pages/detail-livre/detail-livre.component').then(m => m.DetailLivreComponent),
  },
  {
    path: 'catalogue/:id/modifier',
    loadComponent: () => import('./pages/ajoutModifLivre/livre-form.component').then(m => m.LivreFormComponent),
    canActivate: [authGuard, adminGuard],
  },
  {
    path: 'mes-emprunts',
    loadComponent: () => import('./pages/mes-emprunts/mes-emprunts.component').then(m => m.MesEmpruntsComponent),
    canActivate: [authGuard],
  },
  {
    path: 'mon-espace',
    loadComponent: () => import('./pages/dashboard-lecteur/dashboard-lecteur.component').then(m => m.DashboardLecteurComponent),
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
    path: 'connexion',
    loadComponent: () => import('./pages/connexion/connexion.component').then(m => m.ConnexionComponent),
  },
  {
    path: 'inscription',
    loadComponent: () => import('./pages/inscription/inscription.component').then(m => m.InscriptionComponent),
  },
  { path: '**', redirectTo: '' },
];
