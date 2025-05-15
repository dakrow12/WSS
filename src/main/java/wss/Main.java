package wss;

import wss.game.*;
import wss.items.FoodBonus;
import wss.player.*;
import wss.trader.Trader;
import wss.trader.TraderType;
import java.util.Scanner;
import wss.items.*;
import wss.trader.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose mode:\n1 = Full game with engine\n2 = Small unit test map\n3 = Full Game - Multi-Run");
        int mode = scanner.nextInt();

        if (mode == 1) {
            runFullGame(scanner);
        } else if (mode == 2) {
            runUnitTest();
        } else if (mode == 3) {
            runMultiGameSimulation(scanner);
        } else {
            System.out.println("Invalid mode selected.");
        }

        scanner.close();
    }

    private static void runFullGame(Scanner scanner) {
        System.out.print("Enter map width: ");
        int width = scanner.nextInt();
        System.out.print("Enter map height: ");
        int height = scanner.nextInt();

        System.out.print("Choose difficulty (EASY, MEDIUM, HARD): ");
        String diffInput = scanner.next().toUpperCase();
        WSSGameEngine.Difficulty difficulty;
        try {
            difficulty = WSSGameEngine.Difficulty.valueOf(diffInput);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid difficulty. Defaulting to EASY.");
            difficulty = WSSGameEngine.Difficulty.EASY;
        }

        System.out.print("Choose brain (NORMAL, GREED, CONSERVATIVE): ");
        String brainInput = scanner.next().toUpperCase();
        WSSGameEngine.BrainType brainType;
        try {
            brainType = WSSGameEngine.BrainType.valueOf(brainInput);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid brain. Defaulting to NORMAL.");
            brainType = WSSGameEngine.BrainType.NORMAL;
        }

        WSSGameEngine game = new WSSGameEngine(width, height, difficulty, brainType);
        game.runGame();
    }

    private static void runUnitTest() {
        // Step 1: Create a simple map (3x3)
        Square[][] grid = new Square[3][3];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                grid[x][y] = new Square("plains", 1, 1, 1);
            }
        }

        // Step 2: Place a FoodBonus east of the start
        grid[1][0].setItem(new FoodBonus(5, false));
        
        // Add a trader to the test map
        Trader trader = new Trader(TraderType.FAIR);
        grid[1][0].setTrader(trader);
        System.out.println("Added a FAIR trader at position (1, 0)");

        // Step 3: Create a map, player, and brain without circular dependencies
        Map map = new Map();
        map.setGrid(grid);
        
        // First create the player with null brain
        Vision vision = new Cautious();
        Player player = new Player(10, 10, 10, vision, null);
        
        // Give the player some gold for trading
        player.addGold(5);
        System.out.println("Gave player 5 gold for trading");
        
        // Then create the brain with the player
        Brain brain = new ConservativeBrain(player, map);
	
        // Finally set the brain on the player
	player.setBrain(brain);

        // Step 4: Display and make move
        System.out.println("Starting position: (" + player.getX() + ", " + player.getY() + ")");
        System.out.println("Food: " + player.getCurrentFood());
        System.out.println("Gold: " + player.getCurrentGold());

        // Move player
        brain.makeMove();

        System.out.println("After move: (" + player.getX() + ", " + player.getY() + ")");
        System.out.println("Food: " + player.getCurrentFood());
        System.out.println("Gold: " + player.getCurrentGold());
        
        // Manually check for trader interaction
        Square currentSquare = map.getSquare(player.getX(), player.getY());
        System.out.println("Current square has trader: " + currentSquare.hasTrader());
        
        if (currentSquare.hasTrader()) {
            System.out.println("MANUALLY triggering trader interaction!");
            
            // First activate the trader
            Trader squareTrader = currentSquare.getTrader();
            squareTrader.activate(player);
            
            // Create a simple trade offer
            TradeOffer offer = new TradeOffer();
            offer.setGoldOffered(2);
            offer.setWaterRequested(4);
            
            System.out.println("Player making offer: " + offer);
            TradeResponse response = squareTrader.makeTrade(offer);
            
            if (response.isAccepted()) {
                System.out.println("Trader accepted the offer!");
                player.finalizeTrade(response.getFinalOffer());
            } else {
                System.out.println("Trader rejected the offer. Looking for counter-offer...");
                TradeOffer counter = squareTrader.generateCounterOffer(offer);
                
                if (counter != null) {
                    System.out.println("Trader made counter-offer: " + counter);
                    if (squareTrader.acceptTraderCounterOffer()) {
                        System.out.println("Player accepted counter-offer");
                        player.finalizeTrade(counter);
                    } else {
                        System.out.println("Player rejected counter-offer");
                    }
                }
            }
            
            System.out.println("After trade attempt - Food: " + player.getCurrentFood() + 
                             ", Water: " + player.getCurrentWater() + 
                             ", Gold: " + player.getCurrentGold());
        }
    }

    private static void runMultiGameSimulation(Scanner scanner) {
        System.out.println("\nChoose simulation settings:\n1 = Default Settings (13x13, EASY, NORMAL)\n2 = Custom Settings");
        int settingsChoice = scanner.nextInt();
        
        int width, height, numGames;
        WSSGameEngine.Difficulty difficulty;
        WSSGameEngine.BrainType brainType;
        
        if (settingsChoice == 1) {
            width = 13;
            height = 13;
            difficulty = WSSGameEngine.Difficulty.EASY;
            brainType = WSSGameEngine.BrainType.NORMAL;
            System.out.println("Using Default Settings: 13x13 map, EASY difficulty, NORMAL brain.");
        } else {
            // Step 1: Get simulation parameters
            System.out.print("Enter map width: ");
            width = scanner.nextInt();
            System.out.print("Enter map height: ");
            height = scanner.nextInt();

            System.out.print("Choose difficulty (EASY, MEDIUM, HARD): ");
            String diffInput = scanner.next().toUpperCase();
            try {
                difficulty = WSSGameEngine.Difficulty.valueOf(diffInput);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid difficulty. Defaulting to EASY.");
                difficulty = WSSGameEngine.Difficulty.EASY;
            }

            System.out.print("Choose brain (NORMAL, GREED, CONSERVATIVE): ");
            String brainInput = scanner.next().toUpperCase();
            try {
                brainType = WSSGameEngine.BrainType.valueOf(brainInput);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid brain. Defaulting to NORMAL.");
                brainType = WSSGameEngine.BrainType.NORMAL;
            }
        }
        
        System.out.print("Number of games to simulate: ");
        numGames = scanner.nextInt();
        
        // Step 2: Initialize statistics
        int winsCount = 0;
        int lossesFromFood = 0;
        int lossesFromWater = 0;
        int lossesFromStrength = 0;
        int lossesFromMaxTurns = 0;
        int totalTurns = 0;
        int totalTradersEncountered = 0;
        int totalTradesMade = 0;
        int totalFoodBonusesCollected = 0;
        int totalWaterBonusesCollected = 0;
        int totalGoldBonusesCollected = 0;
        
        // Step 3: Run simulations
        System.out.println("\nRunning " + numGames + " simulations...");
        
        for (int gameNum = 1; gameNum <= numGames; gameNum++) {
            System.out.println("\n--- Starting Simulation " + gameNum + " of " + numGames + " ---");
            
            // Create game with statistics tracking
            GameStatistics stats = new GameStatistics();
            WSSGameEngine game = new WSSGameEngine(width, height, difficulty, brainType, stats);
            
            // Run the game silently (without printing each turn)
            boolean showOutput = (gameNum == 1); // Only show detailed output for first game
            game.runGameSimulation(showOutput);
            
            // Collect statistics
            if (stats.isGameWon()) {
                winsCount++;
            } else if (stats.isLostDueToFood()) {
                lossesFromFood++;
            } else if (stats.isLostDueToWater()) {
                lossesFromWater++;
            } else if (stats.isLostDueToStrength()) {
                lossesFromStrength++;
            } else if (stats.isLostDueToMaxTurns()) {
                lossesFromMaxTurns++;
            }
            
            totalTurns += stats.getTurnsTaken();
            totalTradersEncountered += stats.getTradersEncountered();
            totalTradesMade += stats.getTradesMade();
            totalFoodBonusesCollected += stats.getFoodBonusesCollected();
            totalWaterBonusesCollected += stats.getWaterBonusesCollected();
            totalGoldBonusesCollected += stats.getGoldBonusesCollected();
            
            // Progress update
            System.out.println("Completed simulation " + gameNum + ": " + 
                             (stats.isGameWon() ? "WON" : "LOST") + 
                             " after " + stats.getTurnsTaken() + " turns");
        }
        
        // Step 4: Display statistics summary
        displayStatisticsSummary(numGames, winsCount, lossesFromFood, lossesFromWater, 
                              lossesFromStrength, lossesFromMaxTurns, totalTurns,
                              totalTradersEncountered, totalTradesMade,
                              totalFoodBonusesCollected, totalWaterBonusesCollected,
                              totalGoldBonusesCollected,
                              width, height, difficulty, brainType);
    }
    
    private static void displayStatisticsSummary(int numGames, int winsCount, 
                                           int lossesFromFood, int lossesFromWater,
                                           int lossesFromStrength, int lossesFromMaxTurns,
                                           int totalTurns, int totalTradersEncountered,
                                           int totalTradesMade, int totalFoodBonusesCollected,
                                           int totalWaterBonusesCollected, int totalGoldBonusesCollected,
                                           int width, int height, WSSGameEngine.Difficulty difficulty, 
                                           WSSGameEngine.BrainType brainType) {
        System.out.println("\n========================================");
        System.out.println("           SIMULATION SUMMARY           ");
        System.out.println("========================================");
        System.out.println("SIMULATION SETTINGS:");
        System.out.println("  Grid Size: " + width + "x" + height);
        System.out.println("  Difficulty: " + difficulty);
        System.out.println("  Brain Type: " + brainType);
        System.out.println();
        System.out.println("Total games simulated: " + numGames);
        System.out.println();
        
        // Win/loss statistics
        double winRate = (double) winsCount / numGames * 100;
        System.out.println("WIN/LOSS STATISTICS:");
        System.out.println("  Games won: " + winsCount + " (" + String.format("%.1f", winRate) + "%)");
        System.out.println("  Games lost: " + (numGames - winsCount) + " (" + String.format("%.1f", 100 - winRate) + "%)");
        System.out.println("    - From food depletion: " + lossesFromFood);
        System.out.println("    - From water depletion: " + lossesFromWater);
        System.out.println("    - From strength depletion: " + lossesFromStrength);
        System.out.println("    - From max turns: " + lossesFromMaxTurns);
        System.out.println();
        
        // Turn statistics
        double avgTurns = (double) totalTurns / numGames;
        System.out.println("TURN STATISTICS:");
        System.out.println("  Average turns per game: " + String.format("%.1f", avgTurns));
        System.out.println();
        
        // Resource collection statistics
        System.out.println("RESOURCE COLLECTION STATISTICS:");
        System.out.println("  Total food bonuses collected: " + totalFoodBonusesCollected);
        System.out.println("  Total water bonuses collected: " + totalWaterBonusesCollected);
        System.out.println("  Total gold bonuses collected: " + totalGoldBonusesCollected);
        System.out.println("  Average food bonuses per game: " + String.format("%.1f", (double) totalFoodBonusesCollected / numGames));
        System.out.println("  Average water bonuses per game: " + String.format("%.1f", (double) totalWaterBonusesCollected / numGames));
        System.out.println("  Average gold bonuses per game: " + String.format("%.1f", (double) totalGoldBonusesCollected / numGames));
        System.out.println();
        
        // Trader statistics
        System.out.println("TRADER STATISTICS:");
        System.out.println("  Total traders encountered: " + totalTradersEncountered);
        System.out.println("  Total trades made: " + totalTradesMade);
        System.out.println("  Average traders per game: " + String.format("%.1f", (double) totalTradersEncountered / numGames));
        System.out.println("  Average trades per game: " + String.format("%.1f", (double) totalTradesMade / numGames));
        System.out.println("  Trade success rate: " + (totalTradersEncountered > 0 ? 
                                                    String.format("%.1f", (double) totalTradesMade / totalTradersEncountered * 100) : "0.0") + "%");
        System.out.println("========================================");
    }
}
