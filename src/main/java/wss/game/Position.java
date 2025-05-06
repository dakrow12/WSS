package wss.game;

import wss.game.Direction;

public class Position {
    public final int x, y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position move(Direction dir) {
        return new Position(x + dir.dx(), y + dir.dy());
    }

    public int getX() { return x; }
    public int getY() { return y; }
}
