import { Component, OnInit } from '@angular/core';

import { Observable, Subject } from 'rxjs';

import {
  debounceTime, distinctUntilChanged, switchMap
} from 'rxjs/operators';

import { Note } from '../note';
import { NoteService } from '../note.service';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: [ './search.component.css' ]
})
export class SearchComponent implements OnInit {
  notes$: Observable<Note[]>;
  private searchTerms = new Subject<string>();

  constructor(private noteService: NoteService) {}

  // Push a search term into the observable stream.
  search(term: string): void {
    this.searchTerms.next(term);
  }

  ngOnInit(): void {
    this.notes$ = this.searchTerms.pipe(
      // Wait 300ms after each keystroke before considering the term
      debounceTime(300),

      // Ignore new term if same as previous term
      distinctUntilChanged(),

      // Switch to new search observable each time the term changes
      switchMap((term: string) => this.noteService.searchNotes(term)),
    );
  }
}
