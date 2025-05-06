package wss.player;

import wss.game.Path;
import wss.game.Map;
import wss.game.Position;
import wss.game.Direction;
import wss.game.Square;
import wss.items.FoodBonus; 
import wss.items.WaterBonus;
import wss.items.GoldBonus;  


import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Comparator;

public abstract class Vision {

    public abstract Path closestFood(Player player, Map map);
    public abstract Path closestWater(Player player, Map map);
    public abstract Path closestGold(Player player, Map map);
    public abstract Path closestTrader(Player player, Map map);
    public abstract Path easiestPath(Player player, Map map);
    public abstract Path secondClosestFood(Player player, Map map);
    public abstract Path secondClosestWater(Player player, Map map);
    public abstract Path secondClosestGold(Player player, Map map);
    public abstract Path secondClosestTrader(Player player, Map map);

    /**
     * Common helper method for finding items using Breadth-First Search (BFS).
     * Searches within a specified range for squares matching the given predicate.
     * @param player The player performing the search.
     * @param map The game map.
     * @param predicate A condition to identify the target (e.g., has food, has trader).
     * @param maxRange The maximum depth (number of steps) to search.
     * @param maxResults The maximum number of paths to find and return.
     * @return A list of Path objects to the found items/traders, sorted by their combined cost.
     * Returns an empty list if no items are found or if inputs are invalid.
     */
    protected List<Path> findItemsBFS(Player player, Map map, ItemTypePredicate predicate, int maxRange, int maxResults) {
        List<Path> foundPaths = new ArrayList<>();
        // Basic input validation
        if (player == null || map == null || predicate == null || maxRange <= 0 || maxResults <= 0) {
            return foundPaths; // Return empty list for invalid inputs
        }

        Queue<PathSearchNode> queue = new LinkedList<>();
        Set<Position> visitedInSearch = new HashSet<>(); // Tracks visited positions for the current BFS

        Position startPos = player.getPosition();
        if (startPos == null) {
            return foundPaths; 
        }

        // Initial node: player's current position, no steps taken yet, zero cost, depth 0
        queue.add(new PathSearchNode(startPos, new ArrayList<>(), 0, 0, 0, 0));
        visitedInSearch.add(startPos);

        while (!queue.isEmpty()) {
            PathSearchNode currentNode = queue.poll();

            if (currentNode.depth > 0) { 
                Square currentSquareData = map.getSquare(currentNode.position);
                // Ensure square data is valid before testing predicate
                if (currentSquareData != null && predicate.test(currentSquareData)) {
                    foundPaths.add(new Path(
                        new ArrayList<>(currentNode.directionsToNode), // Defensive copy of directions
                        currentNode.totalMovementCost,
                        currentNode.totalWaterCost,
                        currentNode.totalFoodCost
                    ));
                    // If we only need a certain number of results, and we sort at the end,
                    // we might collect more than maxResults initially and then trim.
                   
                }
            }

            // Stop exploring from this path if it's too long (depth has reached maxRange)
            if (currentNode.depth >= maxRange) {
                continue;
            }

            // Explore neighbors
            for (Direction dir : Direction.values()) { // Iterate through all 8 directions
                Position nextPos = currentNode.position.move(dir);

                if (map.inBounds(nextPos.getX(), nextPos.getY())) {
                    if (!visitedInSearch.contains(nextPos)) { // Process each square only once per search
                        Square nextSquareData = map.getSquare(nextPos);
                        if (nextSquareData == null) continue; // Should not happen with inBounds, but defensive

                        List<Direction> newDirections = new ArrayList<>(currentNode.directionsToNode);
                        newDirections.add(dir);

                        PathSearchNode neighborNode = new PathSearchNode(
                            nextPos,
                            newDirections, // Already a new list from above
                            currentNode.totalMovementCost + nextSquareData.getMovementCost(),
                            currentNode.totalFoodCost + nextSquareData.getFoodCost(),
                            currentNode.totalWaterCost + nextSquareData.getWaterCost(),
                            currentNode.depth + 1
                        );
                        
                        visitedInSearch.add(nextPos);
                        queue.add(neighborNode);
                    }
                }
            }
        }

        // Sort paths by combined cost (movement + food + water)
        // This ensures that "closest" refers to the lowest total cost path found.
        foundPaths.sort(Comparator.comparingDouble(p -> 
            (double)p.getTotalMovementCost() + p.getTotalFoodCost() + p.getTotalWaterCost()
        ));
        
        // Return up to maxResults
        if (foundPaths.size() > maxResults) {
            return new ArrayList<>(foundPaths.subList(0, maxResults)); // Return a new list
        }
        return foundPaths;
    }

    /**
     * Helper to get the Nth path from a list (1-indexed).
     * @param paths List of paths, assumed to be sorted if "closest" matters (findItemsBFS sorts them).
     * @param n The desired rank (1 for first/closest, 2 for second closest, etc.).
     * @return The Nth path from the list, or null if the list is too short or n is invalid.
     */
    protected Path getNthPath(List<Path> paths, int n) {
        // Validate inputs: paths list should not be null, n should be positive.
        if (paths != null && n > 0 && paths.size() >= n) {
            return paths.get(n - 1); // n-1 because list is 0-indexed
        }
        return null; // Return null if the Nth path is not available
    }
}

class PathSearchNode {
    Position position;
    List<Direction> directionsToNode; // The sequence of moves from the start to this node
    int totalMovementCost;
    int totalFoodCost;
    int totalWaterCost;
    int depth; // Number of steps from the start position

    /**
     * Constructs a PathSearchNode.
     * @param position The current position of this node.
     * @param directionsToNode The list of directions taken to reach this node.
     * @param moveCost Accumulated movement cost to reach this node.
     * @param foodCost Accumulated food cost to reach this node.
     * @param waterCost Accumulated water cost to reach this node.
     * @param depth The depth of this node in the search tree (number of steps).
     */
    PathSearchNode(Position position, List<Direction> directionsToNode, int moveCost, int foodCost, int waterCost, int depth) {
        this.position = position;
        // It's crucial that directionsToNode is defensively copied if it's modified later,
        // or ensure the list passed is not modified by the caller or other branches of the search.
        // The findItemsBFS method creates new lists for each step, so this should be fine.
        this.directionsToNode = directionsToNode; 
        this.totalMovementCost = moveCost;
        this.totalFoodCost = foodCost;
        this.totalWaterCost = waterCost;
        this.depth = depth;
    }
}

/**
 * Functional interface for defining a condition to check on a Square.
 * Used by the vision logic (findItemsBFS) to identify squares
 * that contain specific items or meet certain criteria (e.g., has a trader).
 * This can be an inner interface of Vision, or a separate package-private interface.
 */
@FunctionalInterface
interface ItemTypePredicate {
    /**
     * Tests if the given square meets the defined criteria.
     * @param square The Square to test.
     * @return true if the square meets the criteria, false otherwise.
     */
    boolean test(Square square);
}
