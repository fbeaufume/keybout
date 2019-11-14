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
  types = [
    {id: GameType.CAPTURE, name: 'Capture'},
    {id: GameType.RACE, name: 'Race'}
  ];

  // Available number of rounds
  roundsCounts = [1, 2, 3];

  // Available game languages
  languages = [
    {id: 'en', name: 'English'},
    {id: 'fr', name: 'French'}
  ];

  // Available word counts
  wordsCounts = [3, 5, 7, 10, 15];

  // Available word lengths
  wordsLengths = [
    {id: 'shortest', name: 'Shortest'},
    {id: 'shorter', name: 'Shorter'},
    {id: 'standard', name: 'Standard'},
    {id: 'longer', name: 'Longer'},
    {id: 'longest', name: 'Longest'}
  ];

  // Selected game type
  type = this.types[0].id;

  // Selected number of rounds
  rounds = this.roundsCounts[0];

  // Selected lang
  language = this.languages[0].id;

  // Selected word count
  wordsCount = this.wordsCounts[3];

  // Selected word length
  wordsLength = this.wordsLengths[2].id;

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
    this.playService.createGame(this.type, this.rounds, this.language, this.wordsCount, this.wordsLength);
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
