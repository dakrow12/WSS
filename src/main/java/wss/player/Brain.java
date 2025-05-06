package wss.player;

import wss.game.Map;
import wss.game.Path;
import wss.game.Square;
import wss.game.Direction;
import wss.game.Position; // Often needed for map interactions
import wss.items.Item;    // If Brain directly interacts with general items
import wss.trader.Trader; // For trading logic

import java.util.List; // If iterating over paths or directions

/**
 * Abstract class representing the decision-making logic for a Player.
 * Subclasses will implement specific behaviors (e.g., greedy, cautious).
 */
public abstract class Brain {
    protected Player player;
    protected Map map;

    public Brain(Player player, Map map) {
        this.player = player;
        this.map = map;
    }

    public abstract void makeMove();

    protected void movePlayer(Direction direction) {
        if (player == null || direction == null || map == null || map.getGrid() == null) {
            System.err.println("Error in Brain.movePlayer: Null parameter detected.");
            if (player != null) player.rest(); // Rest if critical components are missing for a move
            return;
        }

        if (player.move(direction, map.getGrid())) {
            collectItems(); // Collect items on the new square
            checkTrader();  // Check for traders on the new square
        } else {
            // If move failed (e.g., insufficient resources, blocked path, out of bounds handled by player.move)
            player.rest(); 
        }
    }

    /**
     * Triggers item collection for all items on the player's current square.
     */
    protected void collectItems() {
        if (player == null || map == null) return;
        Position playerPos = player.getPosition();
        if (playerPos == null) return;

        Square currentSquare = map.getSquare(playerPos);
        if (currentSquare != null) {
            // The Square.collectItem(Player) method handles activating and removing items.
            currentSquare.collectItem(player); 
        }
    }

    protected void checkTrader() {
        if (player == null || map == null) return;
        Position playerPos = player.getPosition();
        if (playerPos == null) return;

        Square currentSquare = map.getSquare(playerPos);
        if (currentSquare != null && currentSquare.hasTrader()) {
            Trader trader = currentSquare.getTrader();
            if (trader != null && shouldTradeWith(trader)) {
                initiateTrade(trader);
            }
        }
    }

    protected boolean isPathFeasible(Path path) {
        if (player == null || path == null) {
            return false;
        }
        return path.getTotalMovementCost() <= player.getCurrentStrength() &&
               path.getTotalFoodCost() <= player.getCurrentFood() &&
               path.getTotalWaterCost() <= player.getCurrentWater();
    }

    protected void followPath(Path path) {
        if (player == null || path == null || path.getDirections() == null || map == null || map.getGrid() == null) {
            return;
        }
        for (Direction dir : path.getDirections()) { // Path.java should have getDirections() or getSteps()
            if (dir == null) continue;
            
            // Store current state to see if movePlayer actually moved or just rested.
            Position posBeforeStep = player.getPosition();
            movePlayer(dir);
            Position posAfterStep = player.getPosition();

            // If player didn't move (e.g. rested due to failed move), then break path following.
            if (posBeforeStep.equals(posAfterStep) && !player.move(dir, map.getGrid())) { // Double check if stuck
                 break; // Can't continue path
            }
           
        }
    }
    
    protected abstract boolean shouldTradeWith(Trader trader);

    protected abstract void initiateTrade(Trader trader);
    
    protected boolean isCriticalResourceLevel() {
        if (player == null) return true; // Assume critical if player is null
        // Using a threshold (e.g., 20% of max) to define "critical"
        boolean foodCritical = player.getCurrentFood() < player.getMaxFood() * 0.2;
        boolean waterCritical = player.getCurrentWater() < player.getMaxWater() * 0.2;
        return foodCritical || waterCritical;
    }
}
