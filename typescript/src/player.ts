import Hand from './hand';
import Util from './util';

export default class Player {
  name: string;
  cash: number;
  isDealer: boolean;
  hands: Hand[];

  constructor(name: string, cash: number, isDealer = false) {
    this.name = name;
    this.cash = cash;
    this.isDealer = isDealer;
    this.hands = [];
  }

  async getBet(): Promise<number> {
    if (this.isDealer) return 0;

    let bet = 0;
    const min = 20;
    const max = this.cash;
    while (bet < min || bet > max) {
      let betStr = await Util.questionAsync(`Wager amount. Between $${min}~$${max} (20): `, 20);
      bet = parseInt(betStr, 10);
    }
    this.cash -= bet;
    if (this.hands.length === 0) this.hands.push(new Hand());
    this.hands[0].currentBet = bet;
    console.log(`${this.name} bets $${bet}`);
    return bet;
  }

  print(showHidden) {
    console.log(`${this.name}:`);
    if (!this.isDealer) console.log(`Cash: $${this.cash}`);

    for (const hand of this.hands) {
      const cardArray = hand.cards.map(c => c.toString());
      if (this.isDealer && !showHidden) {
        cardArray[0] = '?';
      }
      console.log(cardArray.join(' '));
    }
  }
}
