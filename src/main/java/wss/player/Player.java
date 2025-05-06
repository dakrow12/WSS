package wss.player;

import wss.game.Direction;
import wss.game.Map; // Required for the new canMove method
import wss.game.Position;
import wss.game.Square;
import wss.items.Item;
import wss.trader.Trader;
import wss.trader.TradeOffer;

public class Player {
    private int maxStrength, currentStrength;
    private int maxFood, currentFood;
    private int maxWater, currentWater;
    private int currentGold;
    private int positionX, positionY;

    private Vision vision;
    private Brain brain; // Brain is part of Player

    public Player(int strength, int food, int water, Vision vision, Brain brain) {
        this.maxStrength = this.currentStrength = strength;
        this.maxFood = this.currentFood = food;
        this.maxWater = this.currentWater = water;
        this.currentGold = 0;
        this.vision = vision;
        this.brain = brain; // Initialize brain
        this.positionX = 0; // Default starting position
        this.positionY = 0;
    }
    
    // Constructor that allows Player to be created, and Brain set later (if needed)
    public Player(int strength, int food, int water, Vision vision) {
        this.maxStrength = this.currentStrength = strength;
        this.maxFood = this.currentFood = food;
        this.maxWater = this.currentWater = water;
        this.currentGold = 0;
        this.vision = vision;
        this.brain = null; // Brain can be set later using setBrain()
        this.positionX = 0;
        this.positionY = 0;
    }


    /**
     * Attempts to move the player in the given direction on the provided map grid.
     * @param dir The direction to move.
     * @param mapGrid The grid of squares representing the map.
     * @return true if the move was successful, false otherwise (out of bounds, insufficient resources, etc.).
     */
    public boolean move(Direction dir, Square[][] mapGrid) {
        if (dir == null || mapGrid == null) {
            System.err.println("Error: Null direction or mapGrid in Player.move");
            return false;
        }

        int newX = positionX + dir.dx();
        int newY = positionY + dir.dy();

        // Check map boundaries
        if (newX < 0 || newY < 0 || newX >= mapGrid.length || (mapGrid.length > 0 && newY >= mapGrid[0].length)) {
            return false; // Out of bounds
        }

        Square target = mapGrid[newX][newY];
        if (target == null) {
            // This implies an issue with map generation if a square within bounds is null.
            System.err.println("Error: Target square is null at (" + newX + "," + newY + ") in Player.move");
            return false; 
        }

        int moveCost = target.getMovementCost();
        int foodCost = target.getFoodCost();
        int waterCost = target.getWaterCost();

        // Check if player has enough resources
        if (currentStrength < moveCost || currentFood < foodCost || currentWater < waterCost) {
            return false; // Not enough resources
        }

        // Deduct resources
        currentStrength -= moveCost;
        currentFood -= foodCost;
        currentWater -= waterCost;

        // Update position
        positionX = newX;
        positionY = newY;
        return true; // Move successful
    }

    /**
     * Checks if the player *can* make a move in the given direction on the map.
     * This considers map boundaries and resource costs for the target square.
     * @param dir The direction to check.
     * @param map The game map object (to access grid and square info).
     * @return true if the move is possible, false otherwise.
     */
    public boolean canMove(Direction dir, Map map) {
        if (dir == null || map == null || map.getGrid() == null) {
            return false;
        }
        Square[][] mapGrid = map.getGrid();
        int newX = positionX + dir.dx();
        int newY = positionY + dir.dy();

        if (newX < 0 || newY < 0 || newX >= mapGrid.length || (mapGrid.length > 0 && newY >= mapGrid[0].length)) {
            return false; // Out of bounds
        }

        Square target = mapGrid[newX][newY];
        if (target == null) {
             System.err.println("Warning: Target square is null at (" + newX + "," + newY + ") in Player.canMove");
            return false;
        }

        return currentStrength >= target.getMovementCost() &&
               currentFood >= target.getFoodCost() &&
               currentWater >= target.getWaterCost();
    }


    public void rest() {
        currentStrength = Math.min(maxStrength, currentStrength + 2); // Regain some strength
        currentFood = Math.max(0, currentFood - 1);     // Consume a little food
        currentWater = Math.max(0, currentWater - 1);   // Consume a little water
    }

