defmodule DeckTest do
  use ExUnit.Case

  test "new" do
    deck = Deck.new
    assert Enum.at(deck, 0)  == {"Spades", "A"}
    assert Enum.at(deck, 51) == {"Clubs", 2}
  end

  test "shuffle" do
    :random.seed(:erlang.now)
    deck = Deck.shuffle
    assert Deck.shuffle != deck
    assert length(Deck.shuffle) == 52
  end

  test "deal" do
    players = [{"tim", []}, {"jen", []}, {"mac", []}, {"kai", []}]
    deck = Deck.new
    players = Deck.deal(deck, players, fn (card, {name, cards}) -> {name, cards ++ [card]} end)
    assert Enum.at(players, 0) == {"tim", [{"Spades", "A"}, {"Spades", 10}, {"Spades", 6}, {"Spades", 2}, {"Hearts", "J"}, {"Hearts", 7}, {"Hearts", 3}, {"Diamonds", "Q"}, {"Diamonds", 8}, {"Diamonds", 4}, {"Clubs", "K"}, {"Clubs", 9}, {"Clubs", 5}]}
    assert Enum.at(players, 1) == {"jen", [{"Spades", "K"}, {"Spades", 9}, {"Spades", 5}, {"Hearts", "A"}, {"Hearts", 10}, {"Hearts", 6}, {"Hearts", 2}, {"Diamonds", "J"}, {"Diamonds", 7}, {"Diamonds", 3}, {"Clubs", "Q"}, {"Clubs", 8}, {"Clubs", 4}]}
    assert Enum.at(players, 2) == {"mac", [{"Spades", "Q"}, {"Spades", 8}, {"Spades", 4}, {"Hearts", "K"}, {"Hearts", 9}, {"Hearts", 5}, {"Diamonds", "A"}, {"Diamonds", 10}, {"Diamonds", 6}, {"Diamonds", 2}, {"Clubs", "J"}, {"Clubs", 7}, {"Clubs", 3}]}
    assert Enum.at(players, 3) == {"kai", [{"Spades", "J"}, {"Spades", 7}, {"Spades", 3}, {"Hearts", "Q"}, {"Hearts", 8}, {"Hearts", 4}, {"Diamonds", "K"}, {"Diamonds", 9}, {"Diamonds", 5}, {"Clubs", "A"}, {"Clubs", 10}, {"Clubs", 6}, {"Clubs", 2}]}
  end
end

