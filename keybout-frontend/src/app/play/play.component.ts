import {Component, OnInit} from '@angular/core';
import {PlayService} from "./play.service";

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

  get state(): string {
    return this.playService.state;
  }

  connect() {
    this.playService.connect();
  }
}
