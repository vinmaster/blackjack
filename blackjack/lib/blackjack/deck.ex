defmodule Blackjack.Deck do
  @moduledoc """
    A deck with a set of 52 cards. Using `Agent`.
  """

  # Start this Agent and storing the deck. Used for `new`.
  def start_link do
    deck = new
    Agent.start_link(fn -> deck end)
  end

  @doc """
    Create a new deck and returns pid and a list of tuples, in sorted order.
  """
  def new do
    for suit <- ~w(Spades Hearts Diamonds Clubs),
      face <- ["A", "K", "Q", "J", 10, 9, 8, 7, 6, 5, 4, 3, 2],
      do: {suit, face}
  end

  @doc """
    Return the top card for the deck with given `pid`.
  """
  def peek(pid) do
    Agent.get(pid, fn [head|_tail] -> head end)
  end

  @doc """
    Return the number of cards in the deck with given `pid`.
  """
  def count(pid) do
    Agent.get(pid, fn deck -> Enum.count(deck) end)
  end

  @doc """
    Delete this deck with given `pid` by stopping this Agent.
  """
  def delete(pid) do
    Agent.stop(pid)
  end

  @doc """
    Get deck with given `pid`.
  """
  def get(pid) do
    Agent.get(pid, fn deck -> deck end)
  end

  @doc """
    Given a `pid` for a deck, shuffle that deck.
  """
  def shuffle(pid) do
    case Agent.update(pid, fn deck -> Enum.shuffle(deck) end) do
      :ok -> get(pid)
    end
  end

  @doc """
    Given a `pid` for a deck, draw a card from the top.
  """
  def draw(pid) do
    Agent.get_and_update(pid, fn [head|tail] -> {head, tail} end)
  end
end
