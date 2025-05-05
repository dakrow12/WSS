package wss.items;

import wss.player.Player;


public class WaterBonus extends Item {
	private int waterAmount;

	/**
	 * Calls the superclass constructor
	 * @param amount The amount of water bonus given to player.
  	 * @param repeating Check if the item can be used multiple times.
         */
	protected WaterBonus(int amount, boolean repeating) {
		super(repeating);
		this.waterAmount = amount;
	}

	@Override

	/**
	 * Calls the superclass constructor
	 * @param player The player activating the item.
         */
	public void activate(Player player) {
		player.addWater(waterAmount);
	}
}
