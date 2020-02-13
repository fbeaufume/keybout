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

export const GameStyles = ['regular', 'hidden', 'anagram'];

export const GameStyleLabels = {
  regular: {pascal: 'Regular', lower: 'regular'},
  hidden: {pascal: 'Hidden', lower: 'hidden'},
  anagram: {pascal: 'Anagram', lower: 'anagram'}
};

export const Languages = ['en', 'fr'];

export const LanguageLabels = {
  en: 'English',
  fr: 'French'
};

export const WordLengths = ['shortest', 'shorter', 'standard', 'longer', 'longest'];

export const WordLengthLabels = {
  shortest: 'Shortest',
  shorter: 'Shorter',
  standard: 'Standard',
  longer: 'Longer',
  longest: 'Longest'
};

export class Game {
  id: number;
  creator: string;
  mode: string;
  style: string;
  rounds: number;
  language: string;
  wordsCount: number;
  wordsLength: string;
  players: string[];

  constructor(id: number, creator: string, mode: string, style: string, rounds: number, language: string, wordsCount: number, wordsLength: string, players: string[]) {
    this.id = id;
    this.creator = creator;
    this.mode = mode;
    this.style = style;
    this.rounds = rounds;
    this.language = language;
    this.wordsCount = wordsCount;
    this.wordsLength = wordsLength;
    this.players = players;
  }
}

export class Word {
  label: string;
  userName: string;
  display: string;

  constructor(label: string, userName: string, display: string) {
    this.label = label;
    this.userName = userName;
    this.display = display;
  }

  getEffectiveDisplay() {
    if (this.userName === '') {
      return this.display;
    } else {
      return this.label;
    }
  }
}

export class Score {
  userName: string;
  points: number;
  speed: number;
  awards: number;
  awardsNames: string[];
}
