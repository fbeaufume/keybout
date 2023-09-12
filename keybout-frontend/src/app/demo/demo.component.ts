import {Component, OnInit} from '@angular/core';
import {PlayService} from '../play/play.service';
import {ClientState, Game} from '../play/model';
import {Router} from '@angular/router';

@Component({
    selector: 'app-demo',
    templateUrl: './demo.component.html',
    standalone: true
})
export class DemoComponent implements OnInit {

  game = new Game(0, 'Chuck Norris', 'capture', 'hidden',
    'en', 'normal', ['TheLegend27', 'Fatal1ty', 'Leeroy Jenkins']);

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
        difficulty: this.game.difficulty,
        creator: this.game.creator
      }]);
    this.redirect();
  }

  playDemo() {
    this.playService.game = this.game;
    this.playService.updateWords(
      [
        ['energy', '', 'e_ergy'],
        ['avoid', 'Fatal1ty', 'avo_d'],
        ['policy', 'TheLegend27', 'po_icy'],
        ['juror', '', '_uror'],
        ['crucial', 'Chuck Norris', 'cr_cia_'],
        ['await', 'Chuck Norris', 'awa_t'],
        ['writer', '', 'wri_er'],
        ['banking', '', 'ba_kin_'],
        ['frankly', '', 'fr_n_ly'],
        ['rhythm', '', 'rh_thm'],
        ['statue', '', 'statu_'],
        ['chest', '', 'che_t'],
        ['blind', '', 'blin_'],
        ['radical', 'Fatal1ty', 'r_di_al'],
        ['grand', 'TheLegend27', 'gr_nd'],
        ['though', '', 'tho_gh'],
        ['blame', 'TheLegend27', 'b_ame'],
        ['attorney', 'Fatal1ty', 'a_torne_'],
        ['formula', 'TheLegend27', 'f_r_ula'],
        ['thigh', '', 'th_gh'],
        ['highway', '', 'hi__way'],
        ['manual', '', 'man_al'],
        ['field', 'TheLegend27', 'fiel_'],
        ['prize', '', 'p_ize'],
        ['member', 'Fatal1ty', 'memb_r'],
        ['power', 'Chuck Norris', 'po_er'],
        ['patch', 'Chuck Norris', 'pa_ch'],
        ['backyard', '', 'ba_kyar_'],
        ['weigh', '', 'we_gh'],
        ['pregnant', '', 'preg_an_'],
        ['define', '', 'defin_'],
        ['enemy', '', 'ene_y'],
        ['onion', 'Chuck Norris', 'onio_'],
        ['valid', '', 'va_id'],
        ['borrow', 'TheLegend27', '_orrow'],
        ['please', '', 'ple_se'],
        ['arrival', '', 'arr_v_l'],
        ['strongly', 'Fatal1ty', 's_rongl_'],
        ['everyone', '', 'ev_ryo_e'],
        ['begin', '', '_egin']
      ]);
    this.playService.state = ClientState.RUNNING;
    this.redirect();
  }

  scoresDemo() {
    this.playService.game = this.game;
    this.playService.roundScores = [
      {userName: 'Chuck Norris', points: 14, speed: 23.34, awards: 3, awardsNames: [], progress: false},
      {userName: 'TheLegend27', points: 11, speed: 20.67, awards: 4, awardsNames: [], progress: false},
      {userName: 'Fatal1ty', points: 9, speed: 17.25, awards: 0, awardsNames: [], progress: false},
      {userName: 'Leeroy Jenkins', points: 6, speed: 12.16, awards: 0, awardsNames: [], progress: false}
    ];
    this.playService.updateRoundAwards();
    this.playService.gameScores = [
      {userName: 'Chuck Norris', points: 1, speed: 23.34, awards: 0, awardsNames: [], progress: false},
      {userName: 'TheLegend27', points: 0, speed: 20.67, awards: 0, awardsNames: [], progress: false},
      {userName: 'Fatal1ty', points: 0, speed: 17.25, awards: 0, awardsNames: [], progress: false},
      {userName: 'Leeroy Jenkins', points: 0, speed: 12.16, awards: 0, awardsNames: [], progress: false}
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
