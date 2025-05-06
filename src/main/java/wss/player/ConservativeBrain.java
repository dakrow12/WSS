package wss.player;

import wss.player.Player;
import wss.game.Map;
import wss.game.Path;
import wss.game.Direction;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import wss.trader.TradeResponse;
import wss.game.Square;
import wss.trader.Trader;
import wss.trader.TradeOffer;
import wss.trader.TradeResponse;




public class ConservativeBrain extends Brain {
    public ConservativeBrain(Player player, Map map) {
        super(player, map);
    }
    
    @Override
    public void makeMove() {
        // Check if we need to prioritize resources
        if (shouldPrioritizeResources()) {
            handleResourcePriority();
            return;
        }
        
        // Otherwise look for easiest path east
        Path easiestEast = findEasiestEastPath();
        if (easiestEast != null && isPathFeasible(easiestEast)) {
            
            followPath(easiestEast);
            
            Square currentSquare = map.getSquare(player.getX(), player.getY());
            currentSquare.collectItem(player);

        } else {
            // If no easy path east, rest to recover
            player.rest();

        }
    }
    
    private boolean shouldPrioritizeResources() {
        return player.getCurrentFood() < player.getMaxFood() * 0.5 ||
               player.getCurrentWater() < player.getMaxWater() * 0.5 ||
               player.getCurrentMovement() < player.getMaxMovement() * 0.3;
    }
    
    private void handleResourcePriority() {
        // Priority 1: Food if critically low
        if (player.getCurrentFood() < player.getMaxFood() * 0.3) {
            Path closestFood = player.getVision().closestFood(player, map);
            if (closestFood != null && isPathFeasible(closestFood)) {
                followPath(closestFood);
                return;
            }
        }
        
        // Priority 2: Water if critically low
        if (player.getCurrentWater() < player.getMaxWater() * 0.3) {
            Path closestWater = player.getVision().closestWater(player, map);
            if (closestWater != null && isPathFeasible(closestWater)) {
                followPath(closestWater);
                return;
            }
        }
        
        // Priority 3: Rest if movement is low
        if (player.getCurrentMovement() < player.getMaxMovement() * 0.5) {
            player.rest();
            return;
        }
        
        // If resources are low but nothing nearby, try to move to easier terrain
        Path easiestPath = player.getVision().easiestPath(player, map);
        if (easiestPath != null && isPathFeasible(easiestPath)) {
            followPath(easiestPath);
        } else {
            player.rest();
        }
    }
    
    private Path findEasiestEastPath() {
        // Look for paths that move generally eastward with lowest costs
        Path bestPath = null;
        double bestScore = Double.MAX_VALUE;
        
        for (Direction dir : Direction.eastwardDirections()) {
            if (player.canMove(dir)) {
                Square nextSquare = map.getSquare(player.getPosition().move(dir));
                double score = calculatePathScore(dir, nextSquare);
                
                if (score < bestScore) {
                    bestScore = score;
                    bestPath = new Path(List.of(dir), 
                                       nextSquare.getMovementCost(),
                                       nextSquare.getFoodCost(),
                                       nextSquare.getWaterCost());
                }
            }
        }
        
        return bestPath;
    }
    
    private double calculatePathScore(Direction dir, Square square) {
        // Prefer eastward movement
        double eastWeight = dir == Direction.EAST ? 0.5 : 1.0;
        
        // Consider resource costs relative to current levels
        double foodFactor = square.getFoodCost() / player.getCurrentFood();
        double waterFactor = square.getWaterCost() / player.getCurrentWater();
        double movementFactor = square.getMovementCost() / player.getCurrentMovement();
        
        return eastWeight * (foodFactor + waterFactor + movementFactor);
    }
    
    @Override
    protected boolean shouldTradeWith(Trader trader) {
        // Always willing to trade to balance resources
        return true;
    }
    
    @Override
    protected void initiateTrade(Trader trader) {
        TradeOffer offer = new TradeOffer();
        
        // Try to balance resources
        if (player.getCurrentFood() > player.getMaxFood() * 0.7 && 
            player.getCurrentWater() < player.getMaxWater() * 0.5) {
            // Trade excess food for water
            offer.setFoodOffered((int)(player.getCurrentFood() - player.getMaxFood() * 0.5));
            offer.setWaterRequested(offer.getFoodOffered() * 2);
        } 
        else if (player.getCurrentWater() > player.getMaxWater() * 0.7 &&
                player.getCurrentFood() < player.getMaxFood() * 0.5) {
            // Trade excess water for food
            offer.setWaterOffered((int)(player.getCurrentWater() - player.getMaxWater() * 0.5));
            offer.setFoodRequested(offer.getWaterOffered() * 2);
        }
        else if (player.getCurrentGold() > 5 && 
                (player.getCurrentFood() < player.getMaxFood() * 0.5 ||
                 player.getCurrentWater() < player.getMaxWater() * 0.5)) {
            // Trade gold for needed resources
            offer.setGoldOffered(player.getCurrentGold() / 2);
            if (player.getCurrentFood() < player.getMaxFood() * 0.5) {
                offer.setFoodRequested(offer.getGoldOffered() * 3);
            } else {
                offer.setWaterRequested(offer.getGoldOffered() * 3);
            }
        }
        
        if (offer.hasOffer()) {
            TradeResponse response = trader.makeTrade(offer);
            if (response.isAccepted()) {
                player.finalizeTrade(response.getFinalOffer());
            }
        }
    }
}
