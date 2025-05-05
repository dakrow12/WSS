package wss.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import wss.items.Item; // Import the Item class to handle items on the square
import wss.player.Player; // Import the Player class for item collection logic

public class Square {
    private String terrain; // Represents the type of terrain for this square (e.g., "plains", "mountain")
    private int movementCost; 
    private int waterCost; 
    private int foodCost; 
    private Item item; // (Potentially deprecated by 'items' list) A single item that might be on this square
    private List<Item> items; // A list to hold one or more items present on this square

    /**
     * Constructor for the Square class. Initializes a new square with a specific terrain type
     * and associated costs.
     *
     * @param terrain      The type of terrain for this square.
     * @param movementCost The cost to move into this square.
     * @param waterCost    The water consumed by being on this square.
     * @param foodCost     The food consumed by being on this square.
     */
    public Square(String terrain, int movementCost, int waterCost, int foodCost) {
        this.terrain = terrain;
        this.movementCost = movementCost;
        this.waterCost = waterCost;
        this.foodCost = foodCost;
        this.item = null; // Initially, no single item is present
        this.items = new ArrayList<>(); // Initialize the list to hold multiple items
    }

    /**
     * Returns the type of terrain of this square.
     *
     * @return The terrain type (e.g., "plains").
     */
    public String getTerrain() {
        return terrain;
    }

    /**
     * Returns the movement cost to enter this square.
     *
     * @return The movement cost.
     */
    public int getMovementCost() {
        return movementCost;
    }

    /**
     * Returns the water cost associated with this square.
     *
     * @return The water cost.
     */
    public int getWaterCost() {
        return waterCost;
    }

    /**
     * Returns the food cost associated with this square.
     *
     * @return The food cost.
     */
    public int getFoodCost() {
        return foodCost;
    }

    /**
     * Sets a single item on this square. Note: Consider using addItem for multiple items.
     *
     * @param item The item to place on the square.
     */
    public void setItem(Item item) {
        this.item = item;
    }

    /**
     * Returns the single item currently on this square (if any).
     * Note: Consider using the 'items' list for handling multiple items.
     *
     * @return The item on the square, or null if no item is present (using the single 'item' attribute).
     */
    public Item getItem() {
        return item;
    }

    /**
     * Adds an item to the list of items on this square. This allows a square to hold multiple items.
     *
     * @param item The item to add to the square.
     */
    public void addItem(Item item) {
        this.items.add(item);
    }

    /**
     * Removes a specific item from the list of items on this square.
     *
     * @param item The item to remove.
     */
    public void removeItem(Item item) {
        this.items.remove(item);
    }

    /**
     * Allows a player to collect all applicable items from this square.
     * It iterates through the list of items, activates them for the player,
     * and removes non-repeating items from the square.
     *
     * @param player The player collecting the items.
     */
    public void collectItem(Player player) {
        Iterator<Item> it = items.iterator();
        while (it.hasNext()) {
            Item currentItem = it.next();
            currentItem.activate(player); // Activate the effect of the item on the player
            if (!currentItem.isRepeating()) {
                it.remove(); // Remove non-repeating items after collection
            }
        }
    }

    /**
     * Checks if this square contains at least one FoodBonus item.
     *
     * @return True if a FoodBonus item is present, false otherwise.
     */
    public boolean hasFoodBonus() {
        for (Item i : items) {
            if (i instanceof wss.items.FoodBonus) { // Check if the item is an instance of FoodBonus
                return true;
            }
        }
        return false;
    }
}