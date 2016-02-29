defmodule Blackjack.GameServer do
	use GenServer

	# CLient API

	@doc """
		Starts the registry.
	"""
	def start_link() do
		GenServer.start_link(__MODULE__, :ok)
	end
end
