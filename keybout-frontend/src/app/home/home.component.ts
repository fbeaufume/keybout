import {Component, HostListener} from '@angular/core';
import {Router, RouterLink} from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  standalone: true,
  imports: [RouterLink]
})
export class HomeComponent {

  constructor(private router: Router) {
  }

  // The Enter key can be used to go to the connect screen
  @HostListener('document:keyup.enter', ['$event'])
  onEnterKey(event: KeyboardEvent) {
    event.preventDefault();

    this.router.navigate(['/play']);
  }
}
