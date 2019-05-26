import {Component, OnInit} from '@angular/core';
import * as SockJS from 'sockjs-client';

@Component({
  selector: 'app-play',
  templateUrl: './play.component.html',
  styleUrls: ['./play.component.css']
})
export class PlayComponent implements OnInit {

  status = 'NOT_CONNECTED';

  socket: SockJS;

  userName: string;

  constructor() {
  }

  ngOnInit() {
  }

  connect() {
    if (this.status === 'NOT_CONNECTED') {
      this.socket = new SockJS('/api/websocket');
      PlayComponent.log('Connecting');

      this.socket.onopen = () => {
        this.status = 'CONNECTED';
        PlayComponent.log('Connected');

        this.socket.send(`connect ${this.userName}`);
      };

      this.socket.onmessage= m => {
        PlayComponent.log(`Received '${m.data}'`);
        PlayComponent.log('Identified');
        this.status = 'IDENTIFIED';
      }
    }
  }

  static log(message: string) {
    console.log(`PlayComponent: ${message}`);
  }
}
