import {Injectable} from '@angular/core';
import * as SockJS from 'sockjs-client';
import {Game} from './game';
import {Subject} from 'rxjs/internal/Subject';

export enum ClientState {
  UNIDENTIFIED, // Initial state, no used name accepted by the server yet
  IDENTIFYING, // Sending user name
  IDENTIFIED, // User name accepted by server
  CREATING, // User is creating a game
  CREATED, // User has created a game
  DELETING, // User is deleting a game
  JOINING, // User is joining a game
  JOINED, // User has joined a game
  LEAVING, // User is leaving a game
  STARTING, // User is starting a game
  STARTED, // Display the countdown page
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

  // User name attempted by the user
  attemptedUserName = '';

  // User name accepted by the server
  userName = '';

  games: Game[] = [];

  // ID of the game created or joined byh the user
  gameId = 0;

  errorMessage: string;

  // Subject used for countdown notification
  private countdownSubject = new Subject<any>();

  // Observable used for countdown notification
  countdownObservable$ = this.countdownSubject.asObservable();

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
      this.send(`connect ${this.attemptedUserName}`);
    } else {
      this.socket = new SockJS('/api/websocket');
      PlayService.log('Opening connection to the server');

      this.socket.onopen = () => {
        this.send(`connect ${this.attemptedUserName}`);
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
              case 'too-long-name':
                this.errorMessage = 'Sorry, this name is too long';
                this.changeState(ClientState.UNIDENTIFIED);
                break;
              case 'used-name':
                this.errorMessage = 'Sorry, this user name is already used';
                this.changeState(ClientState.UNIDENTIFIED);
                break;
              case 'games-list':
                this.updateGamesList(data.games);
                break;
            }
            break;
          case ClientState.IDENTIFIED:
          case ClientState.CREATING:
          case ClientState.DELETING:
          case ClientState.JOINING:
            if (data.type === 'games-list') {
              this.updateGamesList(data.games);
            }
            break;
          case ClientState.CREATED:
          case ClientState.JOINED:
            switch (data.type) {
              case 'games-list':
                this.updateGamesList(data.games);
                break;
              case 'game-start':
                this.changeState(ClientState.STARTED);
                break;
            }
            break;
          case ClientState.STARTING:
            if (data.type === 'game-start') {
              this.countdownSubject.next();
              this.changeState(ClientState.STARTED);
            }
            break;
          // TODO FBE other cases
        }
      };

      this.socket.onerror = e => {
        PlayService.log(`Socket error: ${e}`);
        this.socket.close();
      };

      this.socket.onclose = () => {
        this.errorMessage = 'Cannot connect to the server';
        this.socket = null;
        this.changeState(ClientState.UNIDENTIFIED);
        PlayService.log(`Socket closed`);
      };
    }
  }

  createGame(type: string, words: number, lang: string) {
    this.changeState(ClientState.CREATING);
    this.send(`create-game ${type} ${words} ${lang}`);
  }

  deleteGame() {
    this.changeState(ClientState.DELETING);
    this.send(`delete-game`);
  }

  joinGame(id: number) {
    this.changeState(ClientState.JOINING);
    this.send(`join-game ${id}`);
  }

  leaveGame() {
    this.changeState(ClientState.LEAVING);
    this.send(`leave-game`);
  }

  startGame() {
    this.changeState(ClientState.STARTING);
    this.send(`start-game`);
  }

  // Update the received games list, for proper display
  updateGamesList(games) {
    this.userName = this.attemptedUserName;
    let state = ClientState.IDENTIFIED;

    for (const game of games) {
      game.type = `Capture (${game.rounds} round${game.rounds > 1 ? 's' : ''})`;
      game.language = game.language === 'english' ? 'English' : 'French';

      // Verify if the game was created by the user
      if (game.creator === this.userName) {
        this.gameId = game.id;
        state = ClientState.CREATED;
      }

      // Verify if the game was joined by the user
      if (game.players.includes(this.userName)) {
        this.gameId = game.id;
        state = ClientState.JOINED;
      }
    }

    this.games = games;

    if (state !== this.state) {
      this.changeState(state);
    }
  }

  changeState(state: ClientState) {
    this.state = state;
    PlayService.log(`Changed state to ${ClientState[state]}`);
  }

  // Send an action to the server
  send(message: string) {
    PlayService.log(`Sending '${message}'`);
    this.socket.send(message);
  }
}
