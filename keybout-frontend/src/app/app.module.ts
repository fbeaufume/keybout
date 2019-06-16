import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {PlayComponent} from './play/play/play.component';
import {ConnectComponent} from './play/connect/connect.component';
import {GamesComponent} from './play/games/games.component';
import {AllComponent} from './notes/all/all.component';
import {DetailComponent} from './notes/detail/detail.component';
import {MessagesComponent} from './notes/messages/messages.component';
import {TopComponent} from './notes/top/top.component';
import {SearchComponent} from './notes/search/search.component';

@NgModule({
  declarations: [
    AppComponent,
    PlayComponent,
    ConnectComponent,
    GamesComponent,
    AllComponent,
    DetailComponent,
    MessagesComponent,
    TopComponent,
    SearchComponent
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
