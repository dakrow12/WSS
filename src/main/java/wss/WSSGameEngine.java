package wss;

import wss.game.*;
import wss.items.*;
import wss.player.*;
import wss.trader.Trader;
import wss.trader.TraderType;


public class WSSGameEngine {
    private Map map;
    private Player player;
    private Brain brain;
    private int turns;
    private boolean gameOver;

    public WSSGameEngine(int width, int height, Difficulty difficulty, BrainType brainType) {
        this.map = new Map();
        map.generateMap(width, height, wss.game.Difficulty.valueOf(difficulty.name()));
        

	// Add early water sources
	map.getSquare(1, 0).addItem(new WaterBonus(3, false));
	map.getSquare(2, 1).addItem(new WaterBonus(4, false));
	map.getSquare(3, 0).addItem(new WaterBonus(2, false));

	// Add early food bonuses
	map.getSquare(2, 0).addItem(new FoodBonus(4, true));  // repeating = survival help
	map.getSquare(3, 1).addItem(new FoodBonus(2, false));


	// Add early traders
	map.getSquare(2, 0).setTrader(new Trader(TraderType.FAIR));

	map.getSquare(9, 1).setTrader(new Trader(TraderType.GENEROUS));




        // Adjust starting resources based on difficulty
        int startingFood = 15;
        int startingWater = 15;
        int startingStrength = 12;
        
        switch (difficulty) {
            case EASY:
                startingFood = 25;
                startingWater = 25;
                startingStrength = 18;
                break;
            case MEDIUM:
                // Default values
                break;
            case HARD:
                startingFood = 12;
                startingWater = 12;
                startingStrength = 10;
                break;
        }
        
        // First create player with null brain
        this.player = new Player(startingStrength, startingFood, startingWater, new Cautious(), null);
        
        // Then create brain with player reference
        this.brain = createBrain(brainType);
        
        // Set the brain on the player
        this.player.setBrain(this.brain);
        
        this.turns = 0;
        this.gameOver = false;
        System.out.println("\nGenerated Map:");
        map.printMap();
        
        System.out.println("\nStarting Resources: Food=" + player.getCurrentFood() + 
                         ", Water=" + player.getCurrentWater() + 
                         ", Strength=" + player.getCurrentStrength());
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
        while (!gameOver) {
            turns++;
            System.out.println("\n=== Turn " + turns + " ===");
            System.out.println("Position: (" + player.getX() + ", " + player.getY() + ")");
            System.out.println("Resources: Food=" + player.getCurrentFood() + 
                             ", Water=" + player.getCurrentWater() + 
                             ", Strength=" + player.getCurrentStrength() + 
                             ", Gold=" + player.getCurrentGold());
            
            updateGameState();
            
            System.out.println("After consumption: Food=" + player.getCurrentFood() + 
                             ", Water=" + player.getCurrentWater());
            
            String terrainType = map.getSquare(player.getX(), player.getY()).getTerrain();
            System.out.println("Current terrain: " + terrainType);
            
            brain.makeMove();
            
            System.out.println("Moved to: (" + player.getX() + ", " + player.getY() + ")");

            // Check win/loss conditions
            if (player.getX() >= map.getWidth() - 1) {
                System.out.println("\uD83C\uDF89 Player reached the east edge and won!");
                gameOver = true;
            } else if (player.getCurrentFood() <= 0 ||
                    player.getCurrentWater() <= 0 ||
                    player.getCurrentStrength() <= 0) {
                System.out.println("\uD83D\uDC80 Player ran out of resources. Game Over.");
                gameOver = true;
            } else if (turns >= 1000) {
                System.out.println("Max turns reached! Game Over.");
                gameOver = true;
            }
        }

        printFinalStats();
    }

    private void updateGameState() {
        // For simplicity, consume 1 food and 1 water per turn
        player.consumeFood(1);
        player.consumeWater(1);
        
        // The Player.rest() method now handles reduced consumption during rest
        
        player.updateVision();
    }

    private void printFinalStats() {
        System.out.println("\n=== Game Statistics ===");
        System.out.println("Turns taken: " + turns);
        System.out.println("Final position: (" + player.getX() + ", " + player.getY() + ")");
        System.out.println("Resources remaining:");
        System.out.println("  Food: " + player.getCurrentFood() + "/" + player.getMaxFood());
        System.out.println("  Water: " + player.getCurrentWater() + "/" + player.getMaxWater());
        System.out.println("  Strength: " + player.getCurrentStrength() + "/" + player.getMaxStrength());
        System.out.println("  Gold: " + player.getCurrentGold());
    }

    public enum BrainType {
        NORMAL, GREED, CONSERVATIVE
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}