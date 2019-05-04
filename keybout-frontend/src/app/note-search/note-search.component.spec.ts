import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NoteSearchComponent } from './note-search.component';

describe('NoteSearchComponent', () => {
  let component: NoteSearchComponent;
  let fixture: ComponentFixture<NoteSearchComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NoteSearchComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NoteSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
