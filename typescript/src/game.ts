import Card from './card';
import Hand from './hand';
import Player from './player';
import Util from './util';

export default class Game {
  static STARTING_CASH = 500;
  static NUM_OF_DECKS = 1;
  static round: number;
  static shoe: Card[];
  static players: Player[];
  static dealer: Player;

  static async start() {
    this.round = 1;
    this.shoe = [];
    this.players = [];

    this.dealer = new Player('Dealer', 0, true);
    this.players.push(this.dealer);
    await this.initPlayers();
    this.initCards();
    await this.runGameLoop();
  }

  static async initPlayers() {
    let name = await Util.questionAsync('what is your name? (Player): ', 'Player');

    const player = this.addPlayer(name);
    console.log(`${player.name} starts with $${player.cash}`);
  }

  static initCards() {
    console.log('Shuffling a new deck');
    for (let deck = 0; deck < this.NUM_OF_DECKS; deck++) {
      for (const suit of Card.SUITS) {
        for (const rank of Card.RANKS) {
          this.shoe.push(new Card(suit, rank));
        }
      }
    }
    Util.shuffleArray(this.shoe);
  }

  static async runGameLoop() {
    let quit = false;
    while (!quit) {
      console.log(`-------------------- Start round #${this.round} --------------------`);
      await this.placeBets();
      this.dealCards();
      // Players goes first
      for (const player of this.players.filter(p => !p.isDealer)) {
        for (let i = 0; i < player.hands.length; i++) {
          const hand = player.hands[i];

          let done = false;
          if (hand.isBlackjack()) {
            done = true;
          }

          while (!done && !quit) {
            this.printTable();
            let choices = `Choices for hand #${i + 1}:
  Hit (1 or h)
  Stand (2 or s)
  Double Down (3 or d)
`;
            if (hand.canSplit()) {
              choices += '  Split (4 or sp)\n';
            }
            choices += '  Quit (0 or q)\n';
            choices += 'Choose one (s): ';
            let answer = await Util.questionAsync(choices, 's');

            switch (answer) {
              case '1':
              case 'h': {
                hand.cards.push(this.drawNewCard());
                break;
              }
              case '2':
              case 's':
                done = true;
                break;
              case '3':
              case 'd': {
                player.cash -= hand.currentBet;
                hand.currentBet *= 2;
                hand.cards.push(this.drawNewCard());
                done = true;
              }
              case '4':
              case 'sp': {
                if (!hand.canSplit) {
                  console.log('Cannot split this hand');
                  break;
                }
                // Create new hand with bet and split current hand
                const newHand = new Hand();
                newHand.currentBet = hand.currentBet;
                player.cash -= newHand.currentBet;
                player.hands.push(newHand);
                newHand.cards.push(hand.cards.pop());
                // Draw new card for this hand and new hand
                hand.cards.push(this.drawNewCard());
                newHand.cards.push(this.drawNewCard());
                done = false;
                break;
              }
              case '0':
              case 'q':
                quit = true;
                break;
              default:
                done = false;
                break;
            }
            if (hand.isBust()) done = true;
          }
        }
      }
      // Dealer goes last
      while (this.dealer.hands[0].getHighestValue() < 17 && !this.dealer.hands[0].isBust()) {
        console.log('Dealer hits');
        this.dealer.hands[0].cards.push(this.drawNewCard());
      }
      this.printTable({ showHidden: true });
      for (const player of this.players.filter(p => !p.isDealer)) {
        for (let i = 0; i < player.hands.length; i++) {
          const hand = player.hands[i];
          if (hand.isBlackjack()) {
            console.log(`Hand #${i + 1} is Blackjack! Winning $${hand.currentBet * 1.5}`);
            player.cash += hand.currentBet * 2.5;
          } else if (hand.isBust()) {
            console.log(`Hand #${i + 1} is bust. Losing $${hand.currentBet}`);
          } else if (this.dealer.hands[0].isBust()) {
            console.log(`Dealer busts. Winning $${hand.currentBet}`);
            player.cash += hand.currentBet * 2;
          } else if (this.dealer.hands[0].getHighestValue() < hand.getHighestValue()) {
            console.log(`Hand #${i + 1} Dealer loses. Winning $${hand.currentBet}`);
            player.cash += hand.currentBet * 2;
          } else if (this.dealer.hands[0].getHighestValue() > hand.getHighestValue()) {
            console.log(`Hand #${i + 1} Dealer wins. Losing $${hand.currentBet}`);
          } else if (this.dealer.hands[0].getHighestValue() === hand.getHighestValue()) {
            console.log(`Hand #${i + 1} It is a push. $${hand.currentBet} is returned`);
            player.cash += hand.currentBet;
          }
        }
      }
      // Clear hands
      for (const player of this.players) {
        player.hands = [];
        if (player.cash <= 0 && !player.isDealer) {
          console.log(`${player.name} is out of money`);
          quit = true;
        }
      }
      this.round++;
    }
    console.log(`-------------------- Exit --------------------`);
  }

  static printTable({ showHidden = false } = {}) {
    for (const player of this.players) {
      player.print(showHidden);
      for (let i = 0; i < player.hands.length; i++) {
        const hand = player.hands[i];
        if (!player.isDealer || showHidden) {
          console.log(`Hand #${i + 1}: ${hand.getValues()}`);
        }
      }
    }
    console.log();
  }

  static async placeBets() {
    for (const player of this.players) {
      await player.getBet();
    }
  }

  static dealCards() {
    for (let i = 0; i < this.players.length * 2; i++) {
      const player = this.players[i % this.players.length];
      if (player.hands.length === 0) {
        player.hands.push(new Hand());
      }
      player.hands[0].cards.push(this.drawNewCard());
    }
    // this.players[1].hands[0].cards[0] = new Card('Clubs', 'A');
    // this.players[1].hands[0].cards[1] = new Card('Hearts', 'A');
  }

  static drawNewCard(): Card {
    if (this.shoe.length === 0) this.initCards();
    return this.shoe.shift();
  }

  static addPlayer(name: string) {
    const player = new Player(name, this.STARTING_CASH);
    this.players.push(player);
    return player;
  }
}
