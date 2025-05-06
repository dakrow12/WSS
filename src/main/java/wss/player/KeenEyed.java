package wss.player;

// Import necessary classes from the game's structure
import wss.game.Direction;
import wss.game.Map;
import wss.game.Path;
import wss.game.Position;
import wss.game.Square;

// Import specific item types to check for
import wss.items.FoodBonus;
import wss.items.GoldBonus;
import wss.items.WaterBonus;

// Import Java utility classes
import java.util.ArrayList; // Though findItemsBFS returns List, Path constructor might use ArrayList
import java.util.List;
// java.util.Comparator, Queue, LinkedList, Set, HashSet are used by findItemsBFS in Vision.java

/**
 * KeenEyed vision for a player.
 * This vision type allows players to see a moderate distance.
 * It inherits common pathfinding logic from the abstract Vision class.
 */
public class KeenEyed extends Vision {
    // Constants defining the behavior of this vision type
    private static final int VISION_RANGE = 4; // KeenEyed players can see a bit further (e.g., 4 squares).
    private static final double EASTWARD_BIAS_FACTOR = 0.9; // Multiplier to make eastward paths slightly more attractive (cost * 0.9)

    /**
     * Finds the closest path to a FoodBonus item within the vision range.
     * @param player The player performing the search.
     * @param map The game map.
     * @return A Path object to the closest food, or null if none is found.
     */
    @Override
    public Path closestFood(Player player, Map map) {
        // Use the findItemsBFS method from the Vision superclass.
        // Predicate: sq -> sq.getItems().stream().anyMatch(it -> it instanceof FoodBonus)
        //   - Checks if any item on the square 'sq' is an instance of FoodBonus.
        // VISION_RANGE: The maximum search depth.
        // 1: We only want the single closest path.
        List<Path> paths = findItemsBFS(player, map, 
            sq -> sq.getItems().stream().anyMatch(it -> it instanceof FoodBonus), 
            VISION_RANGE, 1);
        return getNthPath(paths, 1); // getNthPath is a helper from Vision superclass (1 for first path)
    }

    /**
     * Finds the closest path to a WaterBonus item within the vision range.
     * @param player The player performing the search.
     * @param map The game map.
     * @return A Path object to the closest water, or null if none is found.
     */
    @Override
    public Path closestWater(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            sq -> sq.getItems().stream().anyMatch(it -> it instanceof WaterBonus), 
            VISION_RANGE, 1);
        return getNthPath(paths, 1);
    }

    /**
     * Finds the closest path to a GoldBonus item within the vision range.
     * @param player The player performing the search.
     * @param map The game map.
     * @return A Path object to the closest gold, or null if none is found.
     */
    @Override
    public Path closestGold(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            sq -> sq.getItems().stream().anyMatch(it -> it instanceof GoldBonus), 
            VISION_RANGE, 1);
        return getNthPath(paths, 1);
    }

    /**
     * Finds the closest path to a Trader within the vision range.
     * @param player The player performing the search.
     * @param map The game map.
     * @return A Path object to the closest trader, or null if none is found.
     */
    @Override
    public Path closestTrader(Player player, Map map) {
        // Square::hasTrader is a method reference, equivalent to sq -> sq.hasTrader()
        List<Path> paths = findItemsBFS(player, map, 
            Square::hasTrader, 
            VISION_RANGE, 1);
        return getNthPath(paths, 1);
    }

    /**
     * Finds the "easiest" immediately adjacent path.
     * "Easiest" is determined by the sum of movement, food, and water costs of the next square.
     * Applies a bias towards eastward movement.
     * @param player The player.
     * @param map The game map.
     * @return A Path object for the best single step, or null if no valid moves.
     */
    @Override
    public Path easiestPath(Player player, Map map) {
        Path bestPath = null;
        double lowestEffectiveCost = Double.MAX_VALUE; // Initialize with a very high cost
        Position currentPos = player.getPosition();    // Get player's current position

        // Iterate through all possible directions (N, S, E, W, NE, NW, SE, SW)
        for (Direction dir : Direction.values()) {
            Position nextPos = currentPos.move(dir); // Calculate the potential next position

            // Check if the next position is within the map boundaries
            if (map.inBounds(nextPos.getX(), nextPos.getY())) {
                Square nextSquare = map.getSquare(nextPos); // Get the Square object for the next position
                if (nextSquare == null) continue; // Should not happen if map is consistent

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
        return bestPath; // Return the determined best path (can be null if no moves are possible)
    }

    /**
     * Finds the second closest path to a FoodBonus item within the vision range.
     * @param player The player.
     * @param map The game map.
     * @return Path to the second closest food, or null if fewer than two are found.
     */
    @Override
    public Path secondClosestFood(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            sq -> sq.getItems().stream().anyMatch(it -> it instanceof FoodBonus), 
            VISION_RANGE, 2); // Request up to 2 paths
        return getNthPath(paths, 2);  // Get the second one (if it exists)
    }

    /**
     * Finds the second closest path to a WaterBonus item within the vision range.
     * @param player The player.
     * @param map The game map.
     * @return Path to the second closest water, or null if fewer than two are found.
     */
    @Override
    public Path secondClosestWater(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            sq -> sq.getItems().stream().anyMatch(it -> it instanceof WaterBonus), 
            VISION_RANGE, 2);
        return getNthPath(paths, 2);
    }

    /**
     * Finds the second closest path to a GoldBonus item within the vision range.
     * @param player The player.
     * @param map The game map.
     * @return Path to the second closest gold, or null if fewer than two are found.
     */
    @Override
    public Path secondClosestGold(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            sq -> sq.getItems().stream().anyMatch(it -> it instanceof GoldBonus), 
            VISION_RANGE, 2);
        return getNthPath(paths, 2);
    }

    /**
     * Finds the second closest path to a Trader within the vision range.
     * @param player The player.
     * @param map The game map.
     * @return Path to the second closest trader, or null if fewer than two are found.
     */
    @Override
    public Path secondClosestTrader(Player player, Map map) {
        List<Path> paths = findItemsBFS(player, map, 
            Square::hasTrader, 
            VISION_RANGE, 2);
        return getNthPath(paths, 2);
    }
}
