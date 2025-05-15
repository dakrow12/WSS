package wss.player;

import wss.WSSGameEngine;
import wss.game.Map;
import wss.game.*;
import wss.items.*;
import wss.trader.*;
import wss.util.GameLogger;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;


public abstract class Brain {
    protected Player player;
    protected Map map;
    protected WSSGameEngine gameEngine; // Reference to the game engine for statistics
    
    public Brain(Player player, Map map) {
        this(player, map, null);
    }
    
    public Brain(Player player, Map map, WSSGameEngine gameEngine) {
        this.player = player;
        this.map = map;
        this.gameEngine = gameEngine;
    }
    
    public void setGameEngine(WSSGameEngine engine) {
        this.gameEngine = engine;
    }
    
    public WSSGameEngine getGameEngine() {
        return this.gameEngine;
    }
    
    public abstract void makeMove();
    
    protected void movePlayer(Direction direction) {
        GameLogger.section("Movement");
        GameLogger.info("Attempting to move " + direction);
        
        if (player.canMove(direction)) {
            boolean moved = player.move(direction, map.getGrid());
            GameLogger.movement(direction.toString(), player.getX(), player.getY());
            
            if (moved) {
                collectItems();
                
                // Add additional position check for debugging
                Position pos = player.getPosition();
                GameLogger.info("Player position confirmed: (" + pos.getX() + ", " + pos.getY() + ")");
                
                checkTrader();
            }
        } else {
            GameLogger.warning("Cannot move " + direction + ", resting instead");
            player.rest(); // If can't move, rest instead
        }
    }
    
    protected void collectItems() {
        // Collect all items in current square
        Square currentSquare = map.getSquare(player.getPosition());
        List<Item> items = new ArrayList<>(currentSquare.getItems()); // Create a copy to prevent concurrent modification
        
        if (!items.isEmpty()) {
            GameLogger.info("Found items at position (" + player.getX() + ", " + player.getY() + ")");
            
            for (Item item : items) {
                String itemName = item.getClass().getSimpleName();
                
                // Track statistics for item collection
                if (gameEngine != null) {
                    if (item instanceof FoodBonus) {
                        gameEngine.trackFoodBonusCollected();
                    } else if (item instanceof WaterBonus) {
                        gameEngine.trackWaterBonusCollected();
                    } else if (item instanceof GoldBonus) {
                        gameEngine.trackGoldBonusCollected();
                    }
                }
            }
            
            // Use the Square's collectItem method to properly handle items
            currentSquare.collectItem(player);
        }
    }
    
    protected void checkTrader() {
        Square currentSquare = map.getSquare(player.getPosition());
        
        // Check if there's a trader on the square
        if (currentSquare.hasTrader()) {
            Trader trader = currentSquare.getTrader();
            
            // Track trader encounter statistics
            if (gameEngine != null) {
                gameEngine.trackTraderEncounter();
            }
            
            GameLogger.traderEncounter(trader.getPersonality().toString());
            
            // Activate the trader to begin interaction
            trader.activate(player);
            GameLogger.info("Activating " + trader.getPersonality() + " trader");
            
            // Check if we want to trade with this trader
            if (shouldTradeWith(trader) && trader.getCurrentState() == TraderState.NEGOTIATING) {
                initiateTrade(trader);
            } else {
                GameLogger.info("Decided not to trade with " + trader.getPersonality() + " trader (state: " + trader.getCurrentState() + ")");
            }
        }
    }
    
    protected boolean shouldTradeWith(Trader trader) {
        // Default implementation - override in subclasses for custom behavior
        return true;
    }
    
    protected void initiateTrade(Trader trader) {
        // Default implementation - override in subclasses for custom behavior
        GameLogger.warning("Trade initiation not implemented in this Brain type");
    }
    
    protected void rest() {
        GameLogger.section("Resting");
        GameLogger.info("Resting to recover strength");
        player.rest();
        GameLogger.success("Strength recovered: " + player.getCurrentStrength());
    }

    protected boolean isPathFeasible(Path path) {
        return path.getTotalMovementCost() <= player.getCurrentMovement() &&
               path.getTotalFoodCost() <= player.getCurrentFood() &&
               path.getTotalWaterCost() <= player.getCurrentWater();
    }

    protected void followPath(Path path) {
        for (Direction dir : path.getSteps()) {
            if (player.canMove(dir)) {
                player.move(dir, map.getGrid());
            } else {
                break;
            }
        }
    }
    
    protected boolean isCriticalResourceLevel() {
        return player.getCurrentFood() < player.getMaxFood() * 0.2 ||
               player.getCurrentWater() < player.getMaxWater() * 0.2;
    }
    
    public Map getMap() {
        return map;
    }
}

