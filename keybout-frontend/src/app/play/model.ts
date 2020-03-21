export enum ClientState {
  UNIDENTIFIED, // Initial state, no used name accepted by the server yet
  IDENTIFYING, // Sending user name
  LOBBY, // User is identified and in the lobby getting the list of games
  CREATING, // User is creating a game
  CREATED, // User has created a game
  DELETING, // User is deleting a game
  JOINING, // User is joining a game
  JOINED, // User has joined a game
  LEAVING, // User is leaving a game
  STARTING_GAME, // User is starting a game, i.e. has clicked on Start button
  STARTED, // Display the countdown page
  RUNNING, // A game is running
  END_ROUND, // A game round has ended, display the View scores button
  SCORES, // Displaying the scores
  STARTING_ROUND, // User is starting the next round, i.e. has clicked on Start next round
  QUITTING// User is quitting a game that ended
}

export enum GameMode {
  CAPTURE = 'capture',
  RACE = 'race'
}

export const GameModes = [GameMode.CAPTURE, GameMode.RACE];

export const GameModeLabels = {
  capture: {pascal: 'Capture', lower: 'capture'},
  race: {pascal: 'Race', lower: 'race'}
};

export enum GameStyle {
  REGULAR = 'regular',
  HIDDEN = 'hidden',
  ANAGRAM = 'anagram',
  CALCULUS = 'calculus'
}

export const GameStyles = [GameStyle.REGULAR, GameStyle.HIDDEN, GameStyle.ANAGRAM, GameStyle.CALCULUS];

export const GameStyleLabels = {
  regular: {pascal: 'Regular', lower: 'regular'},
  hidden: {pascal: 'Hidden', lower: 'hidden'},
  anagram: {pascal: 'Anagram', lower: 'anagram'},
  calculus: {pascal: 'Calculus', lower: 'calculus'}
};

export enum Language {
  EN = 'en',
  FR = 'fr',
  NONE = 'none'
}

export const Languages = [Language.EN, Language.FR];

export const LanguageLabels = {
  en: {pascal: 'English', lower: 'english'},
  fr: {pascal: 'French', lower: 'french'},
  none: {pascal: '-', lower: '-'}
};

export const Difficulties = ['easy', 'normal', 'hard'];

export const DifficultyLabels = {
  easy: {pascal: 'Easy', lower: 'easy'},
  normal: {pascal: 'Normal', lower: 'normal'},
  hard: {pascal: 'Hard', lower: 'hard'},
};

export class Game {
  id: number;
  creator: string;
  mode: string;
  style: string;
  language: string;
  difficulty: string;
  players: string[];

  constructor(id: number, creator: string, mode: string, style: string, language: string, difficulty: string, players: string[]) {
    this.id = id;
    this.creator = creator;
    this.mode = mode;
    this.style = style;
    this.language = language;
    this.difficulty = difficulty;
    this.players = players;
  }
}

export class Word {
  value: string;
  userName: string;
  display: string;

  constructor(value: string, userName: string, display: string) {
    this.value = value;
    this.userName = userName;
    this.display = display;
  }

  getEffectiveDisplay() {
    if (this.userName === '') {
      return this.display;
    } else {
      return this.value;
    }
  }
}

export class Score {
  userName: string;
  points: number;
  speed: number;
  awards: number;
  awardsNames: string[];
  progress: boolean;
}
