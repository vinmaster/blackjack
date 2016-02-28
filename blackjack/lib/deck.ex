defmodule Deck do
  @moduledoc """
    Create, shuffle, deal a set of 52 cards.
  """

  @doc """
    Returns a list of tuples, in sorted order.
  """
  def new do
    for suit <- ~w(Spades Hearts Diamonds Clubs),
       face <- ["A", "K", "Q", "J", 10, 9, 8, 7, 6, 5, 4, 3, 2],
       do: {suit, face}
  end

  @doc """
    Given a list of cards (a deck), reorder cards randomly.

    If no deck is given, then create a new one and shuffle that.
  """
  def shuffle(deck \\ new) do
    Enum.shuffle(deck)
  end

  @doc """
    Given a deck of cards, a list of players, and a deal function,
    call the deal function for each card for each player. The function
    should return the updated player.

    Returns the list of players.
  """
  def deal([card | rest_cards], [player | rest_players], deal_fn) do
    player = deal_fn.(card, player)
    deal(rest_cards, rest_players ++ [player], deal_fn)
  end
  def deal([], players, _), do: players
end

