import {Component} from '@angular/core';
import {ClientState, Game, Score} from '../model';
import {PlayService} from '../play.service';

@Component({
  selector: 'app-scores',
  templateUrl: './result.component.html'
})
export class ResultComponent {

  constructor(public playService: PlayService) {
  }

  getMode(): string {
    return this.playService.getGameModeLower();
  }

  getStyle(): string {
    return this.playService.getGameStyleLower();
  }

  getLanguage(): string {
    return this.playService.getLanguageLower();
  }

  getDifficulty(): string {
    return this.playService.getDifficultyLower();
  }

  get state(): ClientState {
    return this.playService.state;
  }

  get game(): Game {
    return this.playService.game;
  }

  get userName(): string {
    return this.playService.userName;
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

  // Is this component visible
  isVisible(): boolean {
    return this.state >= ClientState.SCORES && this.state <= ClientState.STARTING_ROUND;
  }

  isGameManager(): boolean {
    return this.playService.userName === this.playService.gameManager;
  }

  isGameOver(): boolean {
    return this.playService.gameOver;
  }

  canStart() {
    return this.state === ClientState.SCORES;
  }

  startNextRound() {
    this.playService.startRound();
  }

  canQuit() {
    return this.state === ClientState.SCORES;
  }

  quitGame() {
    this.playService.quitGame();
  }
}
