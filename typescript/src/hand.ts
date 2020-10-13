import Card from './card';

export default class Hand {
  cards: Card[];
  currentBet: number;

  constructor() {
    this.cards = [];
    this.currentBet = 0;
  }

  getValues() {
    return this.cards.reduce((accumulator, card) => {
      const values = card.getValues();
      if (accumulator.length === 0) return values;

      let newAccumulator: number[] = [];
      for (const prev of accumulator) {
        newAccumulator = newAccumulator.concat(values.map(v => v + prev));
      }
      return newAccumulator;
    }, []);
  }

  getHighestValue(): number {
    return Math.max(...this.getValues().filter(v => v <= 21));
  }

  canSplit() {
    return this.cards.length === 2 && this.cards[0].rank === this.cards[1].rank;
  }

  isBlackjack() {
    return (
      this.cards.length === 2 &&
      ((this.cards[0].rank === 'A' && this.cards[1].getValues()[0] === 10) ||
        (this.cards[1].rank === 'A' && this.cards[0].getValues()[0] === 10))
    );
  }

  isBust(): boolean {
    return this.getValues().every(v => v > 21);
  }
}
