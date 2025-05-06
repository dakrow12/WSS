package wss.player;

import wss.game.Map;
import wss.game.Path;
import wss.game.Direction;
import wss.game.Position;
import wss.game.Square;
// import wss.items.Item; // Not directly used, Square.collectItem handles it
import wss.trader.Trader;
import wss.trader.TradeOffer;
// import wss.trader.TradeResponse; // Trader methods like evaluatePlayerOffer return boolean directly

import java.util.List;
// import java.util.ArrayList; // Not directly used unless creating new lists here
// import java.util.Random; // Not used in this version

/**
 * NormalBrain attempts to balance resource gathering with eastward progression.
 * It prioritizes critical needs first.
 */
public class NormalBrain extends Brain {
    // Thresholds for decision making
    private static final double CRITICAL_RESOURCE_THRESHOLD = 0.2; // Below 20% is critical
    private static final double GATHER_RESOURCE_THRESHOLD = 0.5;   // Gather if below 50%
    private static final double PRESERVE_STRENGTH_THRESHOLD = 0.3; // Try to keep strength above 30%
    private static final double REST_STRENGTH_THRESHOLD = 0.5;     // Rest if strength is below 50% and no easy action

    public NormalBrain(Player player, Map map) {
        super(player, map);
    }

    @Override
    public void makeMove() {
        if (player == null || map == null || player.getVision() == null) {
            System.err.println("Error in NormalBrain.makeMove: Critical component (player, map, or vision) is null.");
            if (player != null) player.rest(); // Rest if possible
            return;
        }

        // Priority 1: Handle critical resource needs (below 20%)
        if (isCriticalResourceLevel()) { // This method is in Brain.java
            handleCriticalResources();
            return; 
        }

        // Priority 2: Decide whether to gather resources or move east
        if (shouldGatherResources()) {
            gatherNeededResources();
        } else {
            moveTowardEast();
        }
        // Item collection and trader checks are handled within followPath/movePlayer methods called by the above.
    }

    /**
     * Handles actions when resources are critically low.
     * Attempts to find and move towards the most critically needed resource (food or water).
     * If no feasible path is found, the player rests.
     */
    private void handleCriticalResources() {
        Path pathToFood = player.getVision().closestFood(player, map);
        Path pathToWater = player.getVision().closestWater(player, map);

        boolean foodFeasible = (pathToFood != null && isPathFeasible(pathToFood));
        boolean waterFeasible = (pathToWater != null && isPathFeasible(pathToWater));

        // Determine which resource is more critical or if only one is feasible
        boolean takeFoodPath = false;
        boolean takeWaterPath = false;

        // Calculate current percentages to compare needs
        double foodPercentage = (player.getMaxFood() == 0) ? 1.0 : (double)player.getCurrentFood() / player.getMaxFood();
        double waterPercentage = (player.getMaxWater() == 0) ? 1.0 : (double)player.getCurrentWater() / player.getMaxWater();

        if (foodFeasible && waterFeasible) {
            if (foodPercentage <= waterPercentage) { // Food is lower or equally low percentage-wise
                takeFoodPath = true;
            } else {
                takeWaterPath = true;
            }
        } else if (foodFeasible) {
            takeFoodPath = true;
        } else if (waterFeasible) {
            takeWaterPath = true;
        }

        if (takeFoodPath) {
            System.out.println("NormalBrain: Critically low on food, moving to closest source.");
            followPath(pathToFood);
        } else if (takeWaterPath) {
            System.out.println("NormalBrain: Critically low on water, moving to closest source.");
            followPath(pathToWater);
        } else {
            System.out.println("NormalBrain: Resources critical, but no feasible path to food/water. Resting.");
            player.rest(); 
        }
    }

    /**
     * Determines if the player should prioritize gathering resources based on current levels
     * and the feasibility of moving east.
     * @return true if gathering is prioritized, false otherwise.
     */
    private boolean shouldGatherResources() {
        if (player.getCurrentFood() < player.getMaxFood() * GATHER_RESOURCE_THRESHOLD ||
            player.getCurrentWater() < player.getMaxWater() * GATHER_RESOURCE_THRESHOLD ||
            player.getCurrentStrength() < player.getMaxStrength() * PRESERVE_STRENGTH_THRESHOLD) {
            return true; // Prioritize gathering if any resource is low or strength needs preserving
        }
        // If resources are okay, check if moving east is a good option.
        // If not, might as well gather if possible.
        return !hasFeasibleEastwardPath(); 
    }

