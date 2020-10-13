import * as readline from 'readline';

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});

export default class Util {
  static questionAsync(questionText, defaultValue?: any): Promise<string> {
    return new Promise((resolve, reject) => {
      rl.question(questionText, input =>
        defaultValue && input.length === 0 ? resolve(defaultValue) : resolve(input)
      );
    });
  }

  static shuffleArray(array: any[]) {
    let i = array.length;
    while (i--) {
      const ri = Math.floor(Math.random() * (i + 1));
      [array[i], array[ri]] = [array[ri], array[i]];
    }
    return array;
  }
}
