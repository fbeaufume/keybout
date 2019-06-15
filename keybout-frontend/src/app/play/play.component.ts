import {Component} from '@angular/core';
import {ClientState, PlayService} from "./play.service";

@Component({
  selector: 'app-play',
  templateUrl: './play.component.html',
  styleUrls: ['./play.component.css']
})
export class PlayComponent {

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

  connect() {
    this.playService.connect();
  }
}