    /**
     * Attempts to gather the most needed non-critical resource (food or water).
     * If no feasible path to resources, tries the easiest general path or rests.
     */
    private void gatherNeededResources() {
        Path pathToFood = player.getVision().closestFood(player, map);
        Path pathToWater = player.getVision().closestWater(player, map);

        boolean foodFeasible = (pathToFood != null && isPathFeasible(pathToFood));
        boolean waterFeasible = (pathToWater != null && isPathFeasible(pathToWater));
        
        // Calculate need factors (higher means more needed)
        double foodNeed = (player.getMaxFood() == 0) ? 0.0 : 1.0 - ((double)player.getCurrentFood() / player.getMaxFood());
        double waterNeed = (player.getMaxWater() == 0) ? 0.0 : 1.0 - ((double)player.getCurrentWater() / player.getMaxWater());

        boolean takeFoodPath = false;
        boolean takeWaterPath = false;

        if (foodFeasible && waterFeasible) {
            if (foodNeed >= waterNeed) { // Prioritize resource with higher "need" factor
                takeFoodPath = true;
            } else {
                takeWaterPath = true;
            }
        } else if (foodFeasible) {
            takeFoodPath = true;
        } else if (waterFeasible) {
            takeWaterPath = true;
        }

        if (takeFoodPath) {
            System.out.println("NormalBrain: Gathering food.");
            followPath(pathToFood);
        } else if (takeWaterPath) {
            System.out.println("NormalBrain: Gathering water.");
            followPath(pathToWater);
        } else {
            // No feasible path to primary resources.
            // If strength is low, rest. Otherwise, try the general easiest path.
            if (player.getCurrentStrength() < player.getMaxStrength() * REST_STRENGTH_THRESHOLD) {
                System.out.println("NormalBrain: No resource path and strength low. Resting.");
                player.rest();
            } else {
                Path easiestPath = player.getVision().easiestPath(player, map);
                if (easiestPath != null && isPathFeasible(easiestPath)) {
                    System.out.println("NormalBrain: No specific resource path, taking easiest general path.");
                    followPath(easiestPath);
                } else {
                    System.out.println("NormalBrain: No resource or easy path. Resting.");
                    player.rest();
                }
            }
        }
    }
    
    /**
     * Attempts to move eastward, selecting the path with the best score (considering costs and eastwardness).
     * If no eastward move is feasible, tries the overall easiest path or rests.
     */
    private void moveTowardEast() {
        Path bestEastPath = null;
        double highestScore = -Double.MAX_VALUE; // Higher score is better

        Position currentPos = player.getPosition();
        if (currentPos == null) { player.rest(); return; }


        for (Direction dir : Direction.eastwardDirections()) { // Uses helper from Direction enum
            if (!player.canMove(dir, map)) { // Check feasibility before creating path object
                continue;
            }
            // If canMove is true, then the square exists and is affordable for one step
            Position nextPos = currentPos.move(dir); // Already checked in canMove indirectly
            Square nextSquare = map.getSquare(nextPos); // Should be valid if canMove passed
            if (nextSquare == null) continue; // Should not happen

            Path potentialPath = new Path(List.of(dir), nextSquare.getMovementCost(), nextSquare.getWaterCost(), nextSquare.getFoodCost());
            // isPathFeasible for a single step path is redundant if player.canMove(dir,map) was true for that step.
            // However, keeping it for safety or if canMove was simpler.
            // if (isPathFeasible(potentialPath)) { // Redundant if player.canMove(dir,map) is comprehensive
            double score = calculateEastwardScore(potentialPath, dir);
            if (score > highestScore) {
                highestScore = score;
                bestEastPath = potentialPath;
            }
            // }
        }

        if (bestEastPath != null) {
            System.out.println("NormalBrain: Moving east towards best score path.");
            followPath(bestEastPath);
        } else {
            System.out.println("NormalBrain: No feasible eastward path. Checking easiest general path.");
            Path easiestOverallPath = player.getVision().easiestPath(player, map);
            if (easiestOverallPath != null && isPathFeasible(easiestOverallPath)) {
                System.out.println("NormalBrain: Taking easiest general path.");
                followPath(easiestOverallPath);
            } else {
                System.out.println("NormalBrain: No eastward or easy path. Resting.");
                player.rest();
            }
        }
    }

    /**
     * Calculates a score for a potential eastward path. Higher scores are better.
     * Favors lower costs and more direct eastward movement.
     * @param path The single-step path to score.
     * @param dir The direction of the path.
     * @return A score for the path.
     */
    private double calculateEastwardScore(Path path, Direction dir) {
        // Inverse of cost: higher score for lower cost
        double costComponent = 100.0 / (1.0 + path.getTotalMovementCost() + path.getTotalFoodCost() + path.getTotalWaterCost()); 
        
        double directionBonus = 0;
        if (dir == Direction.EAST) directionBonus = 3.0;
        else if (dir == Direction.NORTHEAST || dir == Direction.SOUTHEAST) directionBonus = 1.5;
        
        return costComponent + directionBonus;
    }

