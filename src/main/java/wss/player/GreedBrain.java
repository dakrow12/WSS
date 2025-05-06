package wss.player;

import wss.player.Player;
import wss.game.Map;
import wss.game.*;
import wss.items.*;
import wss.trader.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import wss.trader.TradeResponse;
import wss.game.Square;
import wss.trader.Trader;
import wss.trader.TradeOffer;


public class GreedBrain extends Brain {
    public GreedBrain(Player player, Map map) {
        super(player, map);
    }
    
    @Override
    public void makeMove() {
        if (isCriticalResourceLevel()) {
            handleCriticalResources();
            return;
        }
        
        Path closestGold = player.getVision().closestGold(player, map);
        if (closestGold != null && isPathFeasible(closestGold)) {
            followPath(closestGold);
        } else {
            moveEastOrRandom();
            Square currentSquare = map.getSquare(player.getX(), player.getY());
            currentSquare.collectItem(player);

        }
    }
    
    private void handleCriticalResources() {
        if (player.getCurrentFood() < player.getMaxFood() * 0.2) {
            Path closestFood = player.getVision().closestFood(player, map);
            if (closestFood != null && isPathFeasible(closestFood)) {
                followPath(closestFood);
                return;
            }
        }
        
        if (player.getCurrentWater() < player.getMaxWater() * 0.2) {
            Path closestWater = player.getVision().closestWater(player, map);
            if (closestWater != null && isPathFeasible(closestWater)) {
                followPath(closestWater);
                return;
            }
        }
        
        // If no resources nearby, rest to recover movement
        player.rest();
    }
    
    private void moveEastOrRandom() {
        // Try to move east if possible
        if (player.canMove(Direction.EAST)) {
            movePlayer(Direction.EAST);
        } else {
            // Otherwise move in a random direction that's not west
            Direction[] possibleDirections = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, 
                                            Direction.SOUTHEAST, Direction.SOUTH};
            List<Direction> validDirections = new ArrayList<>();
            
            for (Direction dir : possibleDirections) {
                if (player.canMove(dir)) {
                    validDirections.add(dir);
                }
            }
            
            if (!validDirections.isEmpty()) {
                Random rand = new Random();
                Direction randomDir = validDirections.get(rand.nextInt(validDirections.size()));
                movePlayer(randomDir);
            } else {
                player.rest();
            }
        }
    }
    
    @Override
    protected boolean shouldTradeWith(Trader trader) {
        // Only trade if we can get gold or if we have excess resources
        return (player.getCurrentGold() > 5 || 
                player.getCurrentFood() > player.getMaxFood() * 0.8 ||
                player.getCurrentWater() > player.getMaxWater() * 0.8);
    }
    
    @Override
    protected void initiateTrade(Trader trader) {
        // Offer excess resources for gold
        TradeOffer offer = new TradeOffer();
        
        if (player.getCurrentFood() > player.getMaxFood() * 0.8) {
            offer.setFoodOffered((int)(player.getCurrentFood() - player.getMaxFood() * 0.5));
            offer.setGoldRequested(offer.getFoodOffered() / 2);
        }
        
        if (player.getCurrentWater() > player.getMaxWater() * 0.8) {
            offer.setWaterOffered((int)(player.getCurrentWater() - player.getMaxWater() * 0.5));
            offer.setGoldRequested(offer.getGoldRequested() + offer.getWaterOffered() / 2);
        }
        
        if (offer.getFoodOffered() > 0 || offer.getWaterOffered() > 0) {
            TradeResponse response = trader.makeTrade(offer);
            if (response.isAccepted()) {
                player.finalizeTrade(response.getFinalOffer());
            }
        }
    }
    
    protected boolean isPathFeasible(Path path) {
        return path.getTotalMovementCost() <= player.getCurrentMovement() &&
               path.getTotalFoodCost() <= player.getCurrentFood() &&
               path.getTotalWaterCost() <= player.getCurrentWater();
    }
    
    protected void followPath(Path path) {
        for (Direction dir : path.getDirections()) {
            if (player.canMove(dir)) {
                movePlayer(dir);
            } else {
                break; // Can't continue path
            }
        }
    }
}
