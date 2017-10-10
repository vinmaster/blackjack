defmodule Blackjack.Game do
	use GenServer
	alias Blackjack.Deck, as: Deck

	# CLient API

	@doc """
		Starts the game process.
	"""
	def start_link() do
		GenServer.start(__MODULE__, :ok, [])
	end

	@doc """
		Get list of players with given game `pid`.

		Returns Map with keys :name, :cards
  """
	def get_players(pid) do
		GenServer.call(pid, {:get_players})
	end

	@doc """
		Add player to game with given game `pid`.
  """
	def add_player(pid, player) do
		GenServer.call(pid, {:add_player, player})
	end

	@doc """
		Get current deck with given game `pid`.

		Returns List of Tuple with {suit, rank}
  """
	def get_deck(pid) do
		GenServer.call(pid, {:get_deck})
	end

	@doc """
		Shuffle the deck for this game with given game `pid`
	"""
	def shuffle_deck(pid) do
		GenServer.cast(pid, {:shuffle_deck})
	end

	@doc """
		Deal 2 cards to each player in game with given game `pid`.
  """
	def deal(pid) do
		GenServer.call(pid, {:deal})
	end

	@doc """
		Deal one more card to player with given game `pid`.
  """
	def hit(pid, player) do
		GenServer.call(pid, {:hit, player})
	end

	def start_game(pid) do
		IO.puts "Node: #{inspect node()}"
		IO.puts "PID: #{inspect pid}"
		:global.register_name(:server, self())
	end

	def join_game(node, _pid) do
		Node.connect(node)
		dealer_pid = :global.whereis_name(:server)
		send(dealer_pid, {:joined})
	end

	# Server Callbacks

	def init(:ok) do
		{:ok, deck_pid} = Deck.start_link()
		{:ok, %{:players => [], :deck => deck_pid}}
	end

	def handle_cast({:shuffle_deck}, state) do
  	Deck.shuffle(state.deck)
  	{:noreply, state}
  end

	def handle_call({:get_players}, _from, state) do
		{:reply, state.players, state}
  end

  def handle_call({:add_player, player}, _from, state) do
  	new_players = state.players ++ [player]
  	{:reply, new_players, Map.put(state, :players, new_players)}
  end

  def handle_call({:get_deck}, _from, state) do
		{:reply, Deck.get(state.deck), state}
  end

  def handle_call({:deal}, _from, state) do
  	new_players = for player <- state.players do
  		new_cards = player.cards ++ [Deck.draw(state.deck), Deck.draw(state.deck)]
  		%{player | cards: new_cards}
  	end
  	new_state = Map.put(state, :players, new_players)
  	{:reply, new_players, new_state}
  end

  def handle_call({:hit, %{:name => name}}, _from, state) do
  	new_players = for player <- state.players do
  		new_cards = case player.name do
	  		^name -> player.cards ++ [Deck.draw(state.deck)]
	  		_ -> player.cards
	  	end
  		%{player | cards: new_cards}
  	end
		player = Enum.find(state.players, fn(p) -> p.name == name end)
		total_value = Enum.reduce(player.cards, 0, fn({_, value}, acc) ->
			case value do
				# TODO Update logic to evalute Ace
				"A" when acc <= 10 -> 11 + acc
				"A" -> 1 + acc
				v when v == "K" or v == "Q" or v == "J" -> 10 + acc
				v -> v + acc
			end
		end)
  	new_state = Map.put(state, :players, new_players)
  	{:reply, total_value, new_state}
  end
end
