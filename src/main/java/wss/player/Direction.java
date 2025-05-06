package wss.player;

import java.util.List;
import java.util.Arrays; // For Arrays.asList if used, though List.of is preferred for Java 9+

/**
 * Represents the 8 cardinal and ordinal directions on the game map.
 * Each direction has a delta-x (dx) and delta-y (dy) component
 * indicating the change in coordinates when moving in that direction.
 */
public enum Direction {
    NORTH(0, -1),
    NORTHEAST(1, -1),
    EAST(1, 0),
    SOUTHEAST(1, 1),
    SOUTH(0, 1),
    SOUTHWEST(-1, 1),
    WEST(-1, 0),
    NORTHWEST(-1, -1);

    private final int dx;
    private final int dy;

    /**
     * Private constructor for the enum.
     * @param dx The change in x-coordinate for this direction.
     * @param dy The change in y-coordinate for this direction.
     */
    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Gets the change in the x-coordinate for this direction.
     * @return The delta-x value.
     */
    public int dx() {
        return dx;
    }

    /**
     * Gets the change in the y-coordinate for this direction.
     * @return The delta-y value.
     */
    public int dy() {
        return dy;
    }

    /**
     * Checks if this direction is primarily eastward (East, Northeast, or Southeast).
     * @return true if the direction is eastward, false otherwise.
     */
    public boolean isEastward() {
        return this == EAST || this == NORTHEAST || this == SOUTHEAST;
    }

    /**
     * Provides a list of all eastward directions.
     * Useful for AI logic that prioritizes eastward movement.
     * @return An unmodifiable list of eastward directions.
     */
    public static List<Direction> eastwardDirections() {
        // Using List.of for an unmodifiable list (Java 9+)
        // For older Java versions, you might use:
        // return Collections.unmodifiableList(Arrays.asList(EAST, NORTHEAST, SOUTHEAST));
        return List.of(EAST, NORTHEAST, SOUTHEAST);
    }

    // Optional: A method to get the opposite direction
    /**
     * Gets the opposite direction to this one.
     * For example, NORTH.opposite() would return SOUTH.
     * @return The opposite Direction.
     */
    public Direction opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case NORTHEAST -> SOUTHWEST;
            case EAST -> WEST;
            case SOUTHEAST -> NORTHWEST;
            case SOUTH -> NORTH;
            case SOUTHWEST -> NORTHEAST;
            case WEST -> EAST;
            case NORTHWEST -> SOUTHEAST;
            // default -> null; // Should not happen for an enum
        };
    }
}
