package wss.game;

import java.util.Random;

public class Map {

    private Square[][] grid;
    private int width; 
    private int height; 

    /**
     * Constructor for the Map class. 
     */
    public Map() {
        this.width = 0;
        this.height = 0;
        this.grid = null;
    }


	public Map(Square[][] grid) {
    this.grid = grid;
}



    /**
     * Returns the 2D array representing the game map (grid of Squares).
     *
     * @return The 2D array of Square objects.
     */
    public Square[][] getGrid() {
        return grid;
    }
	public Square getSquare(Position pos) {
    return getSquare(pos.getX(), pos.getY());
}

    /**
     * Returns the width of the map.
     *
     * @return The width of the map.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the map.
     *
     * @return The height of the map.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Generates the game map with the specified width, height, and difficulty.
     * This method is responsible for creating the grid of Squares and populating them with terrain.
     *
     * @param width      The width of the map.
     * @param height     The height of the map.
     * @param difficulty The difficulty level of the game, influencing terrain generation.
     */
    public void generateMap(int width, int height, Difficulty difficulty) {
        this.width = width;
        this.height = height;
        this.grid = new Square[width][height]; 
        Random random = new Random(); 

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                String terrain = determineTerrain(difficulty, random);
                int moveCost = getMoveCost(terrain); 
                int waterCost = getWaterCost(terrain); 
                int foodCost = getFoodCost(terrain); 

                grid[x][y] = new Square(terrain, moveCost, waterCost, foodCost); 
                placeItem(grid[x][y], difficulty, random); // Potentially place an item on this square
            }
        }
    }




