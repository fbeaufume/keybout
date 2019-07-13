import {Component} from '@angular/core';
import {ClientState, PlayService} from '../play.service';

@Component({
  selector: 'app-start',
  templateUrl: './start.component.html',
  styleUrls: ['./start.component.css']
})
export class StartComponent {

  intervalId = 0;

  countdown = 1;

  constructor(public playService: PlayService) {
    this.playService.countdownObservable$.subscribe(() => this.startCountdown());
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

    this.countdown = 5;

    this.intervalId = window.setInterval(() => {
      this.countdown -= 1;

      if (this.countdown <= 1) {
        clearInterval(this.intervalId);
      }
    }, 1000);
  }
}
