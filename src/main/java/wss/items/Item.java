package wss.items;

import wss.player.Player
	
public abstract class Item {
	private boolean repeating;

	public abstract void activate(Player player);
}
