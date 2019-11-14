import {Injectable} from '@angular/core';
import * as SockJS from 'sockjs-client';
import {Subject} from 'rxjs/internal/Subject';
import {Game} from './game';
import {Score} from './score';
import {environment} from '../../environments/environment';

export enum ClientState {
  UNIDENTIFIED, // Initial state, no used name accepted by the server yet
  IDENTIFYING, // Sending user name
  LOBBY, // User is iedntied and in the lobby getting the list of games
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

export enum GameType {
  CAPTURE = 'capture',
  RACE = 'race'
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

  // True when the user wants to disconnect from the server,
  // used to prevent the error message displayed when the user is disconnected from the server
  quitting = false;

  games: Game[] = [];

  // ID of the game created or joined by the user
  gameId = 0;

  // Scores of the round that ended
  roundScores: Score[] = [];

  // Current game scores, when a round ended
  gameScores: Score[] = [];

  // Manager of the game, i.e. the user that can start the next round
  gameManager = '';

  // Type of the game played, such as 'capture' or 'race'
  gameType = '';

  gameOver = false;

  errorMessage: string;

  // Subject used for state change notification
  private stateSubject = new Subject<ClientState>();

  // Observable used for state change notification
  stateObservable$ = this.stateSubject.asObservable();

  // Map used to track who captured each word
  // The key is the word label, the value is the user name or empty is not captured yet
  words: Map<string, string> = new Map();

  static log(message: string) {
    if (!environment.production) {
      console.log(`PlayService: ${message}`);
    }
  }

  constructor() {
    this.changeState(ClientState.UNIDENTIFIED);
  }

  isGameType(gameType: GameType): boolean {
    return this.gameType === gameType;
  }

  connect() {
    this.changeState(ClientState.IDENTIFYING);
    this.errorMessage = null;

    if (this.socket != null) {
      this.send(`connect ${this.attemptedUserName}`);
    } else {
      this.socket = new SockJS('/api/websocket', null, {timeout: 5000});
      // Uncomment during development to use a specific SockJS transport
      // this.socket = new SockJS('/api/websocket', null, {transports: 'websocket', timeout: 60000});
      // this.socket = new SockJS('/api/websocket', null, {transports: 'xhr-streaming', timeout: 60000});
      // this.socket = new SockJS('/api/websocket', null, {transports: 'xhr-polling', timeout: 60000});
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
          case ClientState.LOBBY:
          case ClientState.CREATING:
          case ClientState.DELETING:
          case ClientState.JOINING:
          case ClientState.LEAVING:
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
                this.gameStarted(data.gameType);
                break;
            }
            break;
          case ClientState.STARTING_GAME:
            if (data.type === 'game-start') {
              this.gameStarted(data.gameType);
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
            if (data.type === 'manager') {
              this.gameManager = data.manager;
            }
            break;
          case ClientState.END_ROUND:
            if (data.type === 'game-start') {
              this.gameStarted(data.gameType);
            }
            if (data.type === 'manager') {
              this.gameManager = data.manager;
            }
            break;
          case ClientState.SCORES:
            if (data.type === 'game-start') {
              this.gameStarted(data.gameType);
            }
            if (data.type === 'manager') {
              this.gameManager = data.manager;
            }
            break;
          case ClientState.STARTING_ROUND:
            if (data.type === 'game-start') {
              this.gameStarted(data.gameType);
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
        if (this.quitting === false) {
          this.errorMessage = (this.state >= ClientState.LOBBY) ? 'Disconnected from the server' : 'Cannot connect to the server';
        } else {
          this.quitting = false;
        }
        this.socket = null;
        this.changeState(ClientState.UNIDENTIFIED);
        PlayService.log('Socket closed');
      };
    }
  }

  // Disconnect the user from the server
  quit() {
    this.attemptedUserName = '';
    this.userName = '';
    this.quitting = true;
    this.socket.close();
  }

  createGame(type: string, rounds: number, language: string, wordsCount: number, wordsLength: string) {
    this.changeState(ClientState.CREATING);
    this.send(`create-game ${type} ${rounds} ${language} ${wordsCount} ${wordsLength}`);
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

  gameStarted(gameType) {
    this.gameType = gameType;
    this.changeState(ClientState.STARTED);
  }

  // Update the displayed games list
  updateGamesList(games) {
    this.userName = this.attemptedUserName;
    let state = ClientState.LOBBY;

    for (const game of games) {
      // Format the game type
      game.type = game.type.charAt(0).toUpperCase() + game.type.substring(1);

      // Format the game language
      game.language = game.language === 'en' ? 'English' : 'French';

      // Format the words length by capitalizing the first letter
      game.wordsLength = game.wordsLength.charAt(0).toUpperCase() + game.wordsLength.substring(1);

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
