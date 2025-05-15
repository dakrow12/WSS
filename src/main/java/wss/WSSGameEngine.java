package wss;

import wss.game.*;
import wss.items.*;
import wss.player.*;
import wss.trader.Trader;
import wss.trader.TraderType;
import wss.util.GameLogger;


public class WSSGameEngine {
    private Map map;
    private Player player;
    private Brain brain;
    private int turns;
    private boolean gameOver;
    private GameStatistics stats; // Add statistics tracking

    public WSSGameEngine(int width, int height, Difficulty difficulty, BrainType brainType) {
        this(width, height, difficulty, brainType, null);
    }
    
    public WSSGameEngine(int width, int height, Difficulty difficulty, BrainType brainType, GameStatistics stats) {
        this.map = new Map();
        map.generateMap(width, height, wss.game.Difficulty.valueOf(difficulty.name()));
        

	// Add early water sources - improved quantities for MEDIUM difficulty
	map.getSquare(1, 0).addItem(new WaterBonus(5, false)); // Increased from 4
	map.getSquare(2, 1).addItem(new WaterBonus(6, false)); // Increased from 5
	map.getSquare(3, 0).addItem(new WaterBonus(4, false)); // Increased from 3
        map.getSquare(0, 1).addItem(new WaterBonus(3, true));  // Increased from 2, repeating

	// Add early food bonuses - improved quantities for MEDIUM difficulty
	map.getSquare(2, 0).addItem(new FoodBonus(6, true));  // Increased from 5, still repeating
	map.getSquare(3, 1).addItem(new FoodBonus(4, false)); // Increased from 3
        map.getSquare(1, 1).addItem(new FoodBonus(5, false)); // Increased from 4
        map.getSquare(0, 2).addItem(new FoodBonus(4, true));  // Increased from 3, still repeating

        // Add some early gold bonuses - kept the same
        map.getSquare(2, 2).addItem(new GoldBonus(2, false));
        map.getSquare(3, 2).addItem(new GoldBonus(2, false));

	// Removed hardcoded traders - they're now placed randomly by the Map class



        // Adjust starting resources based on difficulty - slightly reduced
        int startingFood = 22; // Reduced from 25
        int startingWater = 22; // Reduced from 25
        int startingStrength = 18; // Reduced from 20
        int startingGold = 15; // Add starting gold for all players
        
        switch (difficulty) {
            case EASY:
                startingFood = 35; // Reduced from 40
                startingWater = 35; // Reduced from 40
                startingStrength = 22; // Reduced from 25
                break;
            case MEDIUM:
                // Create new MEDIUM settings between EASY and previous MEDIUM (now HARD)
                startingFood = 32;
                startingWater = 32;
                startingStrength = 21;
                break;
            case HARD:
                // Use previous MEDIUM settings for HARD (should give ~50% win rate)
                startingFood = 30;
                startingWater = 30;
                startingStrength = 20;
                break;
        }
        
        // First create player with null brain
        this.player = new Player(startingStrength, startingFood, startingWater, new Cautious(), null);
        
        // Set starting gold
        this.player.setCurrentGold(startingGold);
        
        // Then create brain with player reference
        this.brain = createBrain(brainType);
        
        // Set the brain on the player
        this.player.setBrain(this.brain);
        
        // Set this game engine on the brain for statistics tracking
        this.brain.setGameEngine(this);
        
        this.turns = 0;
        this.gameOver = false;
        this.stats = stats; // Store statistics reference
        
        GameLogger.header("WILDERNESS SURVIVAL SIMULATION");
        GameLogger.section("Generated Map");
        map.printMap();
        
        GameLogger.section("Starting Resources");
        GameLogger.playerStats(
            player.getCurrentFood(), 
            player.getCurrentWater(), 
            player.getCurrentStrength(),
            player.getCurrentGold(),
            player.getX(),
            player.getY()
        );
    }

    private Brain createBrain(BrainType type) {
        switch (type) {
            case GREED -> {
                return new GreedBrain(player, map);
            }
            case CONSERVATIVE -> {
                return new ConservativeBrain(player, map);
            }
            default -> {
                return new NormalBrain(player, map);
            }
        }
    }

    public void runGame() {
        runGameSimulation(true);
    }
    
