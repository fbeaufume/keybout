import {Component} from '@angular/core';
import {ClientState, GameType, PlayService} from '../play.service';
import {Game} from '../game';

@Component({
  selector: 'app-games',
  templateUrl: './games.component.html',
  styleUrls: ['./games.component.css']
})
export class GamesComponent {

  // Available game types
  types = [GameType.CAPTURE, GameType.RACE];
  typesLabels = {
    capture: 'Capture',
    race: 'Race'
  };

  // Selected game type
  type = GameType.CAPTURE;

  // Available number of rounds
  roundsCounts = [1, 2, 3];

  // Selected number of rounds
  rounds = 1;

  // Available game languages
  languages = ['en', 'fr'];
  languagesLabels = {
    en: 'English',
    fr: 'French'
  };

  // Selected lang
  language = 'en';

  // Available word counts
  wordsCounts = [5, 7, 10, 15, 20, 30];

  // Selected word count
  wordsCount = 10;

  // Available word lengths
  wordsLengths = ['shortest', 'shorter', 'standard', 'longer', 'longest'];
  wordsLengthsLabels = {
    shortest: 'Shortest',
    shorter: 'Shorter',
    standard: 'Standard',
    longer: 'Longer',
    longest: 'Longest'
  };

  // Selected word length
  wordsLength = 'standard';

  // Available word effects
  wordsEffects = ['none', 'hidden', 'reverse', 'shuffle'];
  wordsEffectsLabels = {
    none: {inForm: 'None', inList: 'No'},
    hidden: {inForm: 'Hidden', inList: 'Hidden'},
    reverse: {inForm: 'Reverse', inList: 'Reverse'},
    shuffle: {inForm: 'Shuffle', inList: 'Shuffle'}
  };

  // Selected word effect
  wordsEffect = 'none';

  constructor(public playService: PlayService) {
  }

  get state(): ClientState {
    return this.playService.state;
  }

  get games(): Game[] {
    return this.playService.games;
  }

  get gameId(): number {
    return this.playService.gameId;
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
    this.playService.createGame(this.type, this.rounds, this.language, this.wordsCount, this.wordsLength, this.wordsEffect);
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
