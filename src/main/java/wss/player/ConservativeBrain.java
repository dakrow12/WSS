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
        System.out.println("ConservativeBrain is deciding what to do...");
        
        // Always check for and collect items first, regardless of other priorities
        collectItems();
        
        // Check if there are any items in the neighboring squares
        checkForNearbyItems();
        
        // Check if we need to prioritize resources
        if (shouldPrioritizeResources()) {
            System.out.println("Resources are low, prioritizing resource gathering");
            handleResourcePriority();
            return;
        }
        
        // Otherwise look for easiest path east
        Path easiestEast = findEasiestEastPath();
        if (easiestEast != null && isPathFeasible(easiestEast)) {
            System.out.println("Moving along easiest eastward path");
            followPath(easiestEast);
            
            // Make sure to collect items after moving
            Square currentSquare = map.getSquare(player.getX(), player.getY());
            currentSquare.collectItem(player);
        } else {
            // If no easy path east, rest to recover
            System.out.println("No feasible eastward path, resting to recover");
            player.rest();
        }
    }
    
    private boolean shouldPrioritizeResources() {
        // More conservative thresholds (increased from previous values)
        return player.getCurrentFood() < player.getMaxFood() * 0.6 ||  // was 0.5
               player.getCurrentWater() < player.getMaxWater() * 0.6 || // was 0.5
               player.getCurrentMovement() < player.getMaxMovement() * 0.4; // was 0.3
    }
    
    private void handleResourcePriority() {
        // Priority 1: Food if low
        if (player.getCurrentFood() < player.getMaxFood() * 0.5) { // was 0.3
            System.out.println("Food is low, looking for food sources");
            Path closestFood = player.getVision().closestFood(player, map);
            if (closestFood != null && isPathFeasible(closestFood)) {
                System.out.println("Found food source, moving to collect");
                followPath(closestFood);
                return;
            } else {
                System.out.println("No accessible food source found");
            }
        }
        
        // Priority 2: Water if low
        if (player.getCurrentWater() < player.getMaxWater() * 0.5) { // was 0.3
            System.out.println("Water is low, looking for water sources");
            Path closestWater = player.getVision().closestWater(player, map);
            if (closestWater != null && isPathFeasible(closestWater)) {
                System.out.println("Found water source, moving to collect");
                followPath(closestWater);
                return;
            } else {
                System.out.println("No accessible water source found");
            }
        }
        
        // Priority 3: Rest if movement is low
        if (player.getCurrentMovement() < player.getMaxMovement() * 0.6) { // was 0.5
            System.out.println("Movement energy is low, resting to recover");
            player.rest();
            return;
        }
        
        // If resources are low but nothing nearby, try to move to easier terrain
        System.out.println("Looking for path with least resource cost");
        Path easiestPath = player.getVision().easiestPath(player, map);
        if (easiestPath != null && isPathFeasible(easiestPath)) {
            System.out.println("Found low-cost path, following it");
            followPath(easiestPath);
        } else {
            System.out.println("No accessible paths found, resting");
            player.rest();
        }
    }
    
    // New method to check for items in neighboring squares
    private void checkForNearbyItems() {
        if (player.getCurrentFood() < player.getMaxFood() * 0.8) {
            Path foodPath = player.getVision().closestFood(player, map);
            if (foodPath != null && isPathFeasible(foodPath)) {
                System.out.println("Found nearby food, moving to collect");
                followPath(foodPath);
                return;
            }
        }
        
        if (player.getCurrentWater() < player.getMaxWater() * 0.8) {
            Path waterPath = player.getVision().closestWater(player, map);
            if (waterPath != null && isPathFeasible(waterPath)) {
                System.out.println("Found nearby water, moving to collect");
                followPath(waterPath);
                return;
            }
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
