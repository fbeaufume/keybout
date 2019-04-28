import { Injectable } from '@angular/core';
import { Note } from './note';
import { NOTES } from './mock-notes';
import { Observable, of } from 'rxjs';
import { MessageService } from './message.service';

@Injectable({
  providedIn: 'root'
})
export class NoteService {

  constructor(private messageService: MessageService) {
  }

  getNotes(): Observable<Note[]> {
    this.messageService.add('NoteService: fetched notes');
    return of(NOTES);
  }

  getNote(id: number): Observable<Note> {
    this.messageService.add(`NoteService: fetched note id=${id}`);
    return of(NOTES.find(note => note.id === id));
  }
}
