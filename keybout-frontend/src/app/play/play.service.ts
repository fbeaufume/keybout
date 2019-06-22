import {Injectable} from '@angular/core';
import * as SockJS from 'sockjs-client';
import {Game} from './game';

export enum ClientState {
  UNIDENTIFIED, // Initial state, no used name accepted by the server yet
  IDENTIFYING, // Sending user name, used to disable the corresponding button
  IDENTIFIED, // User name accepted by server
  CREATING, // Creating a game, used to disable the corresponding button
  CREATED, // Has created a game
  JOINING, // Joined a game, used to disable the corresponding button
  JOINED, // Has joined a game
  STARTING, // A game is starting
  PLAYING, // A game is running
  END_ROUND, // A game round has ended
  END_GAME // A game has ended
}

@Injectable({
  providedIn: 'root'
})
export class PlayService {

  state: ClientState;

  socket: SockJS; // lazy initialized during the first "Connect"

  userName = '';

  games: Game[] = [];

  errorMessage: string;

  static log(message: string) {
    console.log(`PlayService: ${message}`);
  }

  constructor() {
    this.changeState(ClientState.UNIDENTIFIED);
  }

  connect() {
    this.changeState(ClientState.IDENTIFYING);
    this.errorMessage = null;

    if (this.socket != null) {
      this.socket.send(`connect ${this.userName}`);
    } else {
      this.socket = new SockJS('/api/websocket');
      PlayService.log('Opening connection to the server');

      this.socket.onopen = () => {
        this.socket.send(`connect ${this.userName}`);
      };

      this.socket.onmessage = m => {
        PlayService.log(`Received '${m.data}'`);

        const data = JSON.parse(m.data);

        switch (this.state) {
          case ClientState.IDENTIFYING:
            switch (data.type) {
              case 'incorrect-name':
                this.errorMessage = 'Sorry, this is not a valid user name';
                this.changeState(ClientState.UNIDENTIFIED);
                break;
              case 'used-name':
                this.errorMessage = 'Sorry, this user name is already used';
                this.changeState(ClientState.UNIDENTIFIED);
                break;
              case 'games-list':
                // TODO FBE use the games list
                this.changeState(ClientState.IDENTIFIED);
                break;
            }
            break;
          case ClientState.IDENTIFIED:
            // TODO FBE
            break;
        }
      };

      this.socket.onerror = e => {
        // TODO FBE
        PlayService.log(`Socket error: ${e}`);
      };

      this.socket.onclose = () => {
        // TODO FBE
        PlayService.log(`Socket closed`);
      };
    }
  }

  changeState(state: ClientState) {
    this.state = state;
    PlayService.log(`Changed state to ${ClientState[state]}`);
  }
}
