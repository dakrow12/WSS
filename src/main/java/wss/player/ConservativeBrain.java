package wss.player;

import wss.game.Map;
import wss.game.Path;
import wss.game.Direction;
import wss.game.Position;
import wss.game.Square;
import wss.trader.Trader;
import wss.trader.TradeOffer;

import java.util.List;

/**
 * ConservativeBrain prioritizes resource safety and makes cautious moves.
 * It prefers to keep resources well above critical levels and takes low-risk paths.
 */
public class ConservativeBrain extends Brain {

    // More conservative thresholds for resource management
    private static final double RESOURCE_PRIORITY_THRESHOLD_FOOD = 0.6;
    private static final double RESOURCE_PRIORITY_THRESHOLD_WATER = 0.6;
    private static final double RESOURCE_PRIORITY_THRESHOLD_STRENGTH = 0.4;

    private static final double CRITICAL_FOOD_THRESHOLD = 0.5; // Seek food if below 50%
    private static final double CRITICAL_WATER_THRESHOLD = 0.5; // Seek water if below 50%
    private static final double LOW_STRENGTH_REST_THRESHOLD = 0.6; // Rest if strength below 60%

    public ConservativeBrain(Player player, Map map) {
        super(player, map);
    }
    
    @Override
    public void makeMove() {
        if (player == null || map == null || player.getVision() == null) {
            System.err.println("Error in ConservativeBrain.makeMove: Critical component is null.");
            if (player != null) player.rest();
            return;
        }
        
        System.out.println("ConservativeBrain turn. Resources: F" + player.getCurrentFood() +
                           " W" + player.getCurrentWater() + " S" + player.getCurrentStrength());

        // Collect items on the current square at the start of the turn.
        // movePlayer (called by followPath) will handle collection after moves.
        collectItems(); 
        
        if (!isCriticalResourceLevel() && 
            (player.getCurrentFood() < player.getMaxFood() * 0.8 || player.getCurrentWater() < player.getMaxWater() * 0.8) ) {
            if(checkForNearbyItemsAndMove()) { // This method now returns true if an action was taken
                return;
            }
        }
        
        // Main priority: Check if critical resources need immediate attention
        if (shouldPrioritizeResources()) {
            System.out.println("ConservativeBrain: Prioritizing resources.");
            handleResourcePriority();
            return; 
        }
        
        // If resources are fine, look for the easiest path eastward
        System.out.println("ConservativeBrain: Resources OK. Looking for easiest eastward path.");
        Path easiestEastPath = findEasiestEastPath();
        if (easiestEastPath != null && isPathFeasible(easiestEastPath)) {
            System.out.println("ConservativeBrain: Found feasible eastward path. Following: " + easiestEastPath.getDirections());
            followPath(easiestEastPath);
            // Item collection after each step in followPath is handled by movePlayer.
        } else {
            System.out.println("ConservativeBrain: No feasible eastward path. Resting.");
            player.rest();
        }
    }
    
    private boolean shouldPrioritizeResources() {
        // Uses getCurrentStrength() and getMaxStrength() from Player
        return player.getCurrentFood() < player.getMaxFood() * RESOURCE_PRIORITY_THRESHOLD_FOOD ||
               player.getCurrentWater() < player.getMaxWater() * RESOURCE_PRIORITY_THRESHOLD_WATER ||
               player.getCurrentStrength() < player.getMaxStrength() * RESOURCE_PRIORITY_THRESHOLD_STRENGTH;
    }
    
    private void handleResourcePriority() {
        // Priority 1: Food if below its critical threshold
        if (player.getCurrentFood() < player.getMaxFood() * CRITICAL_FOOD_THRESHOLD) {
            Path closestFood = player.getVision().closestFood(player, map);
            if (closestFood != null && isPathFeasible(closestFood)) {
                System.out.println("ConservativeBrain: Food low, moving to source: " + closestFood.getDirections());
                followPath(closestFood);
                return;
            }
        }
        
        // Priority 2: Water if below its critical threshold
        if (player.getCurrentWater() < player.getMaxWater() * CRITICAL_WATER_THRESHOLD) {
            Path closestWater = player.getVision().closestWater(player, map);
            if (closestWater != null && isPathFeasible(closestWater)) {
                System.out.println("ConservativeBrain: Water low, moving to source: " + closestWater.getDirections());
                followPath(closestWater);
                return;
            }
        }
        
        // Priority 3: Rest if strength is low
        if (player.getCurrentStrength() < player.getMaxStrength() * LOW_STRENGTH_REST_THRESHOLD) {
            System.out.println("ConservativeBrain: Strength low while needing resources. Resting.");
            player.rest();
            return;
        }
        
        // Fallback: If specific resources aren't critically low but still prioritized,
        // or if paths to them weren't feasible, try the overall easiest path.
        System.out.println("ConservativeBrain: Looking for any easiest path due to resource priority.");
        Path easiestOverallPath = player.getVision().easiestPath(player, map);
        if (easiestOverallPath != null && isPathFeasible(easiestOverallPath)) {
            System.out.println("ConservativeBrain: Taking easiest general path for resources: " + easiestOverallPath.getDirections());
            followPath(easiestOverallPath);
        } else {
            System.out.println("ConservativeBrain: No specific resource paths or easy path found. Resting.");
            player.rest();
        }
    }
    
