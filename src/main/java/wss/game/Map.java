package wss.game;

public class Map {
   
    public Square[][] getGrid() {
    return grid;
    }


    public Map(Square[][] grid) {
        this.grid = grid;
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < grid.length && y < grid[0].length;
    }

    public Square getSquare(int x, int y) {
        return grid[x][y];
    }
}
