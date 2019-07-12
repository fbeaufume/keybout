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
    {id: 'capture 5', name: 'Capture (5 rounds)'},
    {id: 'capture 10', name: 'Capture (10 rounds)'}
  ];

  // Available word counts
  wordCounts = [5, 10, 20, 30, 50];

  // Available game langs
  langs = [
    {id: 'english', name: 'English'},
    {id: 'french', name: 'French'}
  ];

  // Selected game type
  type = this.types[0].id;

  // Selected word count
  wordCount = this.wordCounts[1];

  // Selected lang
  lang = this.langs[0].id;

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
    return this.state >= ClientState.IDENTIFIED && this.state <= ClientState.JOINED;
  }

  noAvailableGame(): boolean {
    return this.games === undefined || this.games.length === 0;
  }

  canCreate() {
    return this.state === ClientState.IDENTIFIED;
  }

  // Create a new game
  create() {
    this.playService.createGame(this.type, this.wordCount, this.lang);
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
