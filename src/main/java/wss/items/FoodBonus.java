package wss.items;

import wss.player.Player;

public class FoodBonus extends Item {
    private int foodAmount;

    public FoodBonus(int amount, boolean repeating) {
        super(repeating);
        this.foodAmount = amount;
    }

    @Override
    
    /**
	 * Activates the item's effect of giving food to the player.
	 * @param player The player activating the item.
	 */
    public void activate(Player player) {
        player.addFood(foodAmount);
    }
}
