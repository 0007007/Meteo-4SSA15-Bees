import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    title: 'Weather - Meteo App',
    loadComponent: () => import('./pages/dashboard/dashboard').then((m) => m.Dashboard),
  },
  {
    // one generic page for the 14 entities: /manage/cities, /manage/reports, ...
    path: 'manage/:resource',
    loadComponent: () => import('./pages/crud-page/crud-page').then((m) => m.CrudPage),
  },
  { path: '**', redirectTo: '' },
];
