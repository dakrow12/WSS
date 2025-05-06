package wss.game;

/**
 * Immutable grid coordinate.
 */
public class Position {
    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // ----------------------------------
    // Accessors
    // ----------------------------------
    public int getX() { return x; }
    public int getY() { return y; }

    // ----------------------------------
    // Movement helpers
    // ----------------------------------
    /** Returns a new Position moved one step in the given direction. */
    public Position move(Direction dir) {
        return new Position(x + dir.dx(), y + dir.dy());
    }

    /**
     * Returns the Direction from this position to {@code target}.
     * Assumes 8‑way movement (rook + diagonal) and that target is
     * exactly one step away.
     */
    public Direction directionTo(Position target) {
        int dx = Integer.compare(target.x, x);
        int dy = Integer.compare(target.y, y);
        for (Direction d : Direction.values())
            if (d.dx() == dx && d.dy() == dy)
                return d;
        throw new IllegalArgumentException(
                "No matching direction for delta (" + dx + "," + dy + ')');
    }

    // ----------------------------------
    // Boilerplate
    // ----------------------------------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
