import {Component, ElementRef, ViewChild} from '@angular/core';
import {ClientState, PlayService} from '../play.service';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.css']
})
export class GameComponent {

  // To give the focus to the word input
  @ViewChild('wordInput', {read: ElementRef}) wordInput: ElementRef;

  inputWord = '';

  constructor(public playService: PlayService) {
    this.playService.stateObservable$.subscribe(state => {
      if (state === ClientState.RUNNING) {
        // See https://github.com/angular/components/issues/3922
        // and https://stackoverflow.com/questions/39908967/how-to-get-reference-of-the-component-associated-with-elementref-in-angular-2/39909203#39909203
        // and https://blog.angular-university.io/angular-viewchild/
        //this.wordInput.nativeElement.focus(); // Does not work here (or in ngAfterViewInit) : ERROR TypeError: "_this.wordInput is undefined"
      }
    });
  }

  get state(): ClientState {
    return this.playService.state;
  }

  get words(): Map<string, string> {
    return this.playService.words;
  }

  // Is this component visible
  isVisible(): boolean {
    return this.state >= ClientState.RUNNING && this.state <= ClientState.END_ROUND;
  }

  // TODO FBE focus the input

  // Return the class attribute for a given word
  getClass(label: string) {
    switch (this.words.get(label)) {
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
      const userName = this.playService.words.get(this.inputWord);
      if (userName != null) {
        if (userName === '') {
          this.playService.claimWord(this.inputWord);
        }
        this.inputWord = '';
      }
    }
  }

  canViewScores() {
    return this.state === ClientState.END_ROUND;
  }

  viewScores() {
    this.playService.changeState(ClientState.SCORES);
  }
}
