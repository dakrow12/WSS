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


public class NormalBrain extends Brain {
    private static final double RESOURCE_THRESHOLD = 0.4;
    private static final double MOVEMENT_THRESHOLD = 0.4;
    
    public NormalBrain(Player player, Map map) {
        super(player, map);
    }
    
    @Override
    public void makeMove() {
        // Check critical needs first
        if (isCriticalResourceLevel()) {
            handleCriticalResources();
            return;
        }
        
        // Balance between moving east and gathering resources
        if (shouldGatherResources()) {
            handleResourceGathering();
        } else {
            moveTowardEast();


        }
    }
    


private void handleCriticalResources() {
    Path foodPath = player.getVision().closestFood(player, map);
    if (foodPath != null && isPathFeasible(foodPath)) {
        followPath(foodPath);
        return;
    }

    Path waterPath = player.getVision().closestWater(player, map);
    if (waterPath != null && isPathFeasible(waterPath)) {
        followPath(waterPath);
        return;
    }

    player.rest();
}

    private boolean shouldGatherResources() {
        // Gather resources if below threshold or if eastward path is too costly
        return player.getCurrentFood() < player.getMaxFood() * RESOURCE_THRESHOLD ||
               player.getCurrentWater() < player.getMaxWater() * RESOURCE_THRESHOLD ||
               player.getCurrentMovement() < player.getMaxMovement() * MOVEMENT_THRESHOLD ||
               !hasFeasibleEastPath();
    }
    
    private void handleResourceGathering() {
        // Prioritize based on most critical need
        if (player.getCurrentFood() < player.getCurrentWater()) {
            Path foodPath = player.getVision().closestFood(player, map);
            if (foodPath != null && isPathFeasible(foodPath)) {
                followPath(foodPath);
                return;
            }
        }
        
        Path waterPath = player.getVision().closestWater(player, map);
        if (waterPath != null && isPathFeasible(waterPath)) {
            followPath(waterPath);
            return;
        }
        
        // If no resources nearby, try to rest or find easier path
        if (player.getCurrentMovement() < player.getMaxMovement() * 0.5) {
            player.rest();
        } else {
            Path easiestPath = player.getVision().easiestPath(player, map);
            if (easiestPath != null && isPathFeasible(easiestPath)) {
                followPath(easiestPath);
            } else {
                player.rest();
            }
        }
    }
    
    private void moveTowardEast() {
        // Try to find a path that balances eastward progress with resource costs
        Path bestPath = null;
        double bestScore = Double.MIN_VALUE;
        
        for (Direction dir : Direction.eastwardDirections()) {
            if (player.canMove(dir)) {
                Square nextSquare = map.getSquare(player.getPosition().move(dir));
                double score = calculateDirectionScore(dir, nextSquare);
                
                if (score > bestScore) {
                    bestScore = score;
                    bestPath = new Path(List.of(dir), 
                                     nextSquare.getMovementCost(),
                                     nextSquare.getFoodCost(),
                                     nextSquare.getWaterCost());
                }
            }
        }
        
        if (bestPath != null) {
            followPath(bestPath);
        } else {
            player.rest();
        }
    }
    
    private double calculateDirectionScore(Direction dir, Square square) {
        // Score based on eastward progress, resource costs, and current needs
        double eastScore = dir == Direction.EAST ? 1.0 : 
                          dir.isEastward() ? 0.7 : 0.3;
        
        double foodScore = 1.0 - (square.getFoodCost() / player.getCurrentFood());
        double waterScore = 1.0 - (square.getWaterCost() / player.getCurrentWater());
        double movementScore = 1.0 - (square.getMovementCost() / player.getCurrentMovement());
        
        // Weight scores based on current needs
        double foodWeight = player.getCurrentFood() < player.getMaxFood() * 0.6 ? 1.5 : 1.0;
        double waterWeight = player.getCurrentWater() < player.getMaxWater() * 0.6 ? 1.5 : 1.0;
        double movementWeight = player.getCurrentMovement() < player.getMaxMovement() * 0.6 ? 1.5 : 1.0;
        
        return eastScore * 2.0 + 
               foodScore * foodWeight + 
               waterScore * waterWeight + 
               movementScore * movementWeight;
    }
    
    private boolean hasFeasibleEastPath() {
        for (Direction dir : Direction.eastwardDirections()) {
            if (player.canMove(dir)) {
                Square square = map.getSquare(player.getPosition().move(dir));
                if (square.getFoodCost() <= player.getCurrentFood() &&
                    square.getWaterCost() <= player.getCurrentWater() &&
                    square.getMovementCost() <= player.getCurrentMovement()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    protected boolean shouldTradeWith(Trader trader) {
        // Trade if we have imbalance in resources
        return (player.getCurrentFood() > player.getMaxFood() * 0.7 && 
                player.getCurrentWater() < player.getMaxWater() * 0.5) ||
               (player.getCurrentWater() > player.getMaxWater() * 0.7 && 
                player.getCurrentFood() < player.getMaxFood() * 0.5) ||
               (player.getCurrentGold() > 3 && 
                (player.getCurrentFood() < player.getMaxFood() * 0.4 || 
                 player.getCurrentWater() < player.getMaxWater() * 0.4));
    }
    
    @Override
    protected void initiateTrade(Trader trader) {
        TradeOffer offer = new TradeOffer();
        
        // Balance resources
        if (player.getCurrentFood() > player.getMaxFood() * 0.7 && 
            player.getCurrentWater() < player.getMaxWater() * 0.5) {
            offer.setFoodOffered((int)(player.getCurrentFood() - player.getMaxFood() * 0.5));
            offer.setWaterRequested(offer.getFoodOffered());
        } 
        else if (player.getCurrentWater() > player.getMaxWater() * 0.7 &&
                player.getCurrentFood() < player.getMaxFood() * 0.5) {
            offer.setWaterOffered((int)(player.getCurrentWater() - player.getMaxWater() * 0.5));
            offer.setFoodRequested(offer.getWaterOffered());
        }
        else if (player.getCurrentGold() > 3) {
            // Use gold to supplement needed resources
            offer.setGoldOffered(Math.min(3, player.getCurrentGold()));
            if (player.getCurrentFood() < player.getCurrentWater()) {
                offer.setFoodRequested(offer.getGoldOffered() * 2);
            } else {
                offer.setWaterRequested(offer.getGoldOffered() * 2);
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
