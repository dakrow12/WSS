package wss.items;

import wss.player.Player;

public abstract class Item {
	private boolean repeating;

	// Constructor to initialize the repeating status
	protected Item(boolean repeating) { // Use protected if only subclasses should call it directly
		this.repeating = repeating;
	}

	/**
	 * Activates the item's effect on the player.
	 * @param player The player activating the item.
	 */
	public abstract void activate(Player player);

	/**
	 * Checks if this item is repeating (can be used multiple times).
	 * @return true if the item is repeating, false otherwise.
	 */
	public boolean isRepeating() {
		return repeating;
	}

	public void reset()
	{

	}
}