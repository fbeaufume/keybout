import {Component} from '@angular/core';
import {ClientState, Game} from '../model';
import {PlayService} from '../play.service';
import { NgIf } from '@angular/common';

const COUNTDOWN = 5; // Total duration of the countdown

@Component({
    selector: 'app-start',
    templateUrl: './start.component.html',
    standalone: true,
    imports: [NgIf]
})
export class StartComponent {

  intervalId = 0;

  countdown = COUNTDOWN; // Current value of the countdown

  constructor(public playService: PlayService) {
    // TODO FBE unsubscribe somewhere ?
    this.playService.stateObservable$.subscribe(state => {
      if (state === ClientState.STARTED) {
        this.startCountdown();
      }
    });
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

  // Is this component visible
  isVisible(): boolean {
    return this.state === ClientState.STARTED;
  }

  startCountdown() {
    PlayService.log('Starting countdown');
    // Stop the current interval, if any
    if (this.intervalId > 0) {
      clearInterval(this.intervalId);
    }

    this.countdown = COUNTDOWN;

    this.intervalId = window.setInterval(() => {
      this.countdown -= 1;

      if (this.countdown <= 1) {
        clearInterval(this.intervalId);
      }
    }, 1000);
  }
}
