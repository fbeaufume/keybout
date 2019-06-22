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
    return this.playService.userName;
  }

  set userName(value: string) {
    this.playService.userName = value;
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

  // Is the Connect button disabled
  isConnectDisabled(): boolean {
    return this.state === ClientState.IDENTIFYING;
  }

  connect() {
    this.playService.connect();
  }
}
