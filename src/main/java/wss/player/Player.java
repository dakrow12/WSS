package wss.player;

import wss.game.Direction;
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
    private boolean duringRest; // Track if player is resting
    private int turnsSinceLastRest = 0;

    private Vision vision;
    private Brain brain;

    public Player(int strength, int food, int water, Vision vision, Brain brain) {
        this.maxStrength = this.currentStrength = strength;
        this.maxFood = this.currentFood = food;
        this.maxWater = this.currentWater = water;
        this.currentGold = 0;
        this.vision = vision;
        this.brain = brain;
        this.positionX = 0;
        this.positionY = 0;
        this.duringRest = false;
        this.turnsSinceLastRest = 0;
    }

    public boolean move(Direction dir, Square[][] map) {
        int newX = positionX + dir.dx();
        int newY = positionY + dir.dy();

        if (newX < 0 || newY < 0 || newX >= map.length || newY >= map[0].length)
            return false;

        Square target = map[newX][newY];
        int moveCost = target.getMovementCost();
        int foodCost = target.getFoodCost();
        int waterCost = target.getWaterCost();

        if (currentStrength < moveCost || currentFood < foodCost || currentWater < waterCost)
            return false;

        currentStrength -= moveCost;
        currentFood -= foodCost;
        currentWater -= waterCost;

        positionX = newX;
        positionY = newY;
        duringRest = false;
        return true;
    }

    public void rest() {
        System.out.println("Player is resting to recover strength");
        
        // Recover strength - Increased from +2 to +4
        currentStrength = Math.min(maxStrength, currentStrength + 4);
        
        // Resting consumes less resources
        // Already handled by WSSGameEngine, so we don't consume here
        
        duringRest = true;
        turnsSinceLastRest = 0;
    }

    public void trade(Trader trader) {
        trader.activate(this);
        TradeOffer myOffer = new TradeOffer(0, 2, 0, 0, 0, 2);
        boolean accepted = trader.evaluatePlayerOffer(myOffer);

        if (!accepted) {
            TradeOffer counter = trader.generateCounterOffer(myOffer);
            if (counter != null) {
                System.out.println("Trader made a counter: " + counter);
                if (trader.acceptTraderCounterOffer()) {
                    if (finalizeTrade(counter)) {
                        System.out.println("Trade completed (counter accepted)." );
                    } else {
                        System.out.println("Trade failed: not enough resources.");
                    }
                } else {
                    trader.rejectTraderCounterOffer();
                }
            }
        } else {
            if (finalizeTrade(myOffer)) {
                System.out.println("Trade completed (original offer accepted)." );
            } else {
                System.out.println("Trade failed: not enough resources.");
            }
        }
    }

    public void collectItem(Item item) {
        item.activate(this);
    }

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

    public boolean canAffordOffer(TradeOffer offer) {
        return currentGold >= offer.offerGold && currentFood >= offer.offerFood && currentWater >= offer.offerWater;
    }

    public boolean finalizeTrade(TradeOffer offer) {
        if (!canAffordOffer(offer)) return false;
        
        // Update player resources
        currentGold -= offer.offerGold;
        currentFood -= offer.offerFood;
        currentWater -= offer.offerWater;
        currentGold += offer.requestGold;
        currentFood = Math.min(currentFood + offer.requestFood, maxFood);
        currentWater = Math.min(currentWater + offer.requestWater, maxWater);
        
        // Track successful trade in statistics
        if (brain != null && brain.getGameEngine() != null) {
            brain.getGameEngine().trackSuccessfulTrade();
        }
        
        return true;
    }

    // New methods added to support Brain.java
    public int getCurrentMovement() { return currentStrength; }
    public int getMaxMovement() { return maxStrength; }

    public void consumeFood(int amount) { currentFood = Math.max(0, currentFood - amount); }
    public void consumeWater(int amount) { currentWater = Math.max(0, currentWater - amount); }

    public void updateVision() { /* placeholder for vision logic */ }

    public boolean canMove(Direction dir) {
        // Real implementation to check map boundaries and terrain costs
        if (brain == null || brain.getMap() == null) return false;
        
        int newX = positionX + dir.dx();
        int newY = positionY + dir.dy();
        
        // Check if the new position is within map boundaries
        if (!brain.getMap().inBounds(newX, newY)) return false;
        
        // Get the target square
        Square target = brain.getMap().getSquare(newX, newY);
        
        // Check if player has enough resources to enter the square
        return currentStrength >= target.getMovementCost() &&
               currentFood >= target.getFoodCost() &&
               currentWater >= target.getWaterCost();
    }

    public Position getPosition() {
        return new Position(positionX, positionY);
    }


	public void collect(Item item) {
    collectItem(item);
}


    // Getters
    public int getX() { return positionX; }
    public int getY() { return positionY; }
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
    public void setCurrentStrength(int currentStrength) { this.currentStrength = currentStrength; }
    public void setMaxFood(int maxFood) { this.maxFood = maxFood; }
    public void setCurrentFood(int currentFood) { this.currentFood = currentFood; }
    public void setMaxWater(int maxWater) { this.maxWater = maxWater; }
    public void setCurrentWater(int currentWater) { this.currentWater = currentWater; }
    public void setCurrentGold(int currentGold) { this.currentGold = currentGold; }
    public void setPositionX(int positionX) { this.positionX = positionX; }
    public void setPositionY(int positionY) { this.positionY = positionY; }
    public void setVision(Vision vision) { this.vision = vision; }
    public void setBrain(Brain brain) { this.brain = brain; }

    public void setPosition(int x, int y, int mapWidth, int mapHeight) {
        if (x >= 0 && x < mapWidth && y >= 0 && y < mapHeight) {
            this.positionX = x;
            this.positionY = y;
        }
    }

    public boolean isDuringRest() { return duringRest; }
}