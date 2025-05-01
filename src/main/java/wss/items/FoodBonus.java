package wss.items;

import wss.player.Player;

public class FoodBonus extends Item {
    private int foodAmount;

    public FoodBonus(int amount) {
        this.foodAmount = amount;
    }

    @Override
    public void activate(Player player) {
        player.addFood(foodAmount);
    }
}
