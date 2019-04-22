import { Component, OnInit } from '@angular/core';
import { Note } from '../note';
import { NOTES } from '../mock-notes';

@Component({
  selector: 'app-notes',
  templateUrl: './notes.component.html',
  styleUrls: ['./notes.component.css']
})
export class NotesComponent implements OnInit {

  notes = NOTES;

  selectedNote: Note;

  constructor() { }

  ngOnInit() {
  }

  onSelect(note: Note):void {
    this.selectedNote = note;
  }
}
