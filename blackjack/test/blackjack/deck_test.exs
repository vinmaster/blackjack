defmodule Blackjack.DeckTest do
  use ExUnit.Case, async: true
  alias Blackjack.Deck, as: Deck

  setup do
    {:ok, deck_pid} = Deck.start_link
    {:ok, pid: deck_pid}
  end

  test "peek", %{pid: pid} do
    assert Deck.count(pid) == 52
    assert Deck.peek(pid) != nil
    assert Deck.count(pid) == 52
  end

  test "new", %{pid: pid} do
    deck = Deck.get(pid)
    assert Enum.at(deck, 0)  == {"Spades", "A"}
    assert Enum.at(deck, 51) == {"Clubs", 2}
  end

  test "shuffle", %{pid: pid} do
    :random.seed(:erlang.unique_integer)
    deck1 = Deck.get(pid)
    deck2 = Deck.shuffle(pid)
    deck3 = Deck.shuffle(pid)
    assert deck1 != deck2
    assert deck2 != deck3
    assert deck1 != deck3
    assert deck3 == Deck.get(pid)
    assert length(deck2) == 52
  end

  test "draw", %{pid: pid} do
    deck = Deck.get(pid)
    assert Deck.count(pid) == 52
    card = Deck.draw(pid)
    assert card == {"Spades", "A"}
    assert deck != Deck.get(pid)
    assert Deck.count(pid) == 51
  end
end
