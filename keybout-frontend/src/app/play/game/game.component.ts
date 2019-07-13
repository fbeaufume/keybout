import {Component} from '@angular/core';
import {ClientState, PlayService} from '../play.service';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.css']
})
export class GameComponent {

  constructor(public playService: PlayService) {
  }

  get state(): ClientState {
    return this.playService.state;
  }

  // Is this component visible
  isVisible(): boolean {
    return this.state >= ClientState.PLAYING;
  }
}
