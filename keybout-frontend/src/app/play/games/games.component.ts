import {Component} from '@angular/core';
import {ClientState, PlayService} from '../play.service';
import {Game} from '../game';

@Component({
  selector: 'app-games',
  templateUrl: './games.component.html',
  styleUrls: ['./games.component.css']
})
export class GamesComponent {

  // Available game types
  types = [
    {id: 'capture 1', name: 'Capture (1 round)'},
    {id: 'capture 2', name: 'Capture (2 rounds)'},
    {id: 'capture 3', name: 'Capture (3 rounds)'},
    {id: 'capture 5', name: 'Capture (5 rounds)'}
  ];

  // Available game langs
  languages = [
    {id: 'en', name: 'English'},
    {id: 'fr', name: 'French'}
  ];

  // Available word counts
  wordCounts = [5, 7, 10, 15, 20];

  // Available word lengths
  wordLengths = [
    {id: 'shortest', name: 'Shortest'},
    {id: 'shorter', name: 'Shorter'},
    {id: 'standard', name: 'Standard'},
    {id: 'longer', name: 'Longer'},
    {id: 'longest', name: 'Longest'}
  ];

  // Selected game type
  type = this.types[0].id;

  // Selected lang
  language = this.languages[0].id;

  // Selected word count
  wordCount = this.wordCounts[2];

  // Selected word length
  wordLength = this.wordLengths[2].id;

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
    return this.state >= ClientState.IDENTIFIED && this.state <= ClientState.STARTING_GAME;
  }

  noAvailableGame(): boolean {
    return this.games === undefined || this.games.length === 0;
  }

  canCreate() {
    return this.state === ClientState.IDENTIFIED;
  }

  // Create a new game
  create() {
    this.playService.createGame(this.type, this.language, this.wordCount, this.wordLength);
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
    return this.state === ClientState.IDENTIFIED;
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
