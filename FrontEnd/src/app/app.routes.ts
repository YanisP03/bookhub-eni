import { Routes } from '@angular/router';
import {DetailLivreComponent} from './pages/detail-livre/detail-livre.component';
import { CatalogComponent } from './pages/catalog/catalog.component';

// @ts-ignore
export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/home/home.component').then(m => m.HomeComponent),
  },
  {
    path: 'catalogue',
    loadComponent: () =>
      import('./pages/catalog/catalog.component').then(m => m.CatalogComponent),
  },

  {
    path: 'livre/:id',
    loadComponent: () =>
      import('./pages/detail-livre/detail-livre.component').then(m => m.DetailLivreComponent),
  },

  { path: '**', redirectTo: '' },

];
