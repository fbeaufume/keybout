import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './home/home.component';
import {PlayComponent} from './play/play.component';
import {DemoComponent} from './demo/demo.component';
import {ScoresComponent} from './scores/scores.component';
import {StatsComponent} from './stats/stats.component';
import {DocumentationComponent} from './documentation/documentation.component';
import {AboutComponent} from './about/about.component';

const routes: Routes = [
  {path: '', redirectTo: '/home', pathMatch: 'full'},
  {path: 'home', component: HomeComponent},
  {path: 'play', component: PlayComponent},
  {path: 'demo', component: DemoComponent},
  {path: 'scores', component: ScoresComponent},
  {path: 'statistics', component: StatsComponent},
  {path: 'documentation', component: DocumentationComponent},
  {path: 'about', component: AboutComponent},
];

// TODO FBE keep routing but remove the routing module ?
@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
