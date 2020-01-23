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
  capture: 'Capture',
  race: 'Race'
};

export const Languages = ['en', 'fr'];

export const LanguageLabels = {
  en: 'English',
  fr: 'French'
};

export const WordEffects = ['none', 'hidden', 'anagram'];

export const WordEffectLabels = {
  none: {inForm: 'None', inList: 'No'},
  hidden: {inForm: 'Hidden', inList: 'Hidden'},
  anagram: {inForm: 'Anagram', inList: 'Anagram'}
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
  rounds: number;
  language: string;
  wordsCount: number;
  wordsLength: string;
  wordsEffect: string;
  players: string[];
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
  wpm: number;
  awards: number;
  awardsNames: string[];
}
