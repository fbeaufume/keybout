import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {HomeComponent} from './home/home.component';
import {PlayComponent} from './play/play.component';
import {StatsComponent} from './stats/stats.component';
import {DemoComponent} from './demo/demo.component';
import {DocumentationComponent} from './documentation/documentation.component';
import {AboutComponent} from './about/about.component';

const routes: Routes = [
  {path: '', redirectTo: '/home', pathMatch: 'full'},
  {path: 'home', component: HomeComponent},
  {path: 'play', component: PlayComponent},
  {path: 'stats', component: StatsComponent},
  {path: 'demo', component: DemoComponent},
  {path: 'docs', component: DocumentationComponent},
  {path: 'about', component: AboutComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