    /**
     * Checks if any feasible eastward path exists from the player's current position.
     * @return true if at least one eastward move is possible and affordable, false otherwise.
     */
    private boolean hasFeasibleEastwardPath() {
        for (Direction dir : Direction.eastwardDirections()) {
            if (player.canMove(dir, map)) { // Uses the Player's canMove method
                return true;
            }
        }
        return false;
    }
    
    @Override
    protected boolean shouldTradeWith(Trader trader) {
        if (player == null || trader == null) return false;
        
        boolean needsFood = player.getCurrentFood() < player.getMaxFood() * 0.4;
        boolean needsWater = player.getCurrentWater() < player.getMaxWater() * 0.4;
        boolean hasExcessFood = player.getCurrentFood() > player.getMaxFood() * 0.8;
        boolean hasExcessWater = player.getCurrentWater() > player.getMaxWater() * 0.8;

        // Trade if there's an imbalance (high one, low other)
        if ((hasExcessFood && needsWater) || (hasExcessWater && needsFood)) {
            return true; 
        }
        // Trade if has a good amount of gold and needs resources
        if (player.getCurrentGold() > 10 && (needsFood || needsWater)) {
            return true; 
        }
        return false;
    }
    
    @Override
    protected void initiateTrade(Trader trader) {
        if (player == null || trader == null) return;
        TradeOffer offer = new TradeOffer();
        boolean canMakeMeaningfulOffer = false;

        // Determine needs and excesses
        boolean needsFood = player.getCurrentFood() < player.getMaxFood() * 0.5;
        boolean needsWater = player.getCurrentWater() < player.getMaxWater() * 0.5;
        boolean hasExcessFood = player.getCurrentFood() > player.getMaxFood() * 0.7;
        boolean hasExcessWater = player.getCurrentWater() > player.getMaxWater() * 0.7;

        // Scenario 1: Offer excess for needed resource
        if (hasExcessFood && needsWater) {
            int foodToOffer = Math.min(15, (int)(player.getCurrentFood() - player.getMaxFood() * 0.5));
            if (foodToOffer > 0) {
                offer.setFoodOffered(foodToOffer);
                offer.setWaterRequested(Math.max(1, foodToOffer)); // Request similar amount
                canMakeMeaningfulOffer = true;
            }
        } else if (hasExcessWater && needsFood) {
            int waterToOffer = Math.min(15, (int)(player.getCurrentWater() - player.getMaxWater() * 0.5));
            if (waterToOffer > 0) {
                offer.setWaterOffered(waterToOffer);
                offer.setFoodRequested(Math.max(1, waterToOffer));
                canMakeMeaningfulOffer = true;
            }
        } 
        // Scenario 2: Offer gold for needed resources (if no imbalance trade)
        else if (player.getCurrentGold() > 5 && (needsFood || needsWater)) {
            int goldToOffer = Math.min(player.getCurrentGold() / 2, 8); // Offer up to half gold, max 8
             if (goldToOffer > 0) {
                offer.setGoldOffered(goldToOffer);
                if (needsFood && (!needsWater || foodNeedFactor() >= waterNeedFactor())) {
                    offer.setFoodRequested(goldToOffer * 2); // Simple valuation: 1 gold = 2 food/water
                } else if (needsWater) {
                    offer.setWaterRequested(goldToOffer * 2);
                }
                if(offer.requestFood > 0 || offer.requestWater > 0) canMakeMeaningfulOffer = true;
            }
        }

        if (canMakeMeaningfulOffer && offer.isValid()) {
            System.out.println("NormalBrain proposing offer to " + trader.getPersonality() + ": " + offer);
            if(player.canAffordOffer(offer)) { // Player can afford what they are offering
                boolean accepted = trader.evaluatePlayerOffer(offer);
                if (accepted) {
                    if(player.finalizeTrade(offer)) {
                        System.out.println("NormalBrain: Trade successful (initial offer accepted).");
                        trader.reset(); // Trader resets after successful trade
                    } else {
                        System.out.println("NormalBrain: Trade accepted by trader, but player failed to finalize. Rolling back?");
                        // This state is tricky. Trader accepted, but player couldn't complete.
                        // Ideally, Trader might have a way to cancel/revert. For now, trader might just reset.
                        trader.reset();
                    }
                } else { // Initial offer rejected
                    System.out.println("NormalBrain: Initial offer rejected. Trader state: " + trader.getCurrentState());
                    TradeOffer counterOffer = trader.generateCounterOffer(offer);
                    if (counterOffer != null) {
                        System.out.println("NormalBrain: Trader countered with: " + counterOffer);
                        // NormalBrain evaluates counter. Player needs to afford what *trader requests in counter*.
                        // The counterOffer object is structured from Trader's perspective:
                        // counterOffer.offer... is what Trader offers (Player requests)
                        // counterOffer.request... is what Trader requests (Player offers)
                        TradeOffer playerPerspectiveCounter = new TradeOffer(
                            counterOffer.requestGold, counterOffer.requestFood, counterOffer.requestWater, // What player would give
                            counterOffer.offerGold, counterOffer.offerFood, counterOffer.offerWater       // What player would get
                        );

                        if (player.canAffordOffer(playerPerspectiveCounter) && isCounterOfferAcceptable(offer, counterOffer, trader)) {
                            if (trader.acceptTraderCounterOffer()) { // Player accepts, signals trader
                                // Trader's last offer is what needs to be finalized
                                if(player.finalizeTrade(trader.getLastTraderOffer())) {
                                     System.out.println("NormalBrain: Counter offer accepted and finalized.");
                                } else {
                                     System.out.println("NormalBrain: Accepted counter but FAILED to finalize player resources.");
                                }
                                trader.reset(); // Reset after trade attempt
                            } else {
                                System.out.println("NormalBrain: Tried to accept counter, but trader state was not WAITING_FOR_PLAYER_RESPONSE.");
                                trader.reset();
                            }
                        } else {
                            System.out.println("NormalBrain: Rejected trader's counter offer (unacceptable or unaffordable).");
                            trader.rejectTraderCounterOffer(); // Signal rejection to trader
                            trader.reset(); // End negotiation
                        }
                    } else {
                         System.out.println("NormalBrain: Trader did not make a counter-offer. Negotiation ends.");
                         trader.reset();
                    }
                }
            } else {
                System.out.println("NormalBrain: Cannot afford to make the intended offer: " + offer);
            }
        } else {
            System.out.println("NormalBrain: Decided not to make a trade offer at this time.");
        }
    }

