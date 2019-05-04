import {Injectable} from '@angular/core';
import {Note} from './note';
import {Observable, of} from 'rxjs';
import {catchError, tap} from 'rxjs/operators';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {MessageService} from './message.service';

const httpOptions = {
  headers: new HttpHeaders({'Content-Type': 'application/json'})
};

@Injectable({
  providedIn: 'root'
})
export class NoteService {

  private notesUrl = '/api/notes';

  constructor(
    private http: HttpClient,
    private messageService: MessageService) {
  }

  getNotes(): Observable<Note[]> {
    return this.http.get<Note[]>(this.notesUrl).pipe(
      tap(_ => this.log('fetched notes')),
      catchError(this.handleError<Note[]>('getNotes', []))
    );
  }

  getNote(id: number): Observable<Note> {
    const url = `${this.notesUrl}/${id}`;
    return this.http.get<Note>(url).pipe(
      tap(_ => this.log(`fetched note id=${id}`)),
      catchError(this.handleError<Note>(`getNote id=${id}`))
    );
  }

  updateNote(note: Note): Observable<any> {
    return this.http.put(this.notesUrl, note, httpOptions).pipe(
      tap(_ => this.log(`updated note id=${note.id}`)),
      catchError(this.handleError<any>('updateNote'))
    );
  }

  addNote(note: Note): Observable<Note> {
    return this.http.post<Note>(this.notesUrl, note, httpOptions).pipe(
      tap((newNote: Note) => this.log(`added note id=${newNote.id}`)),
      catchError(this.handleError<Note>('addNote'))
    );
  }

  deleteNote(note: Note | number): Observable<Note> {
    const id = typeof note === 'number' ? note : note.id;
    const url = `${this.notesUrl}/${id}`;

    return this.http.delete<Note>(url, httpOptions).pipe(
      tap(_ => this.log(`deleted note id=${id}`)),
      catchError(this.handleError<Note>('deleteNote'))
    );
  }

  searchNotes(term: string): Observable<Note[]> {
    if (!term.trim()) {
      return of([]);
    }
    return this.http.get<Note[]>(`${this.notesUrl}/?message=${term}`).pipe(
      tap(_ => this.log(`found notes matching "${term}"`)),
      catchError(this.handleError<Note[]>('searchNotes', []))
    );
  }

  /**
   * Handle Http operation that failed.
   * Let the app continue.
   * @param operation - name of the operation that failed
   * @param result - optional value to return as the observable result
   */
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(error); // log to console instead

      this.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

  private log(message: string) {
    this.messageService.add(`NoteService: ${message}`);
  }
}