    private boolean checkForNearbyItemsAndMove() {
        // Check for food if not full and a close source exists
        if (player.getCurrentFood() < player.getMaxFood() * 0.9) { // Only if not nearly full
            Path foodPath = player.getVision().closestFood(player, map);
            // Conservative: only take very short paths (e.g., 1-2 steps)
            if (foodPath != null && foodPath.getDirections().size() <= 2 && isPathFeasible(foodPath)) {
                System.out.println("ConservativeBrain: Found very nearby food, collecting.");
                followPath(foodPath);
                return true; // Action taken
            }
        }
        
        // Check for water if not full and a close source exists
        if (player.getCurrentWater() < player.getMaxWater() * 0.9) { // Only if not nearly full
            Path waterPath = player.getVision().closestWater(player, map);
            if (waterPath != null && waterPath.getDirections().size() <= 2 && isPathFeasible(waterPath)) {
                System.out.println("ConservativeBrain: Found very nearby water, collecting.");
                followPath(waterPath);
                return true; // Action taken
            }
        }
        return false; // No action taken for nearby items
    }
    
    private Path findEasiestEastPath() {
        Path bestPath = null;
        double lowestScore = Double.MAX_VALUE;
        
        Position currentPos = player.getPosition();
        if (currentPos == null) return null;

        for (Direction dir : Direction.eastwardDirections()) { // From Direction enum helper
            // Use player.canMove(Direction, Map) which checks resources for the target square
            if (player.canMove(dir, map)) { 
                Square nextSquare = map.getSquare(currentPos.move(dir)); // Should be valid if canMove is true
                if (nextSquare == null) continue; // Should not happen

                double score = calculateEastwardPathScore(dir, nextSquare);
                
                if (score < lowestScore) {
                    lowestScore = score;
                    bestPath = new Path(List.of(dir), 
                                       nextSquare.getMovementCost(),
                                       nextSquare.getFoodCost(), // Corrected order
                                       nextSquare.getWaterCost());
                }
            }
        }
        return bestPath;
    }
    
    private double calculateEastwardPathScore(Direction dir, Square square) {
        // Base cost: sum of resource costs
        double pathCost = square.getMovementCost() + square.getFoodCost() + square.getWaterCost();

        // Weight more direct eastward moves slightly better (lower score is better)
        if (dir == Direction.EAST) {
            pathCost *= 0.9; // Slightly prefer direct East
        } else if (dir == Direction.NORTHEAST || dir == Direction.SOUTHEAST) {
            pathCost *= 0.95; // Slightly prefer other eastward
        }
        
        // Consider current resource levels: penalize paths that consume a large fraction of remaining resources
        if (player.getCurrentFood() > 0) pathCost += (double)square.getFoodCost() / player.getCurrentFood() * 5; // Penalty factor
        if (player.getCurrentWater() > 0) pathCost += (double)square.getWaterCost() / player.getCurrentWater() * 5;
        if (player.getCurrentStrength() > 0) pathCost += (double)square.getMovementCost() / player.getCurrentStrength() * 5;

        return pathCost;
    }
    
    @Override
    protected boolean shouldTradeWith(Trader trader) {
        // Conservative brain trades cautiously, primarily to offload significant excess for a needed resource,
        // or if desperately needing something and having ample gold (less likely to spend gold).
        if (player == null || trader == null) return false;

        boolean needsFoodCritically = player.getCurrentFood() < player.getMaxFood() * 0.3;
        boolean needsWaterCritically = player.getCurrentWater() < player.getMaxWater() * 0.3;
        boolean hasSignificantExcessFood = player.getCurrentFood() > player.getMaxFood() * 0.85;
        boolean hasSignificantExcessWater = player.getCurrentWater() > player.getMaxWater() * 0.85;

        if ((hasSignificantExcessFood && needsWaterCritically) || (hasSignificantExcessWater && needsFoodCritically)) {
            return true; // Good imbalance trade
        }
        // Only consider using gold if resources are very low and trader is not GREEDY
        if (player.getCurrentGold() > 15 && (needsFoodCritically || needsWaterCritically) && 
            trader.getPersonality() != wss.trader.TraderType.GREEDY) {
            return true;
        }
        return false;
    }
    
