import {Injectable} from '@angular/core';
import * as SockJS from 'sockjs-client';
import {Subject} from 'rxjs/internal/Subject';
import {Game} from './game';
import {Score} from './score';

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
  STARTING_GAME, // User is starting a game, i.e. has clicked on Start button
  STARTED, // Display the countdown page
  RUNNING, // A game is running
  END_ROUND, // A game round has ended, display the View scores button
  SCORES, // Displaying the scores
  STARTING_ROUND, // User is starting the next round, i.e. has clicked on Start next round
  QUITTING// User is quitting a game that ended
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

  // ID of the game created or joined by the user
  gameId = 0;

  // Scores of the round that ended
  roundScores: Score[] = [];

  // Current game scores, when a round ended
  gameScores: Score[] = [];

  // Manager of the game, i.e. the user that can start the next round
  gameManager = '';

  gameOver = false;

  errorMessage: string;

  // Subject used for state change notification
  private stateSubject = new Subject<ClientState>();

  // Observable used for state change notification
  stateObservable$ = this.stateSubject.asObservable();

  words: Map<string, string> = new Map();

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
                this.errorMessage = 'Sorry, this is not a valid user name, space is not allowed for instance';
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
                this.gameStarted();
                break;
            }
            break;
          case ClientState.STARTING_GAME:
            if (data.type === 'game-start') {
              this.gameStarted();
            }
            break;
          case ClientState.STARTED:
            if (data.type === 'words-list') {
              this.updateWords(data.words);
              this.changeState(ClientState.RUNNING);
            }
            break;
          case ClientState.RUNNING:
            if (data.type === 'words-list') {
              this.updateWords(data.words);
            }
            if (data.type === 'scores') {
              this.updateWords(data.words);
              this.roundScores = data.roundScores;
              this.gameScores = data.gameScores;
              this.gameManager = data.manager;
              this.gameOver = data.gameOver;
              this.changeState(ClientState.END_ROUND);
            }
            break;
          case ClientState.END_ROUND:
            if (data.type === 'game-start') {
              this.gameStarted();
            }
            break;
          case ClientState.SCORES:
            if (data.type === 'game-start') {
              this.gameStarted();
            }
            break;
          case ClientState.STARTING_ROUND:
            if (data.type === 'game-start') {
              this.gameStarted();
            }
            break;
          case ClientState.QUITTING:
            if (data.type === 'games-list') {
              this.updateGamesList(data.games);
            }
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
        PlayService.log('Socket closed');
      };
    }
  }

  createGame(type: string, words: number, language: string) {
    this.changeState(ClientState.CREATING);
    this.send(`create-game ${type} ${words} ${language}`);
  }

  deleteGame() {
    this.changeState(ClientState.DELETING);
    this.send('delete-game');
  }

  joinGame(id: number) {
    this.changeState(ClientState.JOINING);
    this.send(`join-game ${id}`);
  }

  leaveGame() {
    this.changeState(ClientState.LEAVING);
    this.send('leave-game');
  }

  // Start the first round of the game
  startGame() {
    this.changeState(ClientState.STARTING_GAME);
    this.send(`start-game`);
  }

  gameStarted() {
    this.changeState(ClientState.STARTED);
  }

  // Update the displayed games list
  updateGamesList(games) {
    this.userName = this.attemptedUserName;
    let state = ClientState.IDENTIFIED;

    for (const game of games) {
      game.type = `Capture (${game.rounds} round${game.rounds > 1 ? 's' : ''})`;
      game.language = game.language === 'en' ? 'English' : 'French';

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

  // Update the displayed words
  updateWords(words) {
    this.words.clear();
    for (const k of Object.keys(words)) {
      this.words.set(k, words[k]);
    }
  }

  changeState(state: ClientState) {
    this.state = state;
    this.stateSubject.next(state);
    PlayService.log(`Changed state to ${ClientState[state]}`);
  }

  // The user fully typed an available word, send it to the server
  claimWord(label: string) {
    this.send(`claim-word ${label}`);
  }

  // Start the next round of the game
  startRound() {
    this.changeState(ClientState.STARTING_ROUND);
    this.send('start-round');
  }

  // The user wants to quit a game that ended
  quitGame() {
    this.changeState(ClientState.QUITTING);
    this.send('quit-game');
  }

  // Send an action to the server
  send(message: string) {
    PlayService.log(`Sending '${message}'`);
    this.socket.send(message);
  }
}
