package wss.player;

import wss.game.Direction;
import wss.game.Square;
import wss.items.Item;
import wss.trader.Trader;
import wss.trader.TradeOffer;

import java.sql.SQLOutput;

public class Player {
    private int maxStrength, currentStrength;
    private int maxFood, currentFood;
    private int maxWater, currentWater;
    private int currentGold;
    private int positionX, positionY;

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
        return true;
    }

    public void rest() {
        currentStrength = Math.min(maxStrength, currentStrength + 2);
        currentFood = Math.max(0, currentFood - 1);
        currentWater = Math.max(0, currentWater - 1);
    }

public void trade(Trader trader) {
    trader.activate(this);

    // Simple demo offer — the Brain logic should do this later
    TradeOffer myOffer = new TradeOffer(
        0,  // offerGold
        2,  // offerFood
        0,  // offerWater
        0,  // requestGold
        0,  // requestFood
        2   // requestWater
    );

    boolean accepted = trader.evaluatePlayerOffer(myOffer);

    if (!accepted) {
        TradeOffer counter = trader.generateCounterOffer(myOffer);
        if (counter != null) {
            System.out.println("Trader made a counter: " + counter);

            if (trader.acceptTraderCounterOffer()) {
                if (finalizeTrade(counter)) {
                    System.out.println("Trade completed (counter accepted).");
                } else {
                    System.out.println("Trade failed: not enough resources to fulfill counter offer.");
                }
            } else {
                trader.rejectTraderCounterOffer();
                System.out.println("Player rejected the counter offer.");
            }
        } else {
            System.out.println("No counter-offer from trader.");
        }
    } else {
        if (finalizeTrade(myOffer)) {
            System.out.println("Trade completed (original offer accepted).");
        } else {
            System.out.println("Trade failed: not enough resources to fulfill original offer.");
        }
    }
}


    public void collectItem(Item item) {
        item.activate(this);
    }

    public void addFood(int amount) {
        currentFood = Math.min(currentFood + amount, maxFood);
    }
    public boolean removeFood(int amount) {
        if (currentFood >= amount) {
            currentFood -= amount;
            System.out.println("Player used " + amount + " of food. Current: " + currentFood);
            return true;
        }
        System.out.println("Player tried to use " + amount + " food, but only has " + currentFood);
        return false;
    }
    public void addWater(int amount) {
        currentWater = Math.min(currentWater + amount, maxWater);
    }
    public boolean removeWater(int amount) {
        if (currentWater >= amount) {
            currentWater -= amount;
            System.out.println("Player used " + amount + " of water. Current: " + currentWater);
            return true;
        }
        System.out.println("Player tried to use " + amount + " water, but only has " + currentWater);
        return false;
    }
    public void addGold(int amount) {
        currentGold += amount;
    }
    public boolean removeGold(int amount) {
        if (currentGold >= amount) {
            currentGold -= amount;
            System.out.println("Player used " + amount + " of gold. Current: " + currentGold);
            return true;
        }
        System.out.println("Player tried to use " + amount + " gold, but only has " + currentGold);
        return false;
    }

    // --- Trade Affordability Check ---
    /**
     * Checks if the player has enough resources to fulfill their side of an offer.
     * @param offer The TradeOffer the player intends to make.
     * @return true if the player can afford to give the offered items, false otherwise.
     */
    public boolean canAffordOffer(TradeOffer offer) {
        return currentGold >= offer.offerGold &&
                currentFood >= offer.offerFood &&
                currentWater >= offer.offerWater;
    }

    /**
     * Updates player resources based on an accepted trade offer.
     * Assumes the player is the one INITIATING the offer acceptance.
     * @param acceptedOffer The final agreed-upon TradeOffer.
     */
   public boolean finalizeTrade(TradeOffer offer) {
    // Check if player has enough to give
    if (currentGold < offer.offerGold ||
        currentFood < offer.offerFood ||
        currentWater < offer.offerWater) {
        System.out.println("Trade failed: not enough resources.");
        return false;
    }

    // Deduct offered resources
    currentGold -= offer.offerGold;
    currentFood -= offer.offerFood;
    currentWater -= offer.offerWater;

    // Add received resources
    currentGold += offer.requestGold;
    currentFood = Math.min(currentFood + offer.requestFood, maxFood);
    currentWater = Math.min(currentWater + offer.requestWater, maxWater);

    return true;
}


    // --- Getters ---
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

    // --- Setters ---
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




}