    @Override
    protected void initiateTrade(Trader trader) {
        if (player == null || trader == null) return;
        TradeOffer offer = new TradeOffer();
        boolean canMakeMeaningfulOffer = false;

        boolean needsFood = player.getCurrentFood() < player.getMaxFood() * 0.4;
        boolean needsWater = player.getCurrentWater() < player.getMaxWater() * 0.4;
        boolean hasExcessFood = player.getCurrentFood() > player.getMaxFood() * 0.8;
        boolean hasExcessWater = player.getCurrentWater() > player.getMaxWater() * 0.8;

        if (hasExcessFood && needsWater) {
            int foodToOffer = Math.min(10, (int)(player.getCurrentFood() - player.getMaxFood() * 0.6));
            if (foodToOffer > 0) {
                offer.setFoodOffered(foodToOffer);
                offer.setWaterRequested(Math.max(1, foodToOffer / 2)); // Expects less water in return
                canMakeMeaningfulOffer = true;
            }
        } else if (hasExcessWater && needsFood) {
            int waterToOffer = Math.min(10, (int)(player.getCurrentWater() - player.getMaxWater() * 0.6));
            if (waterToOffer > 0) {
                offer.setWaterOffered(waterToOffer);
                offer.setFoodRequested(Math.max(1, waterToOffer / 2));
                canMakeMeaningfulOffer = true;
            }
        } else if (player.getCurrentGold() > 15 && (needsFood || needsWater) && trader.getPersonality() != wss.trader.TraderType.GREEDY) {
            int goldToOffer = Math.min(player.getCurrentGold() / 3, 5); // Offers less gold
            if (goldToOffer > 0) {
                offer.setGoldOffered(goldToOffer);
                if (needsFood) offer.setFoodRequested(goldToOffer * 1); // Expects less per gold
                else if (needsWater) offer.setWaterRequested(goldToOffer * 1);
                if(offer.requestFood > 0 || offer.requestWater > 0) canMakeMeaningfulOffer = true;
            }
        }

        if (canMakeMeaningfulOffer && offer.isValid()) {
            System.out.println("ConservativeBrain proposing offer to " + trader.getPersonality() + ": " + offer);
            if(player.canAffordOffer(offer)) {
                boolean accepted = trader.evaluatePlayerOffer(offer);
                if (accepted) {
                    if(player.finalizeTrade(offer)) {
                        System.out.println("ConservativeBrain: Trade successful (initial offer).");
                        trader.reset();
                    } else {
                        System.out.println("ConservativeBrain: Trade accepted, but player failed to finalize.");
                        trader.reset();
                    }
                } else {
                    System.out.println("ConservativeBrain: Initial offer rejected. Trader state: " + trader.getCurrentState());
                    TradeOffer counterOffer = trader.generateCounterOffer(offer);
                    if (counterOffer != null) {
                        System.out.println("ConservativeBrain: Trader countered: " + counterOffer);
                        // Conservative brain is very wary of counters.
                        // Only accept if it's clearly very good (e.g., from a GENEROUS trader and affordable)
                        TradeOffer playerPerspectiveCounter = new TradeOffer(
                            counterOffer.requestGold, counterOffer.requestFood, counterOffer.requestWater,
                            counterOffer.offerGold, counterOffer.offerFood, counterOffer.offerWater
                        );
                        if (player.canAffordOffer(playerPerspectiveCounter) && 
                            trader.getPersonality() == wss.trader.TraderType.GENEROUS) { // Only accept GENEROUS counters easily
                            if (trader.acceptTraderCounterOffer()) {
                                if(player.finalizeTrade(trader.getLastTraderOffer())) {
                                    System.out.println("ConservativeBrain: Accepted GENEROUS counter and finalized.");
                                } else {
                                    System.out.println("ConservativeBrain: Failed to finalize GENEROUS counter.");
                                }
                                trader.reset();
                            }
                        } else {
                            System.out.println("ConservativeBrain: Rejecting counter offer.");
                            trader.rejectTraderCounterOffer();
                            trader.reset();
                        }
                    } else {
                        System.out.println("ConservativeBrain: Trader did not counter. Negotiation ends.");
                        trader.reset();
                    }
                }
            } else {
                 System.out.println("ConservativeBrain: Cannot afford to make intended offer: " + offer);
            }
        } else {
            System.out.println("ConservativeBrain: Decided not to make a trade offer.");
        }
    }
}