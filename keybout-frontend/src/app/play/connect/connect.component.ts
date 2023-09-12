import {Component, ElementRef, ViewChild} from '@angular/core';
import {ClientState} from '../model';
import {PlayService} from '../play.service';
import {NavigationEnd, Router} from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';

@Component({
    selector: 'app-connect',
    templateUrl: './connect.component.html',
    standalone: true,
    imports: [NgIf, FormsModule]
})
export class ConnectComponent {

  @ViewChild('outerDiv', {static: true}) outerDiv: ElementRef;

  constructor(private router: Router, public playService: PlayService) {
    // TODO FBE unsubscribe somewhere ?
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd && event.url === '/play' && this.isVisible()) {
        setTimeout(() => {
          // Give the focus to the input element,
          // inspired by https://stackoverflow.com/a/45590831/623185
          this.outerDiv.nativeElement.getElementsByTagName('input').item(0).focus();
        }, 0);
      }
    });
  }

  get userName(): string {
    return this.playService.attemptedUserName;
  }

  set userName(value: string) {
    this.playService.attemptedUserName = value;
  }

  get state(): ClientState {
    return this.playService.state;
  }

  get errorMessage(): string {
    return this.playService.errorMessage;
  }

  // Is this component visible
  isVisible(): boolean {
    return this.state <= ClientState.IDENTIFYING;
  }

  canConnect(): boolean {
    return this.state === ClientState.UNIDENTIFIED;
  }

  connect() {
    // Prevent 'Enter' key from the HTML input
    if (this.canConnect()) {
      this.playService.connect();
    }
  }
}
