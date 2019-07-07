import { Component } from '@angular/core';
import {PlayService} from './play/play.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  constructor(public playService: PlayService) {
  }

  get userName(): string {
    return this.playService.userName;
  }
}
