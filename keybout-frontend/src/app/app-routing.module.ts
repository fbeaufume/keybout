import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {HomeComponent} from "./home/home.component";
import {PlayComponent} from './play/play/play.component';
import {AboutComponent} from "./about/about.component";
import {AllComponent} from './notes/all/all.component';
import {TopComponent} from './notes/top/top.component';
import {DetailComponent} from './notes/detail/detail.component';

const routes: Routes = [
  // Keybout routes
  {path: '', redirectTo: '/home', pathMatch: 'full'},
  {path: 'home', component: HomeComponent},
  {path: 'play', component: PlayComponent},
  {path: 'about', component: AboutComponent},
  // Tour of Heroes routes
  {path: 'notes/top', component: TopComponent},
  {path: 'notes/all', component: AllComponent},
  {path: 'notes/detail/:id', component: DetailComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