    public void trade(Trader trader) {
        if (trader == null) return;
        trader.activate(this); // Player initiates interaction

        // Example: Player makes a simple initial offer (this logic might be in Brain)
        // For now, this is a placeholder; the Brain's initiateTrade should handle offer creation.
        // This method could be called by the Brain after 'shouldTradeWith' is true.
        // Or, the Brain's `initiateTrade` directly interacts with the trader.
        // Let's assume Brain's `initiateTrade` is the primary logic point.
        // This Player.trade method could be simplified or removed if Brain handles all.

        // The complex trading logic from the original Player.java:
        TradeOffer myOffer = new TradeOffer(0, 2, 0, 0, 0, 2); // Example offer
        System.out.println("Player attempting to trade with " + trader.getPersonality() + " trader with offer: " + myOffer);
        
        if (!canAffordOffer(myOffer)) {
            System.out.println("Player cannot afford initial offer.");
            trader.reset(); // Trader might reset if player can't even make an opening valid offer.
            return;
        }

        boolean accepted = trader.evaluatePlayerOffer(myOffer);

        if (!accepted) {
            System.out.println("Player's initial offer rejected. Trader state: " + trader.getCurrentState());
            TradeOffer counter = trader.generateCounterOffer(myOffer); // Trader might make a counter
            if (counter != null) {
                System.out.println("Trader made a counter-offer: " + counter);
                // Player decides whether to accept the counter (could be Brain logic)
                // For simplicity, let's say player always accepts FAIR/GENEROUS counters if affordable
                boolean acceptCounterDecision = false;
                if (trader.getPersonality() == wss.trader.TraderType.FAIR || trader.getPersonality() == wss.trader.TraderType.GENEROUS) {
                    acceptCounterDecision = true;
                }

                if (acceptCounterDecision && canAffordOffer(counter)) { // Player needs to afford what *trader requests*
                    if (trader.acceptTraderCounterOffer()) { // This confirms trader is ready for player to finalize
                         if (finalizeTrade(trader.getLastTraderOffer())) { // Player finalizes with trader's last offer
                            System.out.println("Trade completed (player accepted counter).");
                        } else {
                            System.out.println("Trade failed: Player could not finalize accepted counter (resource issue).");
                            // Inform trader? Trader might need to roll back.
                        }
                    } else {
                        System.out.println("Player decided to accept counter, but trader did not confirm acceptance state.");
                    }
                } else {
                    if (!canAffordOffer(counter)) System.out.println("Player cannot afford trader's counter offer.");
                    trader.rejectTraderCounterOffer(); // Player rejects/cannot afford counter
                     System.out.println("Player rejected trader's counter-offer.");
                }
            } else {
                System.out.println("Trader did not make a counter-offer. Negotiation ends.");
                trader.reset();
            }
        } else { // Player's initial offer was accepted
            System.out.println("Player's initial offer accepted by trader.");
            if (finalizeTrade(myOffer)) {
                System.out.println("Trade completed (original offer accepted).");
            } else {
                System.out.println("Trade failed: Player could not finalize accepted original offer (resource issue).");
                // Trader might need to roll back.
            }
        }
    }

    public void collectItem(Item item) {
        if (item != null) {
            item.activate(this);
        }
    }

    // Resource modification methods
    public void addFood(int amount) { currentFood = Math.min(currentFood + amount, maxFood); }
    public void addWater(int amount) { currentWater = Math.min(currentWater + amount, maxWater); }
    public void addGold(int amount) { currentGold += amount; }

    public boolean removeFood(int amount) {
        if (currentFood >= amount) { currentFood -= amount; return true; }
        return false;
    }
    public boolean removeWater(int amount) {
        if (currentWater >= amount) { currentWater -= amount; return true; }
        return false;
    }
    public boolean removeGold(int amount) {
        if (currentGold >= amount) { currentGold -= amount; return true; }
        return false;
    }

    /**
     * Checks if the player has enough resources to cover what THEY are offering in a trade.
     * @param offer The trade offer.
     * @return true if the player can afford their side of the offer.
     */
    public boolean canAffordOffer(TradeOffer offer) {
        if (offer == null) return false;
        return currentGold >= offer.offerGold && 
               currentFood >= offer.offerFood && 
               currentWater >= offer.offerWater;
    }

    /**
     * Finalizes a trade by adjusting player's resources based on the agreed offer.
     * Assumes canAffordOffer was checked for what player GIVES.
     * @param offer The final agreed-upon trade offer.
     * @return true if the trade was successfully applied to player's inventory.
     */
    public boolean finalizeTrade(TradeOffer offer) {
        if (offer == null || !canAffordOffer(offer)) { // Double check affordability of what player gives
            return false;
        }
        // Deduct what player gives
        currentGold -= offer.offerGold;
        currentFood -= offer.offerFood;
        currentWater -= offer.offerWater;
        
        // Add what player receives
        currentGold += offer.requestGold;
        currentFood = Math.min(currentFood + offer.requestFood, maxFood);
        currentWater = Math.min(currentWater + offer.requestWater, maxWater);
        return true;
    }

    // Getters
    public int getX() { return positionX; }
    public int getY() { return positionY; }
    public Position getPosition() { return new Position(positionX, positionY); }
    public Vision getVision() { return vision; }
    public Brain getBrain() { return brain; }
    public int getCurrentFood() { return currentFood; }
    public int getCurrentWater() { return currentWater; }
    public int getCurrentStrength() { return currentStrength; }
    public int getCurrentGold() { return currentGold; }
    public int getMaxFood() { return maxFood; }
    public int getMaxWater() { return maxWater; }
    public int getMaxStrength() { return maxStrength; }

    // Setters
    public void setMaxStrength(int maxStrength) { this.maxStrength = maxStrength; }
    public void setCurrentStrength(int currentStrength) { this.currentStrength = Math.max(0, currentStrength); }
    public void setMaxFood(int maxFood) { this.maxFood = maxFood; }
    public void setCurrentFood(int currentFood) { this.currentFood = Math.max(0, currentFood); }
    public void setMaxWater(int maxWater) { this.maxWater = maxWater; }
    public void setCurrentWater(int currentWater) { this.currentWater = Math.max(0, currentWater); }
    public void setCurrentGold(int currentGold) { this.currentGold = Math.max(0, currentGold); }
    
    public void setPosition(int x, int y, int mapWidth, int mapHeight) {
        if (x >= 0 && x < mapWidth && y >= 0 && y < mapHeight) {
            this.positionX = x;
            this.positionY = y;
        } else {
            System.err.println("Warning: Attempt to set player position out of bounds.");
            // Optionally clamp to bounds or throw error
        }
    }
    public void setVision(Vision vision) { this.vision = vision; }
    public void setBrain(Brain brain) { this.brain = brain; }

    // From original Player.java, seems like a duplicate or alternative to collectItem(Item)
    public void collect(Item item) {
        collectItem(item);
    }
    // Methods from original Player.java that might be used by WSSGameEngine or Brain
    public void consumeFood(int amount) { currentFood = Math.max(0, currentFood - amount); }
    public void consumeWater(int amount) { currentWater = Math.max(0, currentWater - amount); }
    public void updateVision() { /* Placeholder if vision needs active updating by player state */ }
}
