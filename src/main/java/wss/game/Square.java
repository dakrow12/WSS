package wss.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import wss.items.Item;
import wss.items.FoodBonus;
import wss.player.Player;
import wss.trader.Trader;

public class Square {
    private String terrain;
    private int movementCost;
    private int waterCost;
    private int foodCost;
    private Item item; // optional single-item support
    private List<Item> items;
    private Trader trader;

    public Square(String terrain, int movementCost, int waterCost, int foodCost) {
        this.terrain = terrain;
        this.movementCost = movementCost;
        this.waterCost = waterCost;
        this.foodCost = foodCost;
        this.item = null;
        this.items = new ArrayList<>();
    }

    public String getTerrain() { return terrain; }
    public int getMovementCost() { return movementCost; }
    public int getWaterCost() { return waterCost; }
    public int getFoodCost() { return foodCost; }

    public void setItem(Item item) { this.item = item; }
    public Item getItem() { return item; }

    public void addItem(Item item) { this.items.add(item); }
    public void removeItem(Item item) { this.items.remove(item); }

    public List<Item> getItems() { return items; }

    public void collectItem(Player player) {
        Iterator<Item> it = items.iterator();
        while (it.hasNext()) {
            Item currentItem = it.next();
            currentItem.activate(player);
            if (!currentItem.isRepeating()) {
                it.remove();
            }
        }
    }

    public boolean hasFoodBonus() {
        for (Item i : items) {
            if (i instanceof FoodBonus) {
                return true;
            }
        }
        return false;
    }

    public boolean hasTrader() {
        return trader != null;
    }

    public Trader getTrader() {
        return trader;
    }

    public void setTrader(Trader t) {
        this.trader = t;
    }
}
