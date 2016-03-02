defmodule Blackjack.GameServer do
	use GenServer
	alias Blackjack.Game, as: Game

	# CLient API

	@doc """
		Starts the registry.
	"""
	def start_link() do
		GenServer.start_link(__MODULE__, :ok, name: :game_server)
	end

	def get_rooms() do
		GenServer.call(:game_server, {:get_rooms})
	end

	def get_room_by_name(name) when is_binary(name) do
		GenServer.call(:game_server, {:get_room_by_name, name})
	end

	def join_game(node, name) do
		Node.connect(node)
		# server_pid = :global.whereis_name(:server)
		GenServer.call(:game_server, {:get_room_by_name, name})
		# send(dealer_pid, {:joined})
	end

	def register_player(game_name, player_name) do
		game_pid = GenServer.call(:game_server, {:get_room_by_name, game_name})
		Game.add_player(game_pid, player_name)
	end

	# Server Callbacks

	def init(_) do
		:global.register_name(:server, self)
		{:ok, Map.new}
	end

	def handle_call({:get_room_by_name, name}, _from, state) do
		pid = case Map.get(state, name, nil) do
			nil -> elem(Game.start_link, 1)
			pid -> pid
		end
		new_state = Map.put(state, name, pid)
		{:reply, pid, new_state}
	end

	def handle_call({:get_rooms}, _from, state) do
		{:reply, state, state}
	end

	def handle_info({:DOWN, _ref, :process, pid, _reason}, state) do
    new_state = delete_room_by_pid(pid, state)
    {:noreply, new_state}
  end

	defp delete_room_by_pid(pid, state) do
	end
end
