import {Component} from '@angular/core';
import { ResultsComponent } from './results/results.component';
import { GameComponent } from './game/game.component';
import { StartComponent } from './start/start.component';
import { GamesComponent } from './games/games.component';
import { ConnectComponent } from './connect/connect.component';

@Component({
    selector: 'app-play',
    templateUrl: './play.component.html',
    standalone: true,
    imports: [ConnectComponent, GamesComponent, StartComponent, GameComponent, ResultsComponent]
})
export class PlayComponent {

  constructor() {
  }
}
