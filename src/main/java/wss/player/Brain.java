package wss.player;

import wss.game.Map;
import wss.game.*;
import wss.items.*;
import wss.trader.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;


public abstract class Brain {
    protected Player player;
    protected Map map;
    
    public Brain(Player player, Map map) {
        this.player = player;
        this.map = map;
    }
    
    public abstract void makeMove();
    
    protected void movePlayer(Direction direction) {
        if (player.canMove(direction)) {
            player.move(direction, map.getGrid());
            collectItems();
            checkTrader();
        } else {
            player.rest(); // If can't move, rest instead
        }
    }
    
    protected void collectItems() {
        // Collect all items in current square
        Square currentSquare = map.getSquare(player.getPosition());
        for (Item item : currentSquare.getItems()) {
            player.collect(item);
        }
    }
    
    protected void checkTrader() {
        Square currentSquare = map.getSquare(player.getPosition());
        if (currentSquare.hasTrader()) {
            Trader trader = currentSquare.getTrader();
            if (shouldTradeWith(trader)) {
                initiateTrade(trader);
            }
        }
    }

protected boolean isPathFeasible(Path path) {
    return path.getTotalMovementCost() <= player.getCurrentMovement() &&
           path.getTotalFoodCost() <= player.getCurrentFood() &&
           path.getTotalWaterCost() <= player.getCurrentWater();
}

protected void followPath(Path path) {
    for (Direction dir : path.getSteps()) {
        if (player.canMove(dir)) {
            player.move(dir, map.getGrid());
        } else {
            break;
        }
    }
}

    
    protected abstract boolean shouldTradeWith(Trader trader);
    protected abstract void initiateTrade(Trader trader);
    
    protected boolean isCriticalResourceLevel() {
        return player.getCurrentFood() < player.getMaxFood() * 0.2 ||
               player.getCurrentWater() < player.getMaxWater() * 0.2;
    }
}