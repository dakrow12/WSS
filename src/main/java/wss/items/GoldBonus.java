package wss.items;

import wss.player.Player;


public class GoldBonus extends Item {

	private int goldAmount;

	/**
	 * Calls the superclass constructor
	 * @param amount The amount of gold bonus given to player.
  	 * @param repeating Check if the item can be used multiple times.
    */
	public GoldBonus(int amount, boolean repeating)
	{
		super(repeating);
		this.goldAmount = amount;
	}

	/**
	 * Calls the superclass constructor
	 * @param amount The amount of gold bonus given to player.
    */
	public void activate(Player player) {
		player.addGold(goldAmount);
	}
}
