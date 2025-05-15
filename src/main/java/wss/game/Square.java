package wss.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import wss.items.Item;
import wss.items.FoodBonus;
import wss.player.Player;
import wss.trader.Trader;
import wss.util.GameLogger;

public class Square {
    private String terrain;
    private int movementCost;
    private int waterCost;
    private int foodCost;
    private List<Item> items;
    private Trader trader;

    public Square(String terrain, int movementCost, int waterCost, int foodCost) {
        this.terrain = terrain;
        this.movementCost = movementCost;
        this.waterCost = waterCost;
        this.foodCost = foodCost;
        this.items = new ArrayList<>();
    }

    public String getTerrain() { return terrain; }
    public int getMovementCost() { return movementCost; }
    public int getWaterCost() { return waterCost; }
    public int getFoodCost() { return foodCost; }

    public void setItem(Item item) { 
        this.items.clear();
        this.items.add(item);
    }
    
    public Item getItem() { 
        return items.isEmpty() ? null : items.get(0);
    }

    public void addItem(Item item) { this.items.add(item); }
    public void removeItem(Item item) { this.items.remove(item); }

    public List<Item> getItems() { return items; }

    public void collectItem(Player player) {
        if (items.isEmpty()) {
            return;
        }
        
        GameLogger.section("Item Collection");
        GameLogger.info("Collecting items from " + terrain + " square");
        
        Iterator<Item> it = items.iterator();
        while (it.hasNext()) {
            Item currentItem = it.next();
            
            // Store resource values before activation to calculate the difference
            int beforeFood = player.getCurrentFood();
            int beforeWater = player.getCurrentWater();
            int beforeGold = player.getCurrentGold();
            
            // Activate the item
            currentItem.activate(player);
            
            // Calculate and display the changes
            int foodDiff = player.getCurrentFood() - beforeFood;
            int waterDiff = player.getCurrentWater() - beforeWater;
            int goldDiff = player.getCurrentGold() - beforeGold;
            
            String itemName = currentItem.getClass().getSimpleName();
            String effect = 
                (foodDiff > 0 ? "+" + foodDiff + " food " : "") + 
                (waterDiff > 0 ? "+" + waterDiff + " water " : "") + 
                (goldDiff > 0 ? "+" + goldDiff + " gold" : "");
            
            GameLogger.itemCollection(
                itemName + (currentItem.isRepeating() ? " (repeating)" : ""),
                effect.trim()
            );
            
            // Remove non-repeating items
            if (!currentItem.isRepeating()) {
                it.remove();
            }
        }
        
        GameLogger.info("Resources after collection: Food=" + player.getCurrentFood() + 
                        ", Water=" + player.getCurrentWater() + 
                        ", Gold=" + player.getCurrentGold());
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
        GameLogger.info("Getting trader: " + (trader != null ? trader.getPersonality() : "null"));
        return trader;
    }

    public void setTrader(Trader t) {
        this.trader = t;
        GameLogger.info("Trader set: " + (t != null ? t.getPersonality() : "null"));
    }
}
