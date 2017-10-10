defmodule Blackjack.GameServerTest do
  use ExUnit.Case, async: true
  alias Blackjack.GameServer, as: GameServer
  alias Blackjack.Game, as: Game
  alias Blackjack.Player, as: Player

  setup do
    GameServer.start_link
    :ok
  end

  test "get_games" do
    assert %{} == GameServer.get_games
  end

  test "create_game" do
    pid = GameServer.get_game_by_name("new")
    assert pid == :error
    pid = GameServer.create_game("new")
    assert pid != :error
    pid = GameServer.create_game("new")
    assert pid == :error
  end

  test "get_game_by_name" do
    pid = GameServer.get_game_by_name("new")
    assert pid == :error
    GameServer.create_game("new")
    new_pid = GameServer.get_game_by_name("new")
    assert is_pid(new_pid)
    assert new_pid != pid
  end

  test "join_server" do
    GameServer.join_server(node())
    # assert Node.list != []
  end

  test "add_player" do
    GameServer.create_game("new")
    GameServer.add_player("new", "player")
    game_pid = GameServer.get_game_by_name("new")
    assert Game.get_players(game_pid) == [%Player{cards: [], game_pid: game_pid, name: "player", node_id: :nonode@nohost}]
  end

  test "handle when game shuts down" do
    pid = GameServer.create_game("new")
    assert pid == :ok
    pid = GameServer.get_game_by_name("new")
    assert is_pid(pid)
    GenServer.stop(pid, :shutdown)
    assert GameServer.get_game_by_name("new") == :error
  end
end
