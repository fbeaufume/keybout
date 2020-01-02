import {Component, ElementRef, ViewChild} from '@angular/core';
import {ClientState, GameType, PlayService} from '../play.service';
import {Word} from '../word';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html'
})
export class GameComponent {

  @ViewChild('outerDiv', {static: true}) outerDiv: ElementRef;

  inputWord = '';

  constructor(public playService: PlayService) {
    this.playService.stateObservable$.subscribe(state => {
      if (state === ClientState.RUNNING) {
        // Give the focus to the input element,
        // inspired by https://stackoverflow.com/a/45590831/623185
        setTimeout(() => {
          this.inputWord = '';
          this.outerDiv.nativeElement.getElementsByTagName('input').item(0).focus();
        }, 0);
      }
    });
  }

  get state(): ClientState {
    return this.playService.state;
  }

  get words(): Map<string, Word> {
    return this.playService.words;
  }

  // Is this component visible
  isVisible(): boolean {
    return this.state >= ClientState.RUNNING && this.state <= ClientState.END_ROUND;
  }

  // Return the class attribute for a given word
  getClass(label: string) {
    switch (this.words.get(label).userName) {
      case '':
        return 'btn btn-primary btn-lg mr-2 mb-2';
      case this.playService.userName:
        return 'btn btn-success btn-lg mr-2 mb-2';
      default:
        return 'btn btn-danger btn-lg mr-2 mb-2';
    }
  }

  onKey() {
    // The space key clears the input
    if (this.inputWord.includes(' ')) {
      this.inputWord = '';
    } else {
      // Try to match an available word
      const typed = this.inputWord.toLowerCase();
      const word = this.playService.words.get(typed);
      const userName = word ? word.userName : null;
      if (userName != null) {
        if (userName === '') {
          this.playService.claimWord(typed);
        }
        this.inputWord = '';
      }
    }
  }

  // The waiting message is displayed for a race game when the user has caught all his words
  isWaiting() {
    return this.state !== ClientState.END_ROUND && this.playService.isGameType(GameType.RACE) && this.playService.availableWords === 0;
  }

  canViewScores() {
    return this.state === ClientState.END_ROUND;
  }

  viewScores() {
    this.playService.changeState(ClientState.SCORES);
  }
}
