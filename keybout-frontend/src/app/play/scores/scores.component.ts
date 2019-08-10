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

  get roundScores(): Score[] {
    return this.playService.roundScores;
  }

  get topRoundUser(): string {
    return this.roundScores[0].userName;
  }

  get gameScores(): Score[] {
    return this.playService.gameScores;
  }

  get topGameUser(): string {
    return this.gameScores[0].userName;
  }

  get firstRoundUser(): string {
    return this.roundScores[0].userName;
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
