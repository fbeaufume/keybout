import {Component} from '@angular/core';
import {ClientState, PlayService} from "../play.service";
import {Score} from "../score";

@Component({
  selector: 'app-scores',
  templateUrl: './scores.component.html',
  styleUrls: ['./scores.component.css']
})
export class ScoresComponent {

  constructor(public playService: PlayService) {
  }

  get state(): ClientState {
    return this.playService.state;
  }

  get scores(): Score[] {
    return this.playService.scores;
  }

  // Is this component visible
  isVisible(): boolean {
    return this.state >= ClientState.SCORES && this.state <= ClientState.STARTING_ROUND;
  }

  isGameManager(): boolean {
    return this.playService.userName === this.playService.gameManager;
  }

  canStart() {
    return this.state === ClientState.SCORES;
  }

  startNextRound() {
    this.playService.startRound();
  }
}
