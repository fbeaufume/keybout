import {Injectable} from '@angular/core';
import * as SockJS from 'sockjs-client';
import {Subject} from 'rxjs/internal/Subject';
import {ClientState, GameMode, Game, Word, Score, GameModeLabels, GameStyleLabels} from './model';
import {environment} from '../../environments/environment';

// Contains the play state and communicates with the backend (by sending actions and receiving notifications)
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

  // The list of declared games received from the server
  games: Game[] = [];

  // Game created or joined by the user
  game: Game;

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

  // Map used to track who captured each word
  // The key is the word label, the value is the user name or empty is not captured yet
  words: Map<string, Word> = new Map();

  // Number of available words, used to display a wait message at the end of a race game round
  availableWords = 0;

  // Logs are active during development or when typing "window.keyboutLogging=true" in the console
  static log(message: string) {
    if (!environment.production || (window as any).keyboutLogging) {
      console.log(`PlayService: ${message}`);
    }
  }

  constructor() {
    this.changeState(ClientState.UNIDENTIFIED);
  }

  isGameMode(gameMode: GameMode): boolean {
    return this.game.mode === gameMode;
  }

  getGameModeLower(): string {
    return GameModeLabels[this.game.mode].lower;
  }

  getGameStyleLower(): string {
    return GameStyleLabels[this.game.style].lower;
  }

  connect() {
    this.changeState(ClientState.IDENTIFYING);
    this.errorMessage = null;

    if (this.socket != null) {
      this.send(`connect ${this.attemptedUserName}`);
    } else {
      this.socket = new SockJS('/api/websocket', null, {timeout: 15000});
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
              case 'too-short-name':
                this.errorMessage = 'Sorry, this name is too short';
                this.changeState(ClientState.UNIDENTIFIED);
                break;
              case 'too-long-name':
                this.errorMessage = 'Sorry, this name is too long';
                this.changeState(ClientState.UNIDENTIFIED);
                break;
              case 'used-name':
                this.errorMessage = 'Sorry, this name is already used';
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
              this.updateRoundAwards();
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
              this.gameStarted();
            }
            if (data.type === 'manager') {
              this.gameManager = data.manager;
            }
            break;
          case ClientState.SCORES:
            if (data.type === 'game-start') {
              this.gameStarted();
            }
            if (data.type === 'manager') {
              this.gameManager = data.manager;
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

  createGame(mode: string, style: string, rounds: number, language: string, wordsCount: number, wordsLength: string) {
    this.changeState(ClientState.CREATING);
    this.send(`create-game ${mode} ${style} ${rounds} ${language} ${wordsCount} ${wordsLength}`);
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
    let state = ClientState.LOBBY;

    for (const game of games) {
      // Verify if the game was created by the user
      if (game.creator === this.userName) {
        this.game = game;
        state = ClientState.CREATED;
      }

      // Verify if the game was joined by the user
      if (game.players.includes(this.userName)) {
        this.game = game;
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
    let availableWords = 0;
    for (const k of Object.keys(words)) {
      const word = words[k];
      const userName = word[0];
      if (userName === '') {
        availableWords++;
      }
      this.words.set(k, new Word(k, userName, word[1]));
    }
    this.availableWords = availableWords;
  }

  // The backend sent a single number containing all aggregated awards,
  // this method extracts the awards name
  updateRoundAwards() {
    for (const score of this.roundScores) {
      score.awardsNames = [];

      // tslint:disable-next-line:no-bitwise
      if (score.awards & 1) {
        score.awardsNames.push('First');
      }
      // tslint:disable-next-line:no-bitwise
      if (score.awards & 2) {
        score.awardsNames.push('Longest');
      }
      // tslint:disable-next-line:no-bitwise
      if (score.awards & 4) {
        score.awardsNames.push('Last');
      }
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
