import {Component, OnInit} from '@angular/core';
import {PlayService} from '../play/play.service';
import {ClientState, Game} from '../play/model';
import {Router} from '@angular/router';

@Component({
  selector: 'app-demo',
  templateUrl: './demo.component.html'
})
export class DemoComponent implements OnInit {

  game = new Game(0, 'Chuck Norris', 'capture', 'hidden', 2,
    'en', 10, 'standard', ['TheLegend27', 'Fatal1ty', 'Leeroy Jenkins']);

  constructor(private playService: PlayService, private router: Router) {
  }

  ngOnInit() {
  }

  gamesDemo() {
    this.playService.updateGamesList(
      [{
        id: this.game.id,
        players: this.game.players,
        mode: this.game.mode,
        style: this.game.style,
        language: this.game.language,
        wordsLength: this.game.wordsLength,
        creator: this.game.creator,
        rounds: this.game.rounds,
        wordsCount: this.game.wordsCount
      }]);
    this.redirect();
  }

  playDemo() {
    this.playService.game = this.game;
    this.playService.updateWords(
      {
        await: ['Chuck Norris', 'awa_t'],
        avoid: ['Fatal1ty', 'avo_d'],
        banking: ['', 'ba_kin_'],
        juror: ['', '_uror'],
        crucial: ['Chuck Norris', 'cr_cia_'],
        energy: ['', 'e_ergy'],
        writer: ['', 'wri_er'],
        policy: ['TheLegend27', 'po_icy'],
        frankly: ['', 'fr_n_ly'],
        formula: ['TheLegend27', 'f_r_ula'],
        attorney: ['Fatal1ty', 'a_torne_'],
        chest: ['', 'che_t'],
        blind: ['', 'blin_'],
        radical: ['Fatal1ty', 'r_di_al'],
        grand: ['TheLegend27', 'gr_nd'],
        though: ['', 'tho_gh'],
        blame: ['TheLegend27', 'b_ame'],
        statue: ['', 'statu_'],
        rhythm: ['', 'rh_thm'],
        thigh: ['', 'th_gh'],
        field: ['TheLegend27', 'fiel_'],
        manual: ['', 'man_al'],
        highway: ['', 'hi__way'],
        prize: ['', 'p_ize'],
        member: ['Fatal1ty', 'memb_r'],
        power: ['Chuck Norris', 'po_er'],
        patch: ['Chuck Norris', 'pa_ch'],
        backyard: ['', 'ba_kyar_'],
        weigh: ['', 'we_gh'],
        pregnant: ['', 'preg_an_'],
        define: ['', 'defin_'],
        enemy: ['TheLegend27', 'ene_y'],
        onion: ['Chuck Norris', 'onio_'],
        valid: ['', 'va_id'],
        borrow: ['TheLegend27', '_orrow'],
        please: ['', 'ple_se'],
        arrival: ['', 'arr_v_l'],
        strongly: ['Fatal1ty', 's_rongl_'],
        everyone: ['', 'ev_ryo_e'],
        begin: ['', '_egin']
      });
    this.playService.state = ClientState.RUNNING;
    this.redirect();
  }

  scoresDemo() {
    this.playService.game = this.game;
    this.playService.roundScores = [
      {userName: 'Chuck Norris', points: 14, wpm: 23.34, awards: 3, awardsNames: []},
      {userName: 'TheLegend27', points: 11, wpm: 20.67, awards: 4, awardsNames: []},
      {userName: 'Fatal1ty', points: 9, wpm: 17.25, awards: 0, awardsNames: []},
      {userName: 'Leeroy Jenkins', points: 6, wpm: 12.16, awards: 0, awardsNames: []}
    ];
    this.playService.updateRoundAwards();
    this.playService.gameScores = [
      {userName: 'Chuck Norris', points: 1, wpm: 23.34, awards: 0, awardsNames: []},
      {userName: 'TheLegend27', points: 0, wpm: 20.67, awards: 0, awardsNames: []},
      {userName: 'Fatal1ty', points: 0, wpm: 17.25, awards: 0, awardsNames: []},
      {userName: 'Leeroy Jenkins', points: 0, wpm: 12.16, awards: 0, awardsNames: []}
    ];
    this.playService.gameManager = this.game.creator;
    this.playService.gameOver = false;
    this.playService.state = ClientState.SCORES;
    this.redirect();
  }

  redirect() {
    this.router.navigate(['/play']);
  }
}
