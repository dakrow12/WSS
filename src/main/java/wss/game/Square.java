package wss.game;

import wss.items.Item;

public class Square {
    private String terrain;
    private int movementCost;
    private int waterCost;
    private int foodCost;
    private Item item;

    public Square(String terrain, int move, int water, int food) {
        this.terrain = terrain;
        this.movementCost = move;
        this.waterCost = water;
        this.foodCost = food;
        this.item = null;
    }

    public int getMovementCost() { return movementCost; }
    public int getWaterCost() { return waterCost; }
    public int getFoodCost() { return foodCost; }

    public void setItem(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public boolean hasFoodBonus() {
        return item != null && item instanceof wss.items.FoodBonus;
    }
}
