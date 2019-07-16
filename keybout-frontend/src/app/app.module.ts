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
import {AboutComponent} from './about/about.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    PlayComponent,
    ConnectComponent,
    GamesComponent,
    StartComponent,
    GameComponent,
    AboutComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    AppRoutingModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
