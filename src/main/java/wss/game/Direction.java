package wss.game;
import java.util.List;


public enum Direction {
    NORTH(0, -1),
    SOUTH(0, 1),
    EAST(1, 0),
    WEST(-1, 0),
    NORTHEAST(1, -1),
    NORTHWEST(-1, -1),
    SOUTHEAST(1, 1),
    SOUTHWEST(-1, 1);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int dx() { return dx; }
    public int dy() { return dy; }



	public static List<Direction> eastwardDirections() {
    return List.of(EAST, NORTHEAST, SOUTHEAST);
}

public boolean isEastward() {
    return this == EAST || this == NORTHEAST || this == SOUTHEAST;
}



}




