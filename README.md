# Blackjack

Implementation of [Blackjack](https://en.wikipedia.org/wiki/Blackjack) in different languages

## Requirements

- Determine dealing shoe, or the device used to hold multiple decks of playing cards. Usually 1 - 8 decks.
- Determine initial in-game money. e.g. $500.
- Rules:
	- If the player is dealt an Ace and a ten-value card (called a "blackjack" or "natural"), and the dealer does not, the player wins and usually receives a bonus.
	- If the player exceeds a sum of 21 ("busts"), the player loses, even if the dealer also exceeds 21.
	- If the dealer exceeds 21 ("busts") and the player does not, the player wins.
	- If the player attains a final sum higher than the dealer and does not bust, the player wins.
	- If both dealer and player receive a blackjack or any other hands with the same sum called a "push", no one wins.

1. The Dealer will ask for the Player's first name upon startup.
2. The Player is given initial in-game betting money ('Cash') to start with.
3. Round start with the user places their bet (constrained by a minimum bet of $20 and a maximum of their current available Cash).
4. four cards from the deck are randomly distributed to both the Player and the Dealer in alternating order.
At this point, both the Player and the Dealer have two cards each.
The faces of both cards of the Player are visible, while only one card of the Dealer is visible to the Player.

	* Checks for blackjack. If Player has blackjack, check Dealer for blackjack. If both have blackjack, it is a push (tie). If only Player has blackjack, then Player wins automatically. Payout is 3:2 (150%)

5. The Player chooses one of three actions for the current hand:
	* Hit
	* Stand
	* Double down
	* Split
	* Quit
6. The Player's bet is then deducted from their Cash and placed in the betting box, displayed on-screen.
7. Players go first in the round. They can:
	* Hit  
		The Dealer deals the Player a single card from the deck.
		* If the Player's hand then totals over 21, he 'busts'. The Player loses their bet.
		* If the Player's hand is 21 or under, they can continue hit or stand.
	* Stand
		Wait for the Dealer.
	* Double Down  
		The player is allowed to increase the initial bet up to 100% in exchange for committing to stand after receiving exactly one more card. Add the additional bet to the betting box.
	* Split  
		*This action should only be available if the Player has exactly two cards of equal rank.*
		The Player's hand is split into two hands, each with their own totals.
		Add same bet to the new hand. Add a card to make hand have two cards. If these new hands have equal rank again, Player can split again.
8. Deader goes last in the round.
		* If Player busts before, they instantly loses regardless what the Dealer has.
		* The Dealer deals himself cards one-by-one until his hand totals 17 or greater and stands.
		* If the Dealer busts (with a hand totaling greater than 21), the Player is declared the winner. Player gets the current bet back plus that amount for winning.
		* If the Dealer hand is smaller than the Player, the Player is determined the winner of the round. Player gets the current bet back plus that amount for winning.
8. Start next round.

The game ends when the Player runs out of cash or quits.