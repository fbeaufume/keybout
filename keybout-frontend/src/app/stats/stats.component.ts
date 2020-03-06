import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject} from 'rxjs';
import {switchMap} from 'rxjs/operators';

@Component({
  selector: 'app-stats',
  templateUrl: './stats.component.html'
})
export class StatsComponent implements OnInit {

  constructor(private readonly http: HttpClient) {
  }

  readonly reload$ = new BehaviorSubject(undefined);

  readonly stats$ = this.reload$.pipe(switchMap(() => this.http.get('/api/stats')));

  ngOnInit() {
  }

  reload() {
    this.reload$.next(undefined);
  }
}
