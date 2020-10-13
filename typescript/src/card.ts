export default class Card {
  suit: string;
  rank: string;

  constructor(suit, rank) {
    this.suit = suit;
    this.rank = rank;
  }

  getValues() {
    const value = parseInt(this.rank, 10);
    if (!isNaN(value)) return [value];
    if (this.rank === 'A') {
      return [1, 11];
    } else {
      return [10];
    }
  }

  toString(): string {
    const symbol = Card.SUITS_SYMBOLS[Card.SUITS.indexOf(this.suit)];
    return `${symbol}${this.rank}`;
  }

  static get SUITS_SYMBOLS() {
    return ['♣', '♦', '♥', '♠'];
  }

  static get SUITS() {
    return ['Clubs', 'Diamonds', 'Hearts', 'Spades'];
  }
  static get RANKS() {
    return ['A', '2', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K'];
  }
}
