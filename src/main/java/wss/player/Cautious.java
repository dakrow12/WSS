package wss.player;

import wss.game.Square;
import wss.game.Direction;
import wss.game.Path;
import wss.game.Map;
import wss.player.Player;
import wss.items.*;

public class Cautious extends Vision {
    @Override
    public Path closestFood(Player player, Map map) {
        int x = player.getX();
        int y = player.getY();
        
        // Check East, North, South for food
        Direction[] directions = {Direction.EAST, Direction.NORTH, Direction.SOUTH};
        
        for (Direction dir : directions) {
            int nx = x + dir.dx();
            int ny = y + dir.dy();
            
            if (map.inBounds(nx, ny)) {
                Square square = map.getSquare(nx, ny);
                if (hasFoodBonus(square)) {
                    System.out.println("Vision: Found food to the " + dir);
                    return Path.of(dir, square.getMovementCost(), square.getWaterCost(), square.getFoodCost());
                }
            }
        }
        
        return null;
    }

    @Override
    public Path closestWater(Player player, Map map) {
        int x = player.getX();
        int y = player.getY();
        
        // Check East, North, South for water
        Direction[] directions = {Direction.EAST, Direction.NORTH, Direction.SOUTH};
        
        for (Direction dir : directions) {
            int nx = x + dir.dx();
            int ny = y + dir.dy();
            
            if (map.inBounds(nx, ny)) {
                Square square = map.getSquare(nx, ny);
                if (hasWaterBonus(square)) {
                    System.out.println("Vision: Found water to the " + dir);
                    return Path.of(dir, square.getMovementCost(), square.getWaterCost(), square.getFoodCost());
                }
            }
        }
        
        return null;
    }

    @Override
    public Path closestGold(Player player, Map map) {
        int x = player.getX();
        int y = player.getY();
        
        // Check East, North, South for gold
        Direction[] directions = {Direction.EAST, Direction.NORTH, Direction.SOUTH};
        
        for (Direction dir : directions) {
            int nx = x + dir.dx();
            int ny = y + dir.dy();
            
            if (map.inBounds(nx, ny)) {
                Square square = map.getSquare(nx, ny);
                if (hasGoldBonus(square)) {
                    System.out.println("Vision: Found gold to the " + dir);
                    return Path.of(dir, square.getMovementCost(), square.getWaterCost(), square.getFoodCost());
                }
            }
        }
        
        return null;
    }

    @Override
    public Path closestTrader(Player player, Map map) {
        int x = player.getX();
        int y = player.getY();
        
        // Check East, North, South for traders
        Direction[] directions = {Direction.EAST, Direction.NORTH, Direction.SOUTH};
        
        for (Direction dir : directions) {
            int nx = x + dir.dx();
            int ny = y + dir.dy();
            
            if (map.inBounds(nx, ny)) {
                Square square = map.getSquare(nx, ny);
                if (square.hasTrader()) {
                    System.out.println("Vision: Found trader to the " + dir);
                    return Path.of(dir, square.getMovementCost(), square.getWaterCost(), square.getFoodCost());
                }
            }
        }
        
        return null;
    }

    @Override
    public Path easiestPath(Player player, Map map) {
        int x = player.getX();
        int y = player.getY();

        Direction[] dirs = {Direction.EAST, Direction.NORTH, Direction.SOUTH};
        Direction bestDir = null;
        int lowestCost = Integer.MAX_VALUE;
        Square bestSquare = null;
        
        for (Direction dir : dirs) {
            int nx = x + dir.dx();
            int ny = y + dir.dy();
            if (map.inBounds(nx, ny)) {
                Square s = map.getSquare(nx, ny);
                int totalCost = s.getMovementCost() + s.getWaterCost() + s.getFoodCost();
                if (totalCost < lowestCost) {
                    lowestCost = totalCost;
                    bestDir = dir;
                    bestSquare = s;
                }
            }
        }
        
        if (bestDir != null) {
            return Path.of(bestDir, bestSquare.getMovementCost(), bestSquare.getWaterCost(), bestSquare.getFoodCost());
        }

        return null;
    }

    @Override
    public Path secondClosestFood(Player player, Map map) {
        // Implementation would be similar to closestFood but returning the second match
        return null;
    }

    @Override
    public Path secondClosestWater(Player player, Map map) {
        // Implementation would be similar to closestWater but returning the second match
        return null;
    }

    @Override
    public Path secondClosestGold(Player player, Map map) {
        // Implementation would be similar to closestGold but returning the second match
        return null;
    }

    @Override
    public Path secondClosestTrader(Player player, Map map) {
        // Implementation would be similar to closestTrader but returning the second match
        return null;
    }
    
    // Helper methods
    private boolean hasFoodBonus(Square square) {
        for (Item item : square.getItems()) {
            if (item instanceof FoodBonus) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasWaterBonus(Square square) {
        for (Item item : square.getItems()) {
            if (item instanceof WaterBonus) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasGoldBonus(Square square) {
        for (Item item : square.getItems()) {
            if (item instanceof GoldBonus) {
                return true;
            }
        }
        return false;
    }
}
