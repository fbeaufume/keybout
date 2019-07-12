import {Component} from '@angular/core';
import {ClientState, PlayService} from '../play.service';

@Component({
  selector: 'app-start',
  templateUrl: './start.component.html',
  styleUrls: ['./start.component.css']
})
export class StartComponent {

  constructor(public playService: PlayService) {
  }

  get state(): ClientState {
    return this.playService.state;
  }

  // Is this component visible
  isVisible(): boolean {
    return this.state === ClientState.STARTED;
  }
}
