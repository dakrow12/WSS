package wss;

/**
 * Tracks statistics for a single game run in the WSS simulation.
 * Used for collecting data across multiple game runs.
 */
public class GameStatistics {
    private int turnsTaken = 0;
    private boolean gameWon = false;
    private boolean lostDueToFood = false;
    private boolean lostDueToWater = false;
    private boolean lostDueToStrength = false;
    private boolean lostDueToMaxTurns = false;
    
    private int tradersEncountered = 0;
    private int tradesMade = 0;
    private int foodBonusesCollected = 0;
    private int waterBonusesCollected = 0;
    private int goldBonusesCollected = 0;
    
    // Getters
    public int getTurnsTaken() { return turnsTaken; }
    public boolean isGameWon() { return gameWon; }
    public boolean isLostDueToFood() { return lostDueToFood; }
    public boolean isLostDueToWater() { return lostDueToWater; }
    public boolean isLostDueToStrength() { return lostDueToStrength; }
    public boolean isLostDueToMaxTurns() { return lostDueToMaxTurns; }
    public int getTradersEncountered() { return tradersEncountered; }
    public int getTradesMade() { return tradesMade; }
    public int getFoodBonusesCollected() { return foodBonusesCollected; }
    public int getWaterBonusesCollected() { return waterBonusesCollected; }
    public int getGoldBonusesCollected() { return goldBonusesCollected; }
    
    // Setters and incrementers
    public void setTurnsTaken(int turns) { this.turnsTaken = turns; }
    public void incrementTurnsTaken() { this.turnsTaken++; }
    
    public void setGameWon(boolean won) { this.gameWon = won; }
    public void setLostDueToFood(boolean lost) { this.lostDueToFood = lost; }
    public void setLostDueToWater(boolean lost) { this.lostDueToWater = lost; }
    public void setLostDueToStrength(boolean lost) { this.lostDueToStrength = lost; }
    public void setLostDueToMaxTurns(boolean lost) { this.lostDueToMaxTurns = lost; }
    
    public void incrementTradersEncountered() { this.tradersEncountered++; }
    public void incrementTradesMade() { this.tradesMade++; }
    public void incrementFoodBonusesCollected() { this.foodBonusesCollected++; }
    public void incrementWaterBonusesCollected() { this.waterBonusesCollected++; }
    public void incrementGoldBonusesCollected() { this.goldBonusesCollected++; }
    
    /**
     * Resets all statistics to their default values.
     * Use when reusing the same statistics object for multiple games.
     */
    public void reset() {
        turnsTaken = 0;
        gameWon = false;
        lostDueToFood = false;
        lostDueToWater = false;
        lostDueToStrength = false;
        lostDueToMaxTurns = false;
        tradersEncountered = 0;
        tradesMade = 0;
        foodBonusesCollected = 0;
        waterBonusesCollected = 0;
        goldBonusesCollected = 0;
    }
} 