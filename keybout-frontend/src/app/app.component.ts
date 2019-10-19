import {Component} from '@angular/core';
import {ClientState, PlayService} from './play/play.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  constructor(public playService: PlayService) {
  }

  isIdentified(): boolean {
    return this.playService.state >= ClientState.IDENTIFIED;
  }

  get userName(): string {
    return this.playService.userName;
  }

  quit() {
    this.playService.quit();
  }
}