public void setGrid(Square[][] grid) {
    this.grid = grid;
}


    /**
     * Determines the terrain type for a given square based on the game's difficulty.
     * This method uses a probability-based approach to distribute different terrain types across the map.
     *
     * @param difficulty The difficulty level of the game.
     * @param random     A Random object for generating random numbers.
     * @return The terrain type (e.g., "plains", "mountain").
     */
    private String determineTerrain(Difficulty difficulty, Random random) {
        double randomValue = random.nextDouble(); // Get a random number between 0.0 and 1.0
        double plainsProbability = 0.0;
        double mountainProbability = 0.0;
        double desertProbability = 0.0;
        double swampProbability = 0.0;
        double forestProbability = 0.0;

        // Adjust terrain probabilities based on the game's difficulty
        switch (difficulty) {
            case EASY:
                mountainProbability = 0.4;
                desertProbability = 0.2;
                swampProbability = 0.15;
                forestProbability = 0.15;
                plainsProbability = 0.1;
                break;
            case MEDIUM:
                mountainProbability = 0.3;
                desertProbability = 0.25;
                swampProbability = 0.2;
                forestProbability = 0.15;
                plainsProbability = 0.1;
                break;
            case HARD:
                mountainProbability = 0.25;
                desertProbability = 0.25;
                swampProbability = 0.2;
                forestProbability = 0.2;
                plainsProbability = 0.1;
                break;
        }

        // Determine terrain type based on random value and probabilities
        double cumulativeProbability = 0.0;

        cumulativeProbability += plainsProbability;
        if (randomValue < cumulativeProbability) return "plains";

        cumulativeProbability += mountainProbability;
        if (randomValue < cumulativeProbability) return "mountain";

        cumulativeProbability += desertProbability;
        if (randomValue < cumulativeProbability) return "desert";

        cumulativeProbability += swampProbability;
        if (randomValue < cumulativeProbability) return "swamp";

        cumulativeProbability += forestProbability;
        if (randomValue < cumulativeProbability) return "forest";

        // Fallback terrain type
        return "plains";
    }

    /**
     * Gets the movement cost for a given terrain type.
     *
     * @param terrain The terrain type.
     * @return The movement cost associated with the terrain.
     */
    private int getMoveCost(String terrain) {
        switch (terrain) {
            case "plains": return 1;
            case "mountain": return 3;
            case "desert": return 2;
            case "swamp": return 3;
            case "forest": return 2;
            default: return 1;
        }
    }

    /**
     * Gets the water cost for a given terrain type.
     *
     * @param terrain The terrain type.
     * @return The water cost associated with the terrain.
     */
    private int getWaterCost(String terrain) {
        switch (terrain) {
            case "plains": return 1;
            case "mountain": return 2;
            case "desert": return 3;
            case "swamp": return 3;
            case "forest": return 1;
            default: return 1;
        }
    }

    /**
     * Gets the food cost for a given terrain type.
     *
     * @param terrain The terrain type.
     * @return The food cost associated with the terrain.
     */
    private int getFoodCost(String terrain) {
        switch (terrain) {
            case "plains": return 1;
            case "mountain": return 2;
            case "desert": return 2;
            case "swamp": return 2;
            case "forest": return 2;
            default: return 1;
        }
    }

    /**
     * Places items on the map squares based on difficulty and randomness.
     * This method is a placeholder and needs to be implemented with specific item placement logic.
     *
     * @param square     The Square to potentially place an item on.
     * @param difficulty The difficulty level of the game.
     * @param random     A Random object for generating random numbers.
     */
    private void placeItem(Square square, Difficulty difficulty, Random random) {
        // Implementation of item placement logic based on difficulty and terrain
        double itemChance = 0.0;
        
        // Set base chance based on difficulty - increase frequencies
        switch (difficulty) {
            case EASY:
                itemChance = 0.35; // 35% chance for EASY (was 15%)
                break;
            case MEDIUM:
                itemChance = 0.25; // 25% chance for MEDIUM (was 10%)
                break;
            case HARD:
                itemChance = 0.15; // 15% chance for HARD (was 7%)
                break;
        }
        
        // Roll for item placement
        if (random.nextDouble() < itemChance) {
            String terrain = square.getTerrain();
            
            // Logic for placing specific items based on terrain
            if (terrain.equals("plains")) {
                // Plains have more food
                if (random.nextDouble() < 0.6) {
                    square.addItem(new wss.items.FoodBonus(3, false));
                } else {
                    square.addItem(new wss.items.WaterBonus(2, false));
                }
            } else if (terrain.equals("forest")) {
                // Forests have more food and sometimes gold
                if (random.nextDouble() < 0.6) {  // increased from 0.5
                    square.addItem(new wss.items.FoodBonus(4, false));
                } else if (random.nextDouble() < 0.4) {  // increased from 0.3
                    square.addItem(new wss.items.GoldBonus(1, false));
                }
            } else if (terrain.equals("mountain")) {
                // Mountains have gold and some water
                if (random.nextDouble() < 0.7) {
                    square.addItem(new wss.items.GoldBonus(2, false));
                } else {
                    square.addItem(new wss.items.WaterBonus(3, false));  // added water to mountains
                }
            } else if (terrain.equals("desert")) {
                // Desert has little resources but more valuable
                if (random.nextDouble() < 0.3) {
                    square.addItem(new wss.items.GoldBonus(3, false));
                } else if (random.nextDouble() < 0.2) {  // added food to desert
                    square.addItem(new wss.items.FoodBonus(5, false));  // more valuable food in desert
                }
            } else if (terrain.equals("swamp")) {
                // Swamps have water and sometimes food
                if (random.nextDouble() < 0.8) {  // increased from 0.7
                    square.addItem(new wss.items.WaterBonus(3, true)); // Repeating water source
                } else {
                    square.addItem(new wss.items.FoodBonus(2, false));
                }
            }
        }
        
        // Add traders with lower probability
        double traderChance = 0.05; // 5% base chance (increased from 3%)
        if (difficulty == Difficulty.EASY) {
            traderChance = 0.08; // 8% for EASY (increased from 5%)
        }
        
        if (random.nextDouble() < traderChance) {
            // Create and place a trader
            wss.trader.Trader trader;
            int traderType = random.nextInt(3);
            
            if (traderType == 0) {
                trader = new wss.trader.FriendlyTrader();
            } else if (traderType == 1) {
                trader = new wss.trader.StrictTrader();
            } else {
                trader = new wss.trader.ImpatientTrader();
            }
            
            square.setTrader(trader);
        }
    }

    /**
     * Checks if the given coordinates (x, y) are within the bounds of the map.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return True if the coordinates are within the map bounds, false otherwise.
     */
    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < grid.length && y < grid[0].length;
    }

    /**
     * Gets the Square object at the specified coordinates (x, y).
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return The Square object at the given coordinates.
     */
    public Square getSquare(int x, int y) {
        return grid[x][y];
    }

    /**
     * Prints a text-based representation of the generated map to the console.
     * This method is useful for debugging and visualizing the map layout.
     */
    public void printMap() {
        if (grid != null) {
            for (int y = 0; y < grid[0].length; y++) {
                for (int x = 0; x < grid.length; x++) {
                    System.out.print(getTerrainRepresentation(grid[x][y].getTerrain()) + " ");
                }
                System.out.println();
            }
        } else {
            System.out.println("Map has not been generated yet.");
        }
    }

    /**
     * Gets the character representation for a given terrain type.
     *
     * @param terrain The terrain type.
     * @return The character representing the terrain.
     */
    private String getTerrainRepresentation(String terrain) {
        switch (terrain) {
            case "plains": return "P";
            case "mountain": return "M";
            case "desert": return "D";
            case "swamp": return "~";
            case "forest": return "F";
            default: return "?";
        }
    }
}