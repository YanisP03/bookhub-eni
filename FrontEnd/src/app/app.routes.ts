import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

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
    path: 'connexion',
    loadComponent: () => import('./pages/connexion/connexion.component').then(m => m.ConnexionComponent),
  },
  {
    path: 'inscription',
    loadComponent: () => import('./pages/inscription/inscription.component').then(m => m.InscriptionComponent),
  },
  { path: '**', redirectTo: '' },
];
