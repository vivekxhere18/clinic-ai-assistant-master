import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  {
    path: 'dashboard',
    loadComponent: () => import('./pages/dashboard/dashboard.component').then((m) => m.DashboardComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register.component').then((m) => m.RegisterComponent)
  },
  {
    path: 'patients/:id',
    loadComponent: () =>
      import('./pages/patient-detail/patient-detail.component').then((m) => m.PatientDetailComponent)
  },
  { path: '**', redirectTo: 'dashboard' }
];