    // Helper: Calculate need factor for food
    private double foodNeedFactor() {
        if (player == null || player.getMaxFood() == 0) return 0;
        return 1.0 - ((double)player.getCurrentFood() / player.getMaxFood());
    }
    // Helper: Calculate need factor for water
    private double waterNeedFactor() {
         if (player == null || player.getMaxWater() == 0) return 0;
        return 1.0 - ((double)player.getCurrentWater() / player.getMaxWater());
    }

    /**
     * Decides if a trader's counter-offer is acceptable.
     * @param myInitialOffer The player's original offer.
     * @param traderCounterOffer The trader's counter-offer (from trader's perspective).
     * @param trader The trader.
     * @return true if the counter-offer is acceptable.
     */
    private boolean isCounterOfferAcceptable(TradeOffer myInitialOffer, TradeOffer traderCounterOffer, Trader trader) {
        // Basic valuation: sum of items (gold * 5, water * 2, food * 1)
        // This is a very simple valuation and should ideally be more nuanced.
        int valuePlayerOfferedInitially = myInitialOffer.offerGold * 5 + myInitialOffer.offerFood * 1 + myInitialOffer.offerWater * 2;
        int valuePlayerRequestedInitially = myInitialOffer.requestGold * 5 + myInitialOffer.requestFood * 1 + myInitialOffer.requestWater * 2;
        double initialPlayerRatio = (valuePlayerRequestedInitially == 0) ? Double.POSITIVE_INFINITY : (double)valuePlayerOfferedInitially / valuePlayerRequestedInitially;

        // Trader's counter: trader offers `traderCounterOffer.offer...`, requests `traderCounterOffer.request...`
        int valueTraderWillGivePlayer = traderCounterOffer.offerGold * 5 + traderCounterOffer.offerFood * 1 + traderCounterOffer.offerWater * 2;
        int valueTraderWantsFromPlayer = traderCounterOffer.requestGold * 5 + traderCounterOffer.requestFood * 1 + traderCounterOffer.requestWater * 2;
        double counterPlayerRatio = (valueTraderWillGivePlayer == 0) ? Double.POSITIVE_INFINITY : (double)valueTraderWantsFromPlayer / valueTraderWillGivePlayer;

        System.out.println("NormalBrain evaluating counter: Initial Ratio (Offer/Request from player POV): " + initialPlayerRatio + 
                           ", Counter Ratio (What Player Gives/Gets): " + counterPlayerRatio);

        // Accept if the counter isn't "too much worse" than the initial ratio.
        // Or if the trader is GENEROUS.
        if (trader.getPersonality() == wss.trader.TraderType.GENEROUS) return true;
        if (trader.getPersonality() == wss.trader.TraderType.FAIR) {
            return counterPlayerRatio <= Math.max(1.2, initialPlayerRatio * 1.25); // Allow up to 25% worse or a general 1.2 ratio
        }
        // Be more stringent with GREEDY or CAUTIOUS traders
        return counterPlayerRatio <= Math.max(1.0, initialPlayerRatio * 1.1); // Allow up to 10% worse or a 1.0 ratio
    }
}
