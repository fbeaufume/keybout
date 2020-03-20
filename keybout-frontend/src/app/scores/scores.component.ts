import {Component} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';
import {GameStyle, GameStyleLabels, GameStyles, Language, LanguageLabels, Languages} from '../play/model';
import {PlayService} from '../play/play.service';

class ScoreTypeForm {
  // Available game styles
  styles = GameStyles;
  styleLabels = GameStyleLabels;

  // Selected game style
  style = this.styles[0];

  // Available game languages
  languages = Languages;
  languageLabels = LanguageLabels;

  // Selected lang
  language = this.languages[0];

  // Previously selected lang
  previousLanguage = this.language;

  styleChanged() {
    // Switching to a style without language
    if (this.style === GameStyle.CALCULUS) {
      this.previousLanguage = this.language;
      this.language = Language.NONE;
    }
    // Switching to a style with language
    if (this.style !== GameStyle.CALCULUS && this.language === Language.NONE) {
      this.language = this.previousLanguage;
    }
  }

  isLanguageDisabled() {
    return this.language === '-';
  }
}

@Component({
  selector: 'app-scores',
  templateUrl: './scores.component.html'
})
export class ScoresComponent {

  constructor(private readonly http: HttpClient, public playService: PlayService) {
  }

  scoreTypeForm = new ScoreTypeForm();

  readonly reload$ = new BehaviorSubject(undefined);

  readonly scores$ = this.reload$.pipe(
    switchMap(() => this.http.get('/api/scores', {
      params: {
        style: this.scoreTypeForm.style,
        language: this.scoreTypeForm.language
      }
    })),
    map(scores => {
      return this.transpose(scores);
    }));

  get userName(): string {
    return this.playService.userName;
  }

  styleChanged() {
    this.scoreTypeForm.styleChanged();
    this.reload();
  }

  reload() {
    this.reload$.next(undefined);
  }

  transpose(scores) {
    // Generic transposition of a matrix, e.g. [['A','B','C'],['a','b','c']] to [['A','a'],['B','b'],['C','c']],
    // where first 'map' iterates over the inner array (3 elements in the example) and
    // second 'map' iterates over the outer array (2 elements in the example)
    // return matrix[0].map((_, i) => matrix.map(row => row[i]));

    return scores[0].scores.map((_, i) => scores.map(row => ({
      userName: row.scores[i].userName,
      speed: row.scores[i].speed
    })));
  }
}
