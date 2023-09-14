import {Component} from '@angular/core';
import {ClientState} from './play/model';
import {PlayService} from './play/play.service';
import { NgIf } from '@angular/common';
import { RouterLinkActive, RouterLink, RouterOutlet } from '@angular/router';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    standalone: true,
    imports: [RouterLinkActive, RouterLink, NgIf, RouterOutlet]
})
export class AppComponent {

  constructor(public playService: PlayService) {
  }

  isIdentified(): boolean {
    return this.playService.state >= ClientState.LOBBY;
  }

  get userName(): string {
    return this.playService.userName;
  }

  isAdmin(): boolean {
    return PlayService.adminMode;
  }

  quit() {
    this.playService.quit();
  }
}
