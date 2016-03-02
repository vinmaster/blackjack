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

	def create_game(name) do
		GenServer.call(:game_server, {:create_game, name})
	end

	def get_games() do
		GenServer.call(:game_server, {:get_games})
	end

	def get_game_by_name(name) when is_binary(name) do
		GenServer.call(:game_server, {:get_game_by_name, name})
	end

	def join_server(node) do
		Node.connect(node)
		# server_pid = :global.whereis_name(:server)
		# send(dealer_pid, {:joined})
	end

	def register_player(game_name, player_name) do
		game_pid = GenServer.call(:game_server, {:get_game_by_name, game_name})
		Game.add_player(game_pid, player_name)
	end

	# Server Callbacks

	def init(_) do
		:global.register_name(:server, self)
		{:ok, Map.new}
	end

	def handle_call({:create_game, name}, _from, state) do
		new_state = state
		# Return nil if already exists
		response = case Map.get(state, name, nil) do
			nil ->
				{:ok, pid} = Game.start_link
				Process.monitor(pid)
				new_state = Map.put(state, name, pid)
				:ok
			_ -> :error
		end
		{:reply, response, new_state}
	end

	def handle_call({:get_game_by_name, name}, _from, state) do
		response = case Map.get(state, name, nil) do
			nil -> :error
			pid -> pid
		end
		{:reply, response, state}
	end

	def handle_call({:get_games}, _from, state) do
		{:reply, state, state}
	end

	def handle_info({:DOWN, _ref, :process, pid, _reason}, state) do
    new_state = delete_game_by_pid(pid, state)
    {:noreply, new_state}
  end

	defp delete_game_by_pid(pid, state) do
		{name, _} = List.first(Enum.filter(state, fn({_, game_pid}) -> game_pid == pid end))
		Map.delete(state, name)
	end
end
