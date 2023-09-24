import {Component} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';
import {GameStyle, GameStyleLabels, GameStyles, Language, LanguageLabels, Languages} from '../play/model';
import {PlayService} from '../play/play.service';
import { NgFor, NgIf, NgClass, AsyncPipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

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
    return this.language === Language.NONE;
  }
}

@Component({
    selector: 'app-scores',
    templateUrl: './scores.component.html',
    standalone: true,
    imports: [FormsModule, NgFor, NgIf, NgClass, AsyncPipe, DecimalPipe]
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

  // TODO FBE enable strict mode in tsconfig and add types in this method (such as HighScore)
  // Transpose a high scores matrix, i.e. convert:
  // [
  // 	{
  // 		"style": "REGULAR",
  // 		"language": "EN",
  // 		"difficulty": "EASY",
  // 		"scores": [
  // 			{
  // 				"userName": "John",
  // 				"speed": 43.047783
  // 			},
  // 			{
  // 				"userName": "Tom",
  // 				"speed": 27.728994
  // 			},
  //      ...
  // 		]
  // 	},
  // 	{
  // 		"style": "REGULAR",
  // 		"language": "EN",
  // 		"difficulty": "NORMAL",
  // 		"scores": [
  // 			{
  // 				"userName": "-",
  // 				"speed": 0
  // 			},
  // 			{
  // 				"userName": "-",
  // 				"speed": 0
  // 			},
  //      ...
  // 		]
  // 	},
  // 	{
  // 		"style": "REGULAR",
  // 		"language": "EN",
  // 		"difficulty": "HARD",
  // 		"scores": [
  // 			{
  // 				"userName": "-",
  // 				"speed": 0
  // 			},
  // 			{
  // 				"userName": "-",
  // 				"speed": 0
  // 			},
  //      ...
  // 		]
  // 	}
  // ]
  // into:
  // [
  // 	[
  // 		{
  // 			"userName": "John",
  // 			"speed": 43.047783
  // 		},
  // 		{
  // 			"userName": "-",
  // 			"speed": 0
  // 		},
  // 		{
  // 			"userName": "-",
  // 			"speed": 0
  // 		}
  // 	],
  // 	[
  // 		{
  // 			"userName": "Tom",
  // 			"speed": 27.728994
  // 		},
  // 		{
  // 			"userName": "-",
  // 			"speed": 0
  // 		},
  // 		{
  // 			"userName": "-",
  // 			"speed": 0
  // 		}
  // 	],
  //  ...
  // ]
  // The first 'map' method iterates over the inner array,
  // and the second 'map' method iterates over the outer array
  transpose(scores) {
    return scores[0].scores.map((_, i) => scores.map(row => ({
      userName: row.scores[i].userName,
      speed: row.scores[i].speed
    })));
  }
}
