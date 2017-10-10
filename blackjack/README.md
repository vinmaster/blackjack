# Blackjack
> Using Elixir 1.5.2

## Quick Start

Running the program
```elixir
# To compile in REPL at anytime in debug mode
recompile()

# Specify session name
iex --sname player1

# Specify name with machine IP and debug mode
iex --name player1@192.168.1.1 --cookie key -S mix
iex --name player2@192.168.1.2 --cookie key -S mix

# Connect to machine
Blackjack.GameServer.join_server(:"player2@192.168.1.2")
# List nodes connected
Node.list
# Run code on other node
greet = fn() -> IO.puts("Hello from #{inspect(Node.self)}") end
Node.spawn(:"frank@127.0.0.1", greet)

# Start server
{:ok, server_pid} = Blackjack.GameServer.start_link()
Blackjack.GameServer.create_game("table")
Blackjack.GameServer.add_player("table", "player1")
dealer_pid = Blackjack.GameServer.get_game_by_name("table")
Blackjack.GameServer.deal("table")
Blackjack.GameServer.deal("hit")
```

Run unit tests
```
iex -S mix test --trace
```
