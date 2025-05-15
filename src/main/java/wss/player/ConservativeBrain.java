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
import wss.WSSGameEngine;
import wss.game.Position;




public class ConservativeBrain extends Brain {
    public ConservativeBrain(Player player, Map map) {
        super(player, map);
    }
    
    public ConservativeBrain(Player player, Map map, WSSGameEngine gameEngine) {
        super(player, map, gameEngine);
    }
    
    @Override
    public void makeMove() {
        System.out.println("ConservativeBrain is deciding what to do...");
        
        // Always check for and collect items first, regardless of other priorities
        collectItems();
        
        // Check for nearby traders - conservative brains value trading
        if (checkForNearbyTrader()) {
            return; // Trader interaction handled
        }
        
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
if (player.getCurrentWater() < player.getMaxWater() * 0.5) {
    System.out.println("Water is low, looking for water sources");
    Path closestWater = player.getVision().closestWater(player, map);
    if (closestWater != null && isPathFeasible(closestWater)) {
        System.out.println("Found water source, moving to collect");
        followPath(closestWater);
        return;
    } else {
        System.out.println("No accessible water source found");

        //  Try trading for water if there's a trader on the square
        Square currentSquare = map.getSquare(player.getX(), player.getY());
        if (currentSquare.hasTrader()) {
            Trader trader = currentSquare.getTrader();
            System.out.println("Attempting to trade for water with " + trader.getPersonality());

            TradeOffer offer = new TradeOffer(
                1, // offerGold
                0, // offerFood
                0, // offerWater
                0, // requestGold
                0, // requestFood
                3  // requestWater
            );

            TradeResponse response = trader.makeTrade(offer);
            if (response.isAccepted()) {
                player.finalizeTrade(response.getFinalOffer());
                System.out.println("Trade successful: Gained water from trader.");
                return;
            } else {
                System.out.println("Trade rejected.");
            }
        }
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
        // Always willing to trade - conservative players value having resources
        System.out.println("ConservativeBrain: Should trade with " + trader.getPersonality() + " trader? YES");
        return true;
    }
    
    @Override
    protected void initiateTrade(Trader trader) {
        System.out.println("Creating a trade offer for " + trader.getPersonality() + " trader");
        TradeOffer offer = new TradeOffer();
        boolean offerCreated = false;
        
        // Try to balance resources
        if (player.getCurrentFood() > player.getMaxFood() * 0.7 && 
            player.getCurrentWater() < player.getMaxWater() * 0.5) {
            // Trade excess food for water
            int foodToOffer = (int)(player.getCurrentFood() - player.getMaxFood() * 0.5);
            if (foodToOffer > 0) {
                offer.setFoodOffered(foodToOffer);
                offer.setWaterRequested(foodToOffer);
                System.out.println("Offering " + foodToOffer + " food for " + foodToOffer + " water");
                offerCreated = true;
            }
        } 
        else if (player.getCurrentWater() > player.getMaxWater() * 0.7 &&
                player.getCurrentFood() < player.getMaxFood() * 0.5) {
            // Trade excess water for food
            int waterToOffer = (int)(player.getCurrentWater() - player.getMaxWater() * 0.5);
            if (waterToOffer > 0) {
                offer.setWaterOffered(waterToOffer);
                offer.setFoodRequested(waterToOffer);
                System.out.println("Offering " + waterToOffer + " water for " + waterToOffer + " food");
                offerCreated = true;
            }
        }
        else if (player.getCurrentGold() > 5 && 
                (player.getCurrentFood() < player.getMaxFood() * 0.5 ||
                player.getCurrentWater() < player.getMaxWater() * 0.5)) {
            // Trade gold for needed resources
            int goldToOffer = player.getCurrentGold() / 2;
            if (goldToOffer > 0) {
                offer.setGoldOffered(goldToOffer);
                if (player.getCurrentFood() < player.getMaxFood() * 0.5) {
                    offer.setFoodRequested(goldToOffer);
                    System.out.println("Offering " + goldToOffer + " gold for " + goldToOffer + " food");
                } else {
                    offer.setWaterRequested(goldToOffer);
                    System.out.println("Offering " + goldToOffer + " gold for " + goldToOffer + " water");
                }
                offerCreated = true;
            }
        }
        // Default case: just trade 1 gold for some water
        else if (player.getCurrentGold() >= 1) {
            offer.setGoldOffered(1);
            offer.setWaterRequested(1);
            System.out.println("Default offer: 1 gold for 1 water");
            offerCreated = true;
        }
        
        // Fallback: if no trade has been created yet, create a basic trade based on what resources we have
        if (!offerCreated) {
            // Fallback 1: If we have some food, offer a small amount for water
            if (player.getCurrentFood() > 5) {
                offer.setFoodOffered(2);
                offer.setWaterRequested(2);
                System.out.println("Fallback offer: 2 food for 2 water");
                offerCreated = true;
            } 
            // Fallback 2: If we have some water, offer a small amount for food
            else if (player.getCurrentWater() > 5) {
                offer.setWaterOffered(2);
                offer.setFoodRequested(2);
                System.out.println("Fallback offer: 2 water for 2 food");
                offerCreated = true;
            }
            // Fallback 3: Last resort - offer 1 of whatever we have more of
            else {
                if (player.getCurrentFood() > player.getCurrentWater()) {
                    offer.setFoodOffered(1);
                    offer.setWaterRequested(1);
                    System.out.println("Emergency offer: 1 food for 1 water");
                } else {
                    offer.setWaterOffered(1);
                    offer.setFoodRequested(1);
                    System.out.println("Emergency offer: 1 water for 1 food");
                }
                offerCreated = true;
            }
        }
        
        if (offer.isValid()) {
            System.out.println("Making offer: " + offer);
            TradeResponse response = trader.makeTrade(offer);
            if (response.isAccepted()) {
                System.out.println("Trade accepted! Finalizing trade.");
                player.finalizeTrade(response.getFinalOffer());
                System.out.println("Resources after trade: Food=" + player.getCurrentFood() + 
                                  ", Water=" + player.getCurrentWater() + 
                                  ", Gold=" + player.getCurrentGold());
            } else {
                System.out.println("Trade rejected. Checking for counter offer...");
                TradeOffer counter = trader.generateCounterOffer(offer);
                if (counter != null && counter.isValid()) {
                    System.out.println("Received counter offer: " + counter);
                    // Accept any counter offer if we can afford it
                    if (player.canAffordOffer(counter) && trader.acceptTraderCounterOffer()) {
                        System.out.println("Accepting counter offer");
                        player.finalizeTrade(counter);
                        System.out.println("Resources after trade: Food=" + player.getCurrentFood() + 
                                          ", Water=" + player.getCurrentWater() + 
                                          ", Gold=" + player.getCurrentGold());
                    } else {
                        System.out.println("Cannot afford counter offer or counter rejected");
                    }
                } else {
                    System.out.println("No counter offer received.");
                }
            }
        } else {
            // This should never happen now due to our fallback logic
            System.out.println("ERROR: Could not create a valid trade offer despite fallbacks.");
        }
    }
    
    /**
     * Look for traders in nearby squares and move to them if found
     * @return true if trader found and moved to
     */
    private boolean checkForNearbyTrader() {
        // Conservative players always want to check for trading opportunities
        for (Direction dir : Direction.values()) {
            Position newPos = player.getPosition().move(dir);
            if (map.inBounds(newPos.getX(), newPos.getY())) {
                Square square = map.getSquare(newPos);
                if (square.hasTrader() && player.canMove(dir)) {
                    System.out.println("ConservativeBrain: Found trader nearby, moving to trade");
                    movePlayer(dir);
                    return true;
                }
            }
        }
        return false;
    }
}
