import {Component} from '@angular/core';
import {ClientState, PlayService} from '../play.service';

const COUNTDOWN = 5; // Total duration of the countdown

@Component({
  selector: 'app-start',
  templateUrl: './start.component.html',
  styleUrls: ['./start.component.css']
})
export class StartComponent {

  intervalId = 0;

  countdown = COUNTDOWN; // Current value of the countdown

  constructor(public playService: PlayService) {
    this.playService.stateObservable$.subscribe(state => {
      if (state === ClientState.STARTED) this.startCountdown()
    });
  }

  get state(): ClientState {
    return this.playService.state;
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
