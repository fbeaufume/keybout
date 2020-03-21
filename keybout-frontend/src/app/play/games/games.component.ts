import {Component} from '@angular/core';
import {
  ClientState,
  Difficulties,
  DifficultyLabels,
  Game,
  GameModeLabels,
  GameModes,
  GameStyle,
  GameStyleLabels,
  GameStyles,
  Language,
  LanguageLabels,
  Languages
} from '../model';
import {PlayService} from '../play.service';

class GameForm {
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

  // Previously selected lang
  previousLanguage = this.language;

  // Available difficulties
  difficulties = Difficulties;
  difficultyLabels = DifficultyLabels;

  // Selected difficulty
  difficulty = this.difficulties[0];

  styleChanged() {
    // Switching to a style without language
    if (this.style === GameStyle.CALCULUS) {
      this.previousLanguage = this.language;
      this.language = Language.NONE;
    }
    // Switching to a style with language
    if (this.style !== GameStyle.CALCULUS && this.language === Language.NONE) {
      this.language = this.previousLanguage;
    }
  }

  isLanguageDisabled() {
    return this.language === Language.NONE;
  }
}

@Component({
  selector: 'app-games',
  templateUrl: './games.component.html'
})
export class GamesComponent {

  constructor(public playService: PlayService) {
  }

  gameForm = new GameForm();

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

  styleChanged() {
    this.gameForm.styleChanged();
  }

  // Create a new game
  create() {
    this.playService.createGame(this.gameForm.mode, this.gameForm.style, this.gameForm.language, this.gameForm.difficulty);
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
