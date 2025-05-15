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
        
        // Place traders after all squares are created
        placeTraders(difficulty, random);
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
                // New MEDIUM difficulty between EASY and previous MEDIUM (now HARD)
                mountainProbability = 0.35;
                desertProbability = 0.22;
                swampProbability = 0.17;
                forestProbability = 0.15;
                plainsProbability = 0.1;
                break;
            case HARD:
                // Use previous MEDIUM terrain distribution
                mountainProbability = 0.3;
                desertProbability = 0.25;
                swampProbability = 0.2;
                forestProbability = 0.15;
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
        
        // Set base chance based on difficulty
        switch (difficulty) {
            case EASY:
                itemChance = 0.40; // 40% chance
                break;
            case MEDIUM:
                itemChance = 0.39; // Between EASY and previous MEDIUM (now HARD)
                break;
            case HARD:
                itemChance = 0.38; // Previous MEDIUM chance, should give ~50% win rate
                break;
        }
        
        // Roll for item placement
        if (random.nextDouble() < itemChance) {
            String terrain = square.getTerrain();
            
            // Terrain-specific multipliers for difficulty levels
            double quantityMultiplier = 1.0;
            if (difficulty == Difficulty.MEDIUM) {
                quantityMultiplier = 1.15; // 15% more resources on new MEDIUM
            } else if (difficulty == Difficulty.HARD) {
                quantityMultiplier = 1.2; // Previous MEDIUM multiplier (20%)
            }
            
            // Logic for placing specific items based on terrain
            if (terrain.equals("plains")) {
                // Plains have more food
                if (random.nextDouble() < 0.6) {
                    int foodAmount = (int)(4 * quantityMultiplier);
                    square.addItem(new wss.items.FoodBonus(foodAmount, false));
                } else {
                    int waterAmount = (int)(3 * quantityMultiplier);
                    square.addItem(new wss.items.WaterBonus(waterAmount, false));
                }
            } else if (terrain.equals("forest")) {
                // Forests have more food and sometimes gold
                if (random.nextDouble() < 0.6) {
                    int foodAmount = (int)(5 * quantityMultiplier);
                    square.addItem(new wss.items.FoodBonus(foodAmount, false));
                } else if (random.nextDouble() < 0.5) {
                    square.addItem(new wss.items.GoldBonus(2, false));
                } else {
                    int waterAmount = (int)(3 * quantityMultiplier);
                    square.addItem(new wss.items.WaterBonus(waterAmount, false));
                }
            } else if (terrain.equals("mountain")) {
                // Mountains have gold and some water
                if (random.nextDouble() < 0.7) {
                    square.addItem(new wss.items.GoldBonus(3, false));
                } else {
                    int waterAmount = (int)(4 * quantityMultiplier);
                    square.addItem(new wss.items.WaterBonus(waterAmount, false));
                }
            } else if (terrain.equals("desert")) {
                // Desert has little resources but more valuable
                if (random.nextDouble() < 0.4) {
                    square.addItem(new wss.items.GoldBonus(3, false));
                } else if (random.nextDouble() < 0.3) {
                    int foodAmount = (int)(6 * quantityMultiplier);
                    square.addItem(new wss.items.FoodBonus(foodAmount, false));
                } else {
                    // For MEDIUM difficulty, add more frequently repeating water sources
                    boolean repeating = random.nextDouble() < 0.3 || difficulty == Difficulty.MEDIUM;
                    int waterAmount = (int)(3 * quantityMultiplier);
                    square.addItem(new wss.items.WaterBonus(waterAmount, repeating));
                }
            } else if (terrain.equals("swamp")) {
                // Swamps have water and sometimes food
                if (random.nextDouble() < 0.8) {
                    // Higher chance of repeating water sources for MEDIUM
                    boolean repeating = random.nextDouble() < 0.4 || difficulty == Difficulty.MEDIUM;
                    int waterAmount = (int)(4 * quantityMultiplier);
                    square.addItem(new wss.items.WaterBonus(waterAmount, repeating));
                } else if (random.nextDouble() < 0.6) {
                    int foodAmount = (int)(3 * quantityMultiplier);
                    square.addItem(new wss.items.FoodBonus(foodAmount, false));
                } else {
                    square.addItem(new wss.items.GoldBonus(2, false));
                }
            }
            
            // Small chance for additional item on any terrain - adjusted for difficulties
            double additionalItemChance = 0.10;
            if (difficulty == Difficulty.MEDIUM) {
                additionalItemChance = 0.125; // Between base and previous MEDIUM
            } else if (difficulty == Difficulty.HARD) {
                additionalItemChance = 0.15; // Previous MEDIUM chance
            }
            
            if (random.nextDouble() < additionalItemChance) {
                if (random.nextDouble() < 0.4) {
                    int foodAmount = (int)(2 * quantityMultiplier);
                    square.addItem(new wss.items.FoodBonus(foodAmount, false));
                } else if (random.nextDouble() < 0.7) {
                    int waterAmount = (int)(2 * quantityMultiplier);
                    square.addItem(new wss.items.WaterBonus(waterAmount, false));
                } else {
                    square.addItem(new wss.items.GoldBonus(1, false));
                }
            }
        }
        
        // We'll handle trader placement separately in generateMap method
    }

    /**
     * Places traders randomly on the map based on the total number of tiles.
     * 
     * @param difficulty The difficulty level affecting trader frequency
     * @param random A Random object for generating random numbers
     */
    private void placeTraders(Difficulty difficulty, Random random) {
        // Calculate number of traders based on map size
        int totalTiles = width * height;
        double traderPercentage = 0.0;
        
        // Set percentage based on difficulty
        switch (difficulty) {
            case EASY:
                traderPercentage = 0.10; // 10% of tiles have traders
                break;
            case MEDIUM:
                traderPercentage = 0.095; // Between EASY and previous MEDIUM (now HARD)
                break;
            case HARD:
                traderPercentage = 0.09; // Previous MEDIUM percentage
                break;
        }
        
        int numTraders = (int)(totalTiles * traderPercentage);
        // Ensure minimum traders based on difficulty
        numTraders = Math.max(numTraders, difficulty == Difficulty.HARD ? 5 : 
                                  (difficulty == Difficulty.MEDIUM ? 4 : 3));
        
        System.out.println("Placing " + numTraders + " traders on map...");
        
        // First, guaranteed place 1-2 traders in the western third of the map
        int westernThird = Math.max(1, width / 3);
        int tradersPlacedWest = 0;
        int maxWesternTraders = Math.min(2, numTraders - 1); // Ensure at least 1 trader elsewhere
        
        while (tradersPlacedWest < maxWesternTraders) {
            int x = random.nextInt(westernThird); // Western third only
            int y = random.nextInt(height);
            
            if (!grid[x][y].hasTrader()) {
                wss.trader.TraderType traderType = getRandomTraderType(random);
                grid[x][y].setTrader(new wss.trader.Trader(traderType));
                System.out.println("Placed " + traderType + " trader at western area (" + x + ", " + y + ")");
                tradersPlacedWest++;
            }
        }
        
        // Now place the remaining traders randomly across the map
        int remainingTraders = numTraders - tradersPlacedWest;
        for (int i = 0; i < remainingTraders; i++) {
            int x, y;
            int attempts = 0;
            boolean placed = false;
            
            // Try to place trader, with a limit on attempts
            while (!placed && attempts < 50) {
                x = random.nextInt(width);
                y = random.nextInt(height);
                
                if (!grid[x][y].hasTrader()) {
                    wss.trader.TraderType traderType = getRandomTraderType(random);
                    grid[x][y].setTrader(new wss.trader.Trader(traderType));
                    System.out.println("Placed " + traderType + " trader at (" + x + ", " + y + ")");
                    placed = true;
                }
                attempts++;
            }
        }
    }
    
    /**
     * Helper method to get a random trader type
     */
    private wss.trader.TraderType getRandomTraderType(Random random) {
        int typeRoll = random.nextInt(4);
        switch (typeRoll) {
            case 0:
                return wss.trader.TraderType.FAIR;
            case 1:
                return wss.trader.TraderType.GREEDY;
            case 2:
                return wss.trader.TraderType.CAUTIOUS;
            default:
                return wss.trader.TraderType.GENEROUS;
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