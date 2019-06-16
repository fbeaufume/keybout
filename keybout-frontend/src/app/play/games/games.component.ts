import { Component } from '@angular/core';
import {ClientState, PlayService} from "../play.service";

@Component({
  selector: 'app-games',
  templateUrl: './games.component.html',
  styleUrls: ['./games.component.css']
})
export class GamesComponent {

  // TODO FBE use base class with : PlayService injection, state attribute, etc

  constructor(public playService: PlayService) { }

  get state(): ClientState {
    return this.playService.state;
  }

  // Is this component visible
  isVisible(): boolean {
    return this.state >= ClientState.IDENTIFIED && this.state <= ClientState.JOINED;
  }
}
