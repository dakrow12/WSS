package wss.items;

import wss.player.Player;


public class WaterBonus extends Item {
	private int waterAmount;
	protected WaterBonus(int amount, boolean repeating) {
		super(repeating);
		this.waterAmount = amount;
	}

	@Override
	public void activate(Player player) {
		player.increaseWater(waterAmount);
	}
}
