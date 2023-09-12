import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {Stats} from "../play/model";
import { NgIf, AsyncPipe } from '@angular/common';

@Component({
    selector: 'app-stats',
    templateUrl: './stats.component.html',
    standalone: true,
    imports: [NgIf, AsyncPipe]
})
export class StatsComponent implements OnInit {

  constructor(private readonly http: HttpClient) {
  }

  readonly reload$ = new BehaviorSubject(undefined);

  readonly stats$: Observable<Stats> = this.reload$.pipe(switchMap(() => this.http.get<Stats>('/api/stats')));

  ngOnInit() {
  }

  reload() {
    this.reload$.next(undefined);
  }
}
