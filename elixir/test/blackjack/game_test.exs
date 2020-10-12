defmodule Blackjack.GameTest do
  use ExUnit.Case, async: true
  alias Blackjack.Game, as: Game
  alias Blackjack.Player, as: Player

  setup do
    {:ok, game_pid} = Game.start_link
    {:ok, pid: game_pid}
  end

  test "get_players", %{pid: pid} do
    assert [] == Game.get_players(pid)
  end

  test "add_player", %{pid: pid} do
    assert [] == Game.get_players(pid)
    player = %Player{name: "test"}
    Game.add_player(pid, player)
    assert [player] == Game.get_players(pid)
  end

  test "get_deck", %{pid: pid} do
    assert {"Spades", "A"} == List.first(Game.get_deck(pid))
  end

  test "shuffle_deck", %{pid: pid} do
    deck = Game.get_deck(pid)
    Game.shuffle_deck(pid)
    assert deck != Game.get_deck(pid)
  end

  test "deal", %{pid: pid} do
    assert [] == Game.get_players(pid)
    Game.add_player(pid, %Player{name: "first player"})
    Game.add_player(pid, %Player{name: "second player"})
    Game.deal(pid)
    players = Game.get_players(pid)
    assert [{"Spades", "A"}, {"Spades", "K"}] == List.first(players).cards
    assert [{"Spades", "Q"}, {"Spades", "J"}] == List.last(players).cards
  end

  test "hit", %{pid: pid} do
    first_player = %Player{name: "first player"}
    Game.add_player(pid, first_player)
    Game.add_player(pid, %Player{name: "second player"})
    Game.deal(pid)
    Game.hit(pid, first_player)
    players = Game.get_players(pid)
    assert [{"Spades", "A"}, {"Spades", "K"}, {"Spades", 10}] == List.first(players).cards
  end
end
