import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AllComponent } from './notes/all/all.component';
import { TopComponent } from './notes/top/top.component';
import { DetailComponent } from './notes/detail/detail.component';

const routes: Routes = [
  { path: '', redirectTo: '/notes/top', pathMatch: 'full' },
  { path: 'notes/top', component: TopComponent },
  { path: 'notes/all', component: AllComponent },
  { path: 'notes/detail/:id', component: DetailComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
