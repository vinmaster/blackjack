## Build

```
bash build.sh
```

## Run

```
./app
```

OR

```
lein run
```

## Test

```
lein test
```

## Example
```
What is your name? (Player):
Welcome to Blackjack, Player!
Wager amount. Between 20.00~500.00 (20):
Dealer
? 3♣

Player
Cash: $480.00
10♥ 7♣

Choices for hand #0:
  Hit (1 or h)
  Stand (2 or s)
  Double Down (3 or d)
  Quit (0 or q)
Choose one (s): s
Dealer
2♥ 3♣ J♦ 2♠

Player
Cash: $480.00
10♥ 7♣

It is a push with Dealer. $20.00 is returned
----- End round -----

Wager amount. Between 20.00~500.00 (20):
Dealer
? A♥

Player
Cash: $480.00
3♥ 2♣

Choices for hand #0:
  Hit (1 or h)
  Stand (2 or s)
  Double Down (3 or d)
  Quit (0 or q)
Choose one (s): h
Dealer
? A♥

Player
Cash: $480.00
3♥ 2♣ 8♥

Choices for hand #0:
  Hit (1 or h)
  Stand (2 or s)
  Double Down (3 or d)
  Quit (0 or q)
Choose one (s): h
Dealer
8♠ A♥

Player
Cash: $480.00
3♥ 2♣ 8♥ 9♠

Hand: #0 has busted. Losing $20.00
----- End round -----

Wager amount. Between 20.00~480.00 (20):
Dealer
? A♦

Player
Cash: $460.00
5♠ 7♠

Choices for hand #0:
  Hit (1 or h)
  Stand (2 or s)
  Double Down (3 or d)
  Quit (0 or q)
Choose one (s): h
Dealer
Q♣ A♦

Player
Cash: $460.00
5♠ 7♠ Q♦

Hand: #0 has busted. Losing $20.00
----- End round -----

Wager amount. Between 20.00~460.00 (20): 450
Dealer
? K♥

Player
Cash: $10.00
Q♥ 6♣

Choices for hand #0:
  Hit (1 or h)
  Stand (2 or s)
  Double Down (3 or d)
  Quit (0 or q)
Choose one (s): s
Dealer
4♦ K♥ 8♦

Player
Cash: $10.00
Q♥ 6♣

Dealer busts. Winning $900.00
----- End round -----

Wager amount. Between 20.00~910.00 (20):
Dealer
? 4♣

Player
Cash: $890.00
K♠ 5♣

Choices for hand #0:
  Hit (1 or h)
  Stand (2 or s)
  Double Down (3 or d)
  Quit (0 or q)
Choose one (s): q
Dealer
4♠ 4♣ 7♥ 10♦

Player
Cash: $890.00
K♠ 5♣

Dealer busts. Winning $40.00
----- End round -----
```
