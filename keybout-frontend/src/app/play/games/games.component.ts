import {Component} from '@angular/core';
import {
  ClientState,
  Game,
  GameModes,
  GameModeLabels,
  GameStyles,
  GameStyleLabels,
  Languages,
  LanguageLabels,
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
  mode = this.modes[0];

  // Available game styles
  styles = GameStyles;
  styleLabels = GameStyleLabels;

  // Selected game style
  style = this.styles[0];

  // Available game languages
  languages = Languages;
  languageLabels = LanguageLabels;

  // Selected lang
  language = this.languages[0];

  // Available word lengths
  wordsLengths = WordLengths;
  wordsLengthsLabels = WordLengthLabels;

  // Selected word length
  wordsLength = this.wordsLengths[2];

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
    this.playService.createGame(this.mode, this.style, this.language, this.wordsLength);
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
