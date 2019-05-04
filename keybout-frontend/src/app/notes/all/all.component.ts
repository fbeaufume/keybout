import {Component, OnInit} from '@angular/core';
import {Note} from '../note';
import {NoteService} from '../note.service';

@Component({
  selector: 'app-all',
  templateUrl: './all.component.html',
  styleUrls: ['./all.component.css']
})
export class AllComponent implements OnInit {

  notes: Note[];

  constructor(private noteService: NoteService) {
  }

  ngOnInit() {
    this.getNotes();
  }

  getNotes(): void {
    this.noteService.getNotes().subscribe(notes => this.notes = notes);
  }

  addNote(message: string): void {
    message = message.trim();
    if (!message) {
      return;
    }
    this.noteService.addNote({message} as unknown as Note)
      .subscribe(note => {
        this.notes.push(note);
      });
  }

  deleteNote(note: Note): void {
    this.notes = this.notes.filter(n => n !== note);
    this.noteService.deleteNote(note).subscribe();
  }
}
