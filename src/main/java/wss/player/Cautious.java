package wss.player;

import wss.game.Direction;
import wss.game.Map;
import wss.game.Path;
import wss.game.Position;
import wss.game.Square;
import wss.items.FoodBonus;
import wss.items.GoldBonus;
import wss.items.WaterBonus;

import java.util.ArrayList; // Used by Path constructor if List.of is not available/preferred
import java.util.List;

public class Cautious extends Vision {
    // Constants defining the behavior of this vision type
    private static final int VISION_RANGE = 1; // Cautious players only look at immediately adjacent squares.
    private static final double EASTWARD_BIAS_FACTOR = 0.95; // Slight preference for eastward paths (cost * 0.95)

    @Override
    public Path closestFood(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            sq -> sq.getItems().stream().anyMatch(it -> it instanceof FoodBonus), 
            VISION_RANGE, 1);
        return getNthPath(paths, 1); // getNthPath is a helper from Vision superclass
    }

    @Override
    public Path closestWater(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            sq -> sq.getItems().stream().anyMatch(it -> it instanceof WaterBonus), 
            VISION_RANGE, 1);
        return getNthPath(paths, 1);
    }

    @Override
    public Path closestGold(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            sq -> sq.getItems().stream().anyMatch(it -> it instanceof GoldBonus), 
            VISION_RANGE, 1);
        return getNthPath(paths, 1);
    }

    @Override
    public Path closestTrader(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            Square::hasTrader, // Method reference for checking if a square has a trader
            VISION_RANGE, 1);
        return getNthPath(paths, 1);
    }

    @Override
    public Path easiestPath(Player player, Map map) {
        // Basic null checks for robustness
        if (player == null || map == null) {
            return null;
        }
        Position currentPos = player.getPosition();
        if (currentPos == null) {
            return null;
        }

        Path bestPath = null;
        double lowestEffectiveCost = Double.MAX_VALUE; // Initialize with a very high cost

        // Iterate through all possible directions (N, S, E, W, NE, NW, SE, SW)
        for (Direction dir : Direction.values()) {
            if (dir == null) continue; // Should not happen with Direction.values()

            Position nextPos = currentPos.move(dir); 
            if (nextPos == null) continue; // Should not happen if currentPos and dir are valid

            // Check if the next position is within the map boundaries
            if (map.inBounds(nextPos.getX(), nextPos.getY())) {
                Square nextSquare = map.getSquare(nextPos); // Get the Square object for the next position
                
                // Defensive null check for nextSquare, though ideally, if inBounds is true,
                // getSquare should return a valid Square object.
                if (nextSquare == null) {
                    continue; 
                }
                
                // Calculate the raw cost of moving to the next square
                double pathCost = nextSquare.getMovementCost() + nextSquare.getFoodCost() + nextSquare.getWaterCost();
                
                double effectiveCost = pathCost; // Start with raw cost
                // Apply bias if the direction is eastward
                if (dir.isEastward()) {
                    effectiveCost *= EASTWARD_BIAS_FACTOR; // Reduce cost slightly for eastward moves
                }

                // If this path is cheaper than the current best, update bestPath
                if (effectiveCost < lowestEffectiveCost) {
                    lowestEffectiveCost = effectiveCost;
                    // Create a new Path object for this single step
                    bestPath = new Path(List.of(dir), nextSquare.getMovementCost(), nextSquare.getWaterCost(), nextSquare.getFoodCost());
                } 
                // Tie-breaking: if costs are equal, prefer an eastward path if current best isn't already eastward
                else if (effectiveCost == lowestEffectiveCost && dir.isEastward()) {
                    // Check if bestPath is null or if its first direction is not eastward
                    if (bestPath == null || 
                        (bestPath.getDirections() != null && !bestPath.getDirections().isEmpty() && !bestPath.getDirections().get(0).isEastward()) ) {
                        bestPath = new Path(List.of(dir), nextSquare.getMovementCost(), nextSquare.getWaterCost(), nextSquare.getFoodCost());
                    }
                }
            }
        }
        return bestPath; // Return the determined best path (can be null)
    }

    @Override
    public Path secondClosestFood(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            sq -> sq.getItems().stream().anyMatch(it -> it instanceof FoodBonus), 
            VISION_RANGE, 2); // Request up to 2 paths
        return getNthPath(paths, 2);  // Get the second one (if it exists)
    }

    @Override
    public Path secondClosestWater(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            sq -> sq.getItems().stream().anyMatch(it -> it instanceof WaterBonus), 
            VISION_RANGE, 2);
        return getNthPath(paths, 2);
    }

    @Override
    public Path secondClosestGold(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            sq -> sq.getItems().stream().anyMatch(it -> it instanceof GoldBonus), 
            VISION_RANGE, 2);
        return getNthPath(paths, 2);
    }

    @Override
    public Path secondClosestTrader(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            Square::hasTrader, 
            VISION_RANGE, 2);
        return getNthPath(paths, 2);
    }
}
