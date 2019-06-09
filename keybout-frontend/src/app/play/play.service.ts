import {Injectable} from '@angular/core';
import * as SockJS from 'sockjs-client';

@Injectable({
  providedIn: 'root'
})
export class PlayService {

  state = 'NOT_CONNECTED';

  socket: SockJS;

  userName: string;

  constructor() {
  }

  connect() {
    if (this.state === 'NOT_CONNECTED') {
      this.socket = new SockJS('/api/websocket');
      PlayService.log('Connecting to server');

      this.socket.onopen = () => {
        this.changeState('CONNECTED');
        this.socket.send(`connect ${this.userName}`);
      };

      this.socket.onmessage = m => {
        PlayService.log(`Received '${m.data}'`);
        this.changeState('IDENTIFIED');
      };
    }
  }

  changeState(state: string) {
    this.state = state;
    PlayService.log(`Changed state to ${state}`)
  }

  static log(message: string) {
    console.log(`PlayService: ${message}`);
  }
}
