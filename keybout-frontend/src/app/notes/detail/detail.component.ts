import { Component, OnInit, Input } from '@angular/core';
import { Note } from '../note';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { NoteService } from '../note.service';

@Component({
  selector: 'app-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.css']
})
export class DetailComponent implements OnInit {

  @Input() note: Note;

  constructor(
    private route: ActivatedRoute,
    private noteService: NoteService,
    private location: Location
  ) { }

  ngOnInit(): void {
    this.getHero();
  }

  getHero(): void {
    const id = +this.route.snapshot.paramMap.get('id');
    this.noteService.getNote(id).subscribe(note => this.note = note);
  }

  save(): void {
    this.noteService.updateNote(this.note).subscribe(() => this.goBack());
  }

  goBack(): void {
    this.location.back();
  }
}
