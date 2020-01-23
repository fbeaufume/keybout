import {Component} from '@angular/core';
import {
  ClientState,
  Game,
  GameMode,
  GameModeLabels,
  GameModes,
  Languages,
  LanguageLabels,
  WordEffects,
  WordEffectLabels,
  WordLengths,
  WordLengthLabels
} from '../model';
import {PlayService} from '../play.service';

@Component({
  selector: 'app-games',
  templateUrl: './games.component.html'
})
export class GamesComponent {

  // Available game modes
  modes = GameModes;
  modeLabels = GameModeLabels;

  // Selected game mode
  mode = GameMode.CAPTURE;

  // Available number of rounds
  roundsCounts = [1, 2, 3];

  // Selected number of rounds
  rounds = 1;

  // Available game languages
  languages = Languages;
  languageLabels = LanguageLabels;

  // Selected lang
  language = 'en';

  // Available game styles
  wordEffects = WordEffects;
  wordEffectLabels = WordEffectLabels;

  // Selected game style
  wordEffect = 'none';

  // Available word counts
  wordsCounts = [5, 7, 10, 15, 20, 30];

  // Selected word count
  wordsCount = 10;

  // Available word lengths
  wordsLengths = WordLengths;
  wordsLengthsLabels = WordLengthLabels;

  // Selected word length
  wordsLength = 'standard';

  constructor(public playService: PlayService) {
  }

  get state(): ClientState {
    return this.playService.state;
  }

  get games(): Game[] {
    return this.playService.games;
  }

  get gameId(): number {
    return this.playService.game.id;
  }

  // Is this component visible
  isVisible(): boolean {
    return this.state >= ClientState.LOBBY && this.state <= ClientState.STARTING_GAME;
  }

  noAvailableGame(): boolean {
    return this.games === undefined || this.games.length === 0;
  }

  canCreate() {
    return this.state === ClientState.LOBBY;
  }

  // Create a new game
  create() {
    this.playService.createGame(this.mode, this.rounds, this.language, this.wordsCount, this.wordsLength, this.wordEffect);
  }

  canDeleteOrStart(id: number) {
    return this.state === ClientState.CREATED && id === this.gameId;
  }

  // Delete the game that the user created
  delete() {
    this.playService.deleteGame();
  }

  // Start the game that the user created
  start() {
    this.playService.startGame();
  }

  canJoin() {
    return this.state === ClientState.LOBBY;
  }

  // Join an existing game
  join(id: number) {
    this.playService.joinGame(id);
  }

  canLeave(id: number) {
    return this.state === ClientState.JOINED && id === this.gameId;
  }

  // Leave a game the user joined
  leave() {
    this.playService.leaveGame();
  }
}
