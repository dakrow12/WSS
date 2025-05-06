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
// Comparator, Queue, LinkedList, Set, HashSet are used by findItemsBFS in Vision.java

/**
 * Focused vision for a player.
 * This vision type has a general sight range but an enhanced range or priority
 * for a specific "focused" resource, which is Gold in this implementation.
 * It inherits common pathfinding logic from the abstract Vision class.
 */
public class Focused extends Vision {
    // Constants defining the behavior of this vision type
    private static final int GENERAL_VISION_RANGE = 4;          // Standard sight range for non-focused resources
    private static final int FOCUSED_RESOURCE_VISION_RANGE = 6; // Extended sight range for Gold
    private static final double EASTWARD_BIAS_FACTOR = 0.9;     // Multiplier to make eastward paths slightly more attractive (cost * 0.9)

    @Override
    public Path closestGold(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            sq -> sq.getItems().stream().anyMatch(it -> it instanceof GoldBonus), 
            FOCUSED_RESOURCE_VISION_RANGE, 1); 
        return getNthPath(paths, 1); // getNthPath is a helper from Vision superclass
    }


    @Override
    public Path secondClosestGold(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            sq -> sq.getItems().stream().anyMatch(it -> it instanceof GoldBonus), 
            FOCUSED_RESOURCE_VISION_RANGE, 2); 
        return getNthPath(paths, 2); 
    }

    @Override
    public Path closestFood(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            sq -> sq.getItems().stream().anyMatch(it -> it instanceof FoodBonus), 
            GENERAL_VISION_RANGE, 1);
        return getNthPath(paths, 1);
    }

    @Override
    public Path closestWater(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            sq -> sq.getItems().stream().anyMatch(it -> it instanceof WaterBonus), 
            GENERAL_VISION_RANGE, 1);
        return getNthPath(paths, 1);
    }

    @Override
    public Path closestTrader(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            Square::hasTrader, 
            GENERAL_VISION_RANGE, 1);
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

        for (Direction dir : Direction.values()) {
            if (dir == null) continue; 

            Position nextPos = currentPos.move(dir); 
            if (nextPos == null) continue; 

            if (map.inBounds(nextPos.getX(), nextPos.getY())) {
                Square nextSquare = map.getSquare(nextPos); 
                
                if (nextSquare == null) {
                     System.err.println("Warning: map.getSquare returned null for in-bounds position: " + nextPos + " in Focused.easiestPath");
                    continue; 
                }
                
                double pathCost = nextSquare.getMovementCost() + nextSquare.getFoodCost() + nextSquare.getWaterCost();
                
                double effectiveCost = pathCost; 
                if (dir.isEastward()) {
                    effectiveCost *= EASTWARD_BIAS_FACTOR; 
                }

                if (effectiveCost < lowestEffectiveCost) {
                    lowestEffectiveCost = effectiveCost;
                    bestPath = new Path(List.of(dir), nextSquare.getMovementCost(), nextSquare.getWaterCost(), nextSquare.getFoodCost());
                } 
                else if (effectiveCost == lowestEffectiveCost && dir.isEastward()) {
                    if (bestPath == null || 
                        (bestPath.getDirections() != null && !bestPath.getDirections().isEmpty() && !bestPath.getDirections().get(0).isEastward()) ) {
                        bestPath = new Path(List.of(dir), nextSquare.getMovementCost(), nextSquare.getWaterCost(), nextSquare.getFoodCost());
                    }
                }
            }
        }
        return bestPath; 
    }

    @Override
    public Path secondClosestFood(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            sq -> sq.getItems().stream().anyMatch(it -> it instanceof FoodBonus), 
            GENERAL_VISION_RANGE, 2);
        return getNthPath(paths, 2);
    }

    @Override
    public Path secondClosestWater(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            sq -> sq.getItems().stream().anyMatch(it -> it instanceof WaterBonus), 
            GENERAL_VISION_RANGE, 2);
        return getNthPath(paths, 2);
    }

    @Override
    public Path secondClosestTrader(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            Square::hasTrader, 
            GENERAL_VISION_RANGE, 2);
        return getNthPath(paths, 2);
    }
}
