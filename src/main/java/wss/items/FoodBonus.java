package wss.items;

import wss.player.Player;

public class FoodBonus extends Item {
    private int foodAmount;


    /**
	 * Calls the superclass constructor
	 * @param amount The amount of food bonus given to player.
  	 * @param repeating Check if the item can be used multiple times.
    */
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
