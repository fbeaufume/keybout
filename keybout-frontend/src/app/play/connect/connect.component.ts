import {Component} from '@angular/core';
import {ClientState, PlayService} from '../play.service';

@Component({
  selector: 'app-connect',
  templateUrl: './connect.component.html',
  styleUrls: ['./connect.component.css']
})
export class ConnectComponent {

  constructor(public playService: PlayService) {
  }

  get userName(): string {
    return this.playService.attemptedUserName;
  }

  set userName(value: string) {
    this.playService.attemptedUserName = value;
  }

  get state(): ClientState {
    return this.playService.state;
  }

  get errorMessage(): string {
    return this.playService.errorMessage;
  }

  // Is this component visible
  isVisible(): boolean {
    return this.state <= ClientState.IDENTIFYING;
  }

  canConnect(): boolean {
    return this.state === ClientState.UNIDENTIFIED;
  }

  connect() {
    // Prevent 'Enter' key from the HTML input
    if (this.canConnect()) {
      this.playService.connect();
    }
  }
}
