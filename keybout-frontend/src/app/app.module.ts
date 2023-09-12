import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';

import {HomeComponent} from './home/home.component';
import {PlayComponent} from './play/play.component';
import {ConnectComponent} from './play/connect/connect.component';
import {GamesComponent} from './play/games/games.component';
import {StartComponent} from './play/start/start.component';
import {GameComponent} from './play/game/game.component';
import {ResultsComponent} from './play/results/results.component';
import {DemoComponent} from './demo/demo.component';
import {ScoresComponent} from './scores/scores.component';
import {StatsComponent} from './stats/stats.component';
import {DocumentationComponent} from './documentation/documentation.component';
import {AboutComponent} from './about/about.component';

@NgModule({
    declarations: [AppComponent],
    imports: [
        BrowserModule,
        FormsModule,
        AppRoutingModule,
        HttpClientModule,
        HomeComponent,
        PlayComponent,
        ConnectComponent,
        GamesComponent,
        StartComponent,
        GameComponent,
        ResultsComponent,
        DemoComponent,
        ScoresComponent,
        StatsComponent,
        DocumentationComponent,
        AboutComponent
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {
}
