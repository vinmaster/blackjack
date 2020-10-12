defmodule Blackjack.GameServer do
	use GenServer
	alias Blackjack.Game, as: Game
	alias Blackjack.Player, as: Player

	# CLient API

	def join_server(node) do
		Node.connect(node)
		# server_pid = :global.whereis_name(:server)
		# send(dealer_pid, {:joined})
	end

	def node_name() do
		Node.self()
	end

	@doc """
		Starts the registry.
	"""
	def start_link() do
		GenServer.start_link(__MODULE__, :ok, name: {:global, :game_server})
	end

	def create_game(name) when is_binary(name) do
		GenServer.call({:global, :game_server}, {:create_game, name})
	end

	def get_games() do
		GenServer.call({:global, :game_server}, {:get_games})
	end

	def get_game_by_name(name) when is_binary(name) do
		GenServer.call({:global, :game_server}, {:get_game_by_name, name})
	end

	def get_players(game_name) do
		game_pid = get_game_by_name(game_name)
		Game.get_players(game_pid)
	end

	def add_player(game_name, player_name) do
		game_pid = get_game_by_name(game_name)
		node_id = Node.self()
		Game.add_player(game_pid, %Player{name: player_name, game_pid: game_pid, node_id: node_id})
	end

	def get_deck(game_name) do
		game_pid = get_game_by_name(game_name)
		Game.get_deck(game_pid)
	end

	def shuffle_deck(game_name) do
		game_pid = get_game_by_name(game_name)
		Game.shuffle_deck(game_pid)
	end

	def deal(game_name) do
		game_pid = get_game_by_name(game_name)
		Game.deal(game_pid)
	end

	def hit(game_name) do
		game_pid = get_game_by_name(game_name)
		players = get_players(game_name)
		player = Enum.find(players, fn(p) -> p.node_id == Node.self() end)
		Game.hit(game_pid, player)
	end

	# Server Callbacks

	def init(_) do
		# :global.register_name(:server, self()) # start_link took care of register
		{:ok, Map.new}
	end

	def handle_call({:create_game, name}, _from, state) do
		new_state = state
		# Return nil if already exists
		{response, new_state} = case Map.get(state, name, nil) do
			nil ->
				{:ok, pid} = Game.start_link
				Process.monitor(pid)
				new_state = Map.put(state, name, pid)
				{:ok, new_state}
			_ -> {:error, new_state}
		end
		{:reply, response, new_state}
	end

	def handle_call({:get_games}, _from, state) do
		{:reply, state, state}
	end

	def handle_call({:get_game_by_name, name}, _from, state) do
		response = case Map.get(state, name, nil) do
			nil -> :error
			pid -> pid
		end
		{:reply, response, state}
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
