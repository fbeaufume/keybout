import {Injectable} from '@angular/core';
import * as SockJS from 'sockjs-client';
import {Game} from './game';

export enum ClientState {
  UNIDENTIFIED, // Initial state, no used name accepted by the server yet
  IDENTIFYING, // Sending user name, used to disable the corresponding button
  IDENTIFIED, // User name accepted by server
  CREATING, // Creating a game, used to disable the corresponding buttons
  CREATED, // Has created a game
  DELETING, // Deleting a game, used to disable the corresponding buttons
  JOINING, // Joined a game, used to disable the corresponding buttons
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

  // User name attempted by the user
  attemptedUserName = '';

  // User name accepted by the server
  userName = '';

  games: Game[] = [];

  // ID of the game created or joined byh the user
  gameId = 0;

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

        // Process the games list notification outside the main switch/case
        // since several states use the games list
        if (data.type === 'games-list') {
          this.updateGamesList(data.games);
        }

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
    this.changeState(ClientState.CREATING);
    this.send(`leave-game`);
  }

  startGame() {
    // TODO FBE
  }

  // Update the received games list, for proper display
  updateGamesList(games) {
    this.userName = this.attemptedUserName;
    this.changeState(ClientState.IDENTIFIED);

    for (const game of games) {
      game.type = `Capture (${game.rounds} round${game.rounds > 1 ? 's' : ''})`;
      game.language = game.language === 'english' ? 'English' : 'French';

      // Verify if the game was created by the user
      if (game.creator === this.userName) {
        this.gameId = game.id;
        this.changeState(ClientState.CREATED);
      }

      // Verify if the game was joined by the user
      if (game.players.includes(this.userName)) {
        this.gameId = game.id;
        this.changeState(ClientState.JOINED);
      }
    }

    this.games = games;
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
