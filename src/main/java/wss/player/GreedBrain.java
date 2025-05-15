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
import wss.WSSGameEngine;


public class GreedBrain extends Brain {
    public GreedBrain(Player player, Map map) {
        super(player, map);
    }
    
    public GreedBrain(Player player, Map map, WSSGameEngine gameEngine) {
        super(player, map, gameEngine);
    }
    
    @Override
    public void makeMove() {
        if (isCriticalResourceLevel()) {
            handleCriticalResources();
            return;
        }
        
        // New: Check for low strength and rest if needed, to prevent overexertion
        if (player.getCurrentMovement() < player.getMaxMovement() * 0.35) { // Rest if below 35% strength
            System.out.println("GreedBrain: Strength is low (" + player.getCurrentMovement() + "), resting.");
            player.rest();
            return;
        }
        
        // Check for nearby traders first when we have excess resources
        if (hasExcessResources() && checkForNearbyTrader()) {
            return; // Trader interaction handled in checkForNearbyTrader
        }
        
        Path closestGold = player.getVision().closestGold(player, map);
        if (closestGold != null && isPathFeasible(closestGold)) {
            followPath(closestGold);

            Square currentSquare = map.getSquare(player.getX(), player.getY());
            currentSquare.collectItem(player);
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
        return (player.getCurrentGold() > 0 || 
                player.getCurrentFood() > player.getMaxFood() * 0.6 ||
                player.getCurrentWater() > player.getMaxWater() * 0.6);
    }
    
    @Override
    protected void initiateTrade(Trader trader) {
        System.out.println("GreedBrain: Creating a trade offer");
        TradeOffer offer = new TradeOffer();
        boolean offerMade = false;
        
        // Greedy brain always tries to get the best deal possible - prioritize gold acquisition
        if (player.getCurrentFood() > player.getMaxFood() * 0.8) {
            // Trade excess food for gold
            int foodToOffer = (int)(player.getCurrentFood() - player.getMaxFood() * 0.6);
            if (foodToOffer > 0) {
                offer.setFoodOffered(foodToOffer);
                offer.setGoldRequested(Math.max(1, foodToOffer / 3)); // Ask for gold in return
                System.out.println("Offering " + foodToOffer + " food for " + 
                                  Math.max(1, foodToOffer / 3) + " gold");
                offerMade = true;
            }
        }
        
        if (player.getCurrentWater() > player.getMaxWater() * 0.8) {
            // Trade excess water for gold
            int waterToOffer = (int)(player.getCurrentWater() - player.getMaxWater() * 0.6);
            if (waterToOffer > 0) {
                offer.setWaterOffered(waterToOffer);
                // If we already offered food, don't override the gold request, just add to it
                int currentGoldRequest = offer.getGoldRequested();
                offer.setGoldRequested(currentGoldRequest + Math.max(1, waterToOffer / 3));
                System.out.println("Offering " + waterToOffer + " water for " + 
                                  Math.max(1, waterToOffer / 3) + " gold");
                offerMade = true;
            }
        }
        
        // If critically low on resources, trade gold for what we need
        if (!offerMade && player.getCurrentGold() > 0) {
            if (player.getCurrentFood() < player.getMaxFood() * 0.3) {
                // Trade gold for food if desperate
                int goldToOffer = Math.min(player.getCurrentGold(), 2);
                offer.setGoldOffered(goldToOffer);
                offer.setFoodRequested(goldToOffer * 2); // Expect more food per gold because greedy
                System.out.println("Emergency offer: " + goldToOffer + " gold for " + 
                                  (goldToOffer * 2) + " food");
                offerMade = true;
            }
            else if (player.getCurrentWater() < player.getMaxWater() * 0.3) {
                // Trade gold for water if desperate
                int goldToOffer = Math.min(player.getCurrentGold(), 2);
                offer.setGoldOffered(goldToOffer);
                offer.setWaterRequested(goldToOffer * 2); // Expect more water per gold because greedy
                System.out.println("Emergency offer: " + goldToOffer + " gold for " + 
                                  (goldToOffer * 2) + " water");
                offerMade = true;
            }
        }
        
        // Fallback options if no offer made yet
        if (!offerMade) {
            // Fallback 1: If we have some food, try to get gold
            if (player.getCurrentFood() > 5) {
                offer.setFoodOffered(2);
                offer.setGoldRequested(1);
                System.out.println("Fallback offer: 2 food for 1 gold");
                offerMade = true;
            }
            // Fallback 2: If we have some water, try to get gold
            else if (player.getCurrentWater() > 5) {
                offer.setWaterOffered(2);
                offer.setGoldRequested(1);
                System.out.println("Fallback offer: 2 water for 1 gold");
                offerMade = true;
            }
            // Fallback 3: If we have gold, try to get something we need more of
            else if (player.getCurrentGold() > 0) {
                offer.setGoldOffered(1);
                // Whatever we have less of, get more of it
                if (player.getCurrentFood() < player.getCurrentWater()) {
                    offer.setFoodRequested(2);
                    System.out.println("Fallback offer: 1 gold for 2 food");
                } else {
                    offer.setWaterRequested(2);
                    System.out.println("Fallback offer: 1 gold for 2 water");
                }
                offerMade = true;
            }
            // Last resort - offer whatever we have more of
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
                offerMade = true;
            }
        }
        
        if (offer.isValid()) {
            System.out.println("Making offer: " + offer);
            TradeResponse response = trader.makeTrade(offer);
            if (response.isAccepted()) {
                System.out.println("Trade accepted!");
                player.finalizeTrade(response.getFinalOffer());
                System.out.println("Resources after trade: Food=" + player.getCurrentFood() + 
                                 ", Water=" + player.getCurrentWater() + 
                                 ", Gold=" + player.getCurrentGold());
            } else {
                System.out.println("Trade rejected, checking for counter offer...");
                TradeOffer counter = trader.generateCounterOffer(offer);
                
                if (counter != null && counter.isValid() && player.canAffordOffer(counter)) {
                    System.out.println("Received counter offer: " + counter);
                    // Accept counter only if it still involves getting gold or critical resources
                    boolean shouldAccept = counter.requestGold > 0 || 
                                          (player.getCurrentFood() < player.getMaxFood() * 0.3 && counter.requestFood > 0) ||
                                          (player.getCurrentWater() < player.getMaxWater() * 0.3 && counter.requestWater > 0);
                    
                    if (shouldAccept && trader.acceptTraderCounterOffer()) {
                        System.out.println("Counter offer acceptable, accepting");
                        player.finalizeTrade(counter);
                        System.out.println("Resources after trade: Food=" + player.getCurrentFood() + 
                                         ", Water=" + player.getCurrentWater() + 
                                         ", Gold=" + player.getCurrentGold());
                    } else {
                        System.out.println("Counter offer doesn't meet greed criteria or not accepted");
                        trader.rejectTraderCounterOffer();
                    }
                } else {
                    System.out.println("No valid counter offer received or can't afford it");
                }
            }
        } else {
            // This should never happen now due to our fallback logic
            System.out.println("ERROR: Could not create a valid trade offer despite fallbacks");
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
    
    /**
     * Check if player has excess resources to trade
     */
    private boolean hasExcessResources() {
        return player.getCurrentFood() > player.getMaxFood() * 0.8 ||
               player.getCurrentWater() > player.getMaxWater() * 0.8 ||
               player.getCurrentGold() > 5;
    }
    
    /**
     * Look for traders in nearby squares and move to them if found
     * @return true if trader found and moved to
     */
    private boolean checkForNearbyTrader() {
        // Check all adjacent squares for traders
        for (Direction dir : Direction.values()) {
            Position newPos = player.getPosition().move(dir);
            if (map.inBounds(newPos.getX(), newPos.getY())) {
                Square square = map.getSquare(newPos);
                if (square.hasTrader() && player.canMove(dir)) {
                    System.out.println("GreedBrain: Found trader nearby, moving to trade");
                    movePlayer(dir);
                    return true;
                }
            }
        }
        return false;
    }
}
