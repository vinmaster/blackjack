defmodule Blackjack.GameServerTest do
  use ExUnit.Case, async: true
  alias Blackjack.GameServer, as: GameServer
  alias Blackjack.Game, as: Game
  alias Blackjack.Player, as: Player

  setup do
    GameServer.start_link
    :ok
  end

  test "get_rooms" do
    assert %{} == GameServer.get_rooms
  end

  test "get_room_by_name" do
    pid = GameServer.get_room_by_name("new")
    assert pid != nil
    game_pid = GameServer.get_rooms["new"]
    assert game_pid != :ok
  end

  test "join_game" do
    GameServer.join_game(node(), "new")
  end

  test "register_player" do
    GameServer.register_player("new", "player")
    game_pid = GameServer.get_room_by_name("new")
    assert Game.get_players(game_pid) == ["player"]
  end
end
