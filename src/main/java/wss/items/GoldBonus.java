package wss.items;

import wss.player.Player;


public class GoldBonus extends Item {
	@Override

	private int goldAmount;

	public GoldBonus(int amount, boolean repeating)
	{
		super(repeating);
		this.goldAmount = amount;
	}
	public void activate(Player player) {
		player.increaseGold(goldAmount); 
	}
}
