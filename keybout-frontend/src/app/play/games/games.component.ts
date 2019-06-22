import {Component} from '@angular/core';
import {ClientState, PlayService} from '../play.service';
import {Game} from '../game';

@Component({
  selector: 'app-games',
  templateUrl: './games.component.html',
  styleUrls: ['./games.component.css']
})
export class GamesComponent {

  // TODO FBE use base class with : PlayService injection, state attribute, etc

  constructor(public playService: PlayService) {
  }

  get state(): ClientState {
    return this.playService.state;
  }

  get games(): Game[] {
    return this.playService.games;
  }

  // Is this component visible
  isVisible(): boolean {
    return this.state >= ClientState.IDENTIFIED && this.state <= ClientState.JOINED;
  }

  noAvailableGame(): boolean {
    return this.games === undefined || this.games.length === 0;
  }
}
