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

export interface Label {
  // The pascal case of the label, i.e. 'Hello'
  pascal: string;
  // The lower case of the label, i.e. 'hello
  lower: string;
}

export const GameModeLabels: Map<string, Label> = new Map<string, Label>([
  [GameMode.CAPTURE, {pascal: 'Capture', lower: 'capture'}],
  [GameMode.RACE, {pascal: 'Race', lower: 'race'}]]);

export enum GameStyle {
  REGULAR = 'regular',
  HIDDEN = 'hidden',
  ANAGRAM = 'anagram',
  CALCULUS = 'calculus'
}

export const GameStyles = [GameStyle.REGULAR, GameStyle.HIDDEN, GameStyle.ANAGRAM, GameStyle.CALCULUS];

export const GameStyleLabels: Map<string, Label> = new Map<string, Label>([
  [GameStyle.REGULAR, {pascal: 'Regular', lower: 'regular'}],
  [GameStyle.HIDDEN, {pascal: 'Hidden', lower: 'hidden'}],
  [GameStyle.ANAGRAM, {pascal: 'Anagram', lower: 'anagram'}],
  [GameStyle.CALCULUS, {pascal: 'Calculus', lower: 'calculus'}]]);

export enum Language {
  EN = 'en',
  FR = 'fr',
  NONE = 'none'
}

export const Languages = [Language.EN, Language.FR];

export const LanguageLabels: Map<string, Label> = new Map<string, Label>([
  [Language.EN, {pascal: 'English', lower: 'english'}],
  [Language.FR, {pascal: 'French', lower: 'french'}],
  [Language.NONE, {pascal: '-', lower: '-'}]]);

export enum Difficulty {
  EASY = 'easy',
  NORMAL = 'normal',
  HARD = 'hard'
}

export const Difficulties = [Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD];

export const DifficultyLabels: Map<string, Label> = new Map<string, Label>([
  [Difficulty.EASY, {pascal: 'Easy', lower: 'easy'}],
  [Difficulty.NORMAL, {pascal: 'Normal', lower: 'normal'}],
  [Difficulty.HARD, {pascal: 'Hard', lower: 'hard'}]]);

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

// A score at the end of a round
export class Score {
  userName: string;
  points: number;
  speed: number;
  awards: number;
  awardsNames: string[];
  progress: boolean;

  constructor(userName: string, points: number, speed: number, awards: number, awardsNames: string[], progress: boolean) {
    this.userName = userName;
    this.points = points;
    this.speed = speed;
    this.awards = awards;
    this.awardsNames = awardsNames;
    this.progress = progress;
  }
}

// The high scores for a given style, language and difficulty
export interface HighScoreGroup {
  scores: HighScore[];
  // The backend provides others attributes, but they are not used by the frontend
}

// A high score provided by the backend
export interface HighScore {
  userName: string;
  speed: number;
  // The backend provides others attributes, but they are not used by the frontend
}

// Some backend statistics
export interface Stats {
  users: Measure;
  declaredGames: Measure;
  runningGames: Measure;
  uptime: Uptime;
}

export interface Measure {
  current: number;
  max: number;
  total: number;
}

export interface Uptime {
  current: string;
  max: string;
  total: string;
}