    public void runGameSimulation(boolean showOutput) {
        while (!gameOver) {
            turns++;
            
            if (showOutput) {
                GameLogger.turn(turns);
                GameLogger.playerStats(
                    player.getCurrentFood(), 
                    player.getCurrentWater(), 
                    player.getCurrentStrength(),
                    player.getCurrentGold(),
                    player.getX(),
                    player.getY()
                );
            }
            
            updateGameState();
            
            if (showOutput) {
                GameLogger.info("After consumption: Food=" + player.getCurrentFood() + 
                               ", Water=" + player.getCurrentWater());
                
                String terrainType = map.getSquare(player.getX(), player.getY()).getTerrain();
                GameLogger.info("Current terrain: " + terrainType);
            }
            
            brain.makeMove();
            
            if (showOutput) {
                GameLogger.movement("", player.getX(), player.getY());
            }

            // Check win/loss conditions
            if (player.getX() >= map.getWidth() - 1) {
                if (stats != null) {
                    stats.setGameWon(true);
                    stats.setTurnsTaken(turns);
                }
                
                if (showOutput) {
                    GameLogger.gameOver(true, "Player reached the east edge!");
                }
                gameOver = true;
            } else if (player.getCurrentFood() <= 0) {
                if (stats != null) {
                    stats.setLostDueToFood(true);
                    stats.setTurnsTaken(turns);
                }
                
                if (showOutput) {
                    GameLogger.gameOver(false, "Player ran out of food.");
                }
                gameOver = true;
            } else if (player.getCurrentWater() <= 0) {
                if (stats != null) {
                    stats.setLostDueToWater(true);
                    stats.setTurnsTaken(turns);
                }
                
                if (showOutput) {
                    GameLogger.gameOver(false, "Player ran out of water.");
                }
                gameOver = true;
            } else if (player.getCurrentStrength() <= 0) {
                if (stats != null) {
                    stats.setLostDueToStrength(true);
                    stats.setTurnsTaken(turns);
                }
                
                if (showOutput) {
                    GameLogger.gameOver(false, "Player ran out of strength.");
                }
                gameOver = true;
            } else if (turns >= 1000) {
                if (stats != null) {
                    stats.setLostDueToMaxTurns(true);
                    stats.setTurnsTaken(turns);
                }
                
                if (showOutput) {
                    GameLogger.gameOver(false, "Max turns reached!");
                }
                gameOver = true;
            }
            
            // Update statistics for this turn if we're tracking them
            if (stats != null) {
                stats.incrementTurnsTaken();
            }
            
            // Add a divider between turns for better readability
            if (showOutput && !gameOver) {
                GameLogger.divider();
            }
        }

        if (showOutput) {
            printFinalStats();
        }
    }

    private void updateGameState() {
        // For simplicity, consume 1 food and 1 water per turn
        player.consumeFood(1);
        player.consumeWater(1);
        
        // The Player.rest() method now handles reduced consumption during rest
        
        player.updateVision();
    }

    private void printFinalStats() {
        GameLogger.header("GAME STATISTICS");
        GameLogger.info("Turns taken: " + turns);
        GameLogger.info("Final position: (" + player.getX() + ", " + player.getY() + ")");
        
        GameLogger.section("Resources Remaining");
        GameLogger.info("Food: " + player.getCurrentFood() + "/" + player.getMaxFood());
        GameLogger.info("Water: " + player.getCurrentWater() + "/" + player.getMaxWater());
        GameLogger.info("Strength: " + player.getCurrentStrength() + "/" + player.getMaxStrength());
        GameLogger.info("Gold: " + player.getCurrentGold());
    }
    
    // Method to track trader encounter in statistics
    public void trackTraderEncounter() {
        if (stats != null) {
            stats.incrementTradersEncountered();
        }
    }
    
    // Method to track a successful trade in statistics
    public void trackSuccessfulTrade() {
        if (stats != null) {
            stats.incrementTradesMade();
        }
    }
    
    // Methods to track resource collection
    public void trackFoodBonusCollected() {
        if (stats != null) {
            stats.incrementFoodBonusesCollected();
        }
    }
    
    public void trackWaterBonusCollected() {
        if (stats != null) {
            stats.incrementWaterBonusesCollected();
        }
    }
    
    public void trackGoldBonusCollected() {
        if (stats != null) {
            stats.incrementGoldBonusesCollected();
        }
    }

    public enum BrainType {
        NORMAL, GREED, CONSERVATIVE
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}