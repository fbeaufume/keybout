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
