package wss.player;

import wss.game.Map;
import wss.game.Path;
import wss.game.Direction;
import wss.game.Position;
import wss.game.Square;
// import wss.items.Item; // Not directly used
import wss.trader.Trader;
import wss.trader.TradeOffer;
// import wss.trader.TradeResponse; // Trader methods return boolean or TradeOffer

import java.util.List;
import java.util.ArrayList;
import java.util.Collections; // For shuffling
import java.util.Random;

/**
 * GreedBrain prioritizes acquiring gold. If resources are critically low,
 * it will seek them, but otherwise, it focuses on gold and eastward movement.
 */
public class GreedBrain extends Brain {
    private static final Random random = new Random(); // For random movement choices

    public GreedBrain(Player player, Map map) {
        super(player, map);
    }
    
    @Override
    public void makeMove() {
        if (player == null || map == null || player.getVision() == null) {
            System.err.println("Error in GreedBrain.makeMove: Critical component (player, map, or vision) is null.");
            if (player != null) player.rest();
            return;
        }

        // Priority 1: Handle critical resource needs
        if (isCriticalResourceLevel()) { // Method from Brain superclass
            handleCriticalResources();
            return; 
        }
        
        // Priority 2: Look for gold
        Path closestGoldPath = player.getVision().closestGold(player, map);
        if (closestGoldPath != null && isPathFeasible(closestGoldPath)) {
            System.out.println("GreedBrain: Moving towards gold.");
            followPath(closestGoldPath);
            return; 
        }
        
        // Priority 3: If no gold path or not feasible, move east or explore randomly
        System.out.println("GreedBrain: No gold path, attempting to move east or explore.");
        moveEastOrRandom();
    }
    
    /**
     * Handles actions when resources are critically low.
     * Prioritizes the most critical of food or water. If no path, rests.
     */
    private void handleCriticalResources() {
        Path pathToFood = player.getVision().closestFood(player, map);
        Path pathToWater = player.getVision().closestWater(player, map);

        boolean foodFeasible = (pathToFood != null && isPathFeasible(pathToFood));
        boolean waterFeasible = (pathToWater != null && isPathFeasible(pathToWater));

        double foodPercentage = (player.getMaxFood() == 0) ? 1.0 : (double)player.getCurrentFood() / player.getMaxFood();
        double waterPercentage = (player.getMaxWater() == 0) ? 1.0 : (double)player.getCurrentWater() / player.getMaxWater();

        boolean takeFoodPath = false;
        boolean takeWaterPath = false;

        if (foodFeasible && waterFeasible) {
            if (foodPercentage <= waterPercentage) {
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
            System.out.println("GreedBrain: Critically low on food, moving to source.");
            followPath(pathToFood);
        } else if (takeWaterPath) {
            System.out.println("GreedBrain: Critically low on water, moving to source.");
            followPath(pathToWater);
        } else {
            System.out.println("GreedBrain: Resources critical, no feasible path. Resting.");
            player.rest();
        }
    }
    
    /**
     * Attempts to move east. If not possible, moves in a random valid non-westward direction.
     * If no such moves are possible, rests.
     */
    private void moveEastOrRandom() {
        // Try to move directly East first using player.canMove(Direction, Map)
        if (player.canMove(Direction.EAST, map)) {
            movePlayer(Direction.EAST); // movePlayer is from Brain superclass
            return;
        }

        // If direct East is not feasible, try other eastward or random valid non-westward directions
        List<Direction> possibleDirections = new ArrayList<>(List.of(
            Direction.NORTHEAST, Direction.SOUTHEAST, // Prioritize other eastward
            Direction.NORTH, Direction.SOUTH          // Then neutral (non-westward)
        ));
        Collections.shuffle(possibleDirections, random); // Randomize order of attempts

        for (Direction dir : possibleDirections) {
            if (player.canMove(dir, map)) {
                movePlayer(dir);
                return;
            }
        }
        
        // If no preferred moves are feasible, rest.
        System.out.println("GreedBrain: No preferred eastward/random move feasible. Resting.");
        player.rest();
    }
    
    @Override
    protected boolean shouldTradeWith(Trader trader) {
        if (player == null || trader == null) return false;
        // GreedBrain trades if it has excess resources (to get gold) 
        // or needs resources desperately and has a lot of gold (less likely to spend gold).
        boolean hasExcessFood = player.getCurrentFood() > player.getMaxFood() * 0.8;
        boolean hasExcessWater = player.getCurrentWater() > player.getMaxWater() * 0.8;
        
        // Only interested in trades that yield gold or offload significant excess.
        return hasExcessFood || hasExcessWater;
    }
    
    @Override
    protected void initiateTrade(Trader trader) {
        if (player == null || trader == null) return;

        TradeOffer offer = new TradeOffer();
        boolean offeredSomething = false;
        int goldRequestedTotal = 0;

        // Offer excess food for gold
        if (player.getCurrentFood() > player.getMaxFood() * 0.75) { // Slightly higher threshold for greed
            int foodToOffer = (int) (player.getCurrentFood() - player.getMaxFood() * 0.5);
            if (foodToOffer > 0) {
                offer.setFoodOffered(foodToOffer);
                goldRequestedTotal += Math.max(1, foodToOffer / 2); // Greedier: wants at least 1 gold per 2 food
                offeredSomething = true;
            }
        }
        
        // Offer excess water for gold
        if (player.getCurrentWater() > player.getMaxWater() * 0.75) {
            int waterToOffer = (int) (player.getCurrentWater() - player.getMaxWater() * 0.5);
            if (waterToOffer > 0) {
                offer.setWaterOffered(waterToOffer);
                // Water might be valued more, request more gold per water
                goldRequestedTotal += Math.max(1, (int)Math.ceil(waterToOffer / 1.5)); 
                offeredSomething = true;
            }
        }
        offer.setGoldRequested(goldRequestedTotal);

        if (offeredSomething && offer.isValid() && offer.getGoldRequested() > 0) {
            System.out.println("GreedBrain proposing offer to " + trader.getPersonality() + ": " + offer);
            if(player.canAffordOffer(offer)) { // Player can afford what they are offering
                boolean accepted = trader.evaluatePlayerOffer(offer);
                if (accepted) {
                    if(player.finalizeTrade(offer)) {
                        System.out.println("GreedBrain: Trade successful (initial offer for gold accepted).");
                        trader.reset();
                    } else {
                        System.out.println("GreedBrain: Trade accepted by trader, but player failed to finalize.");
                        trader.reset(); // Or handle rollback
                    }
                } else { // Initial offer rejected
                    System.out.println("GreedBrain: Initial offer for gold rejected. Trader state: " + trader.getCurrentState());
                    TradeOffer counterOffer = trader.generateCounterOffer(offer);
                    if (counterOffer != null) {
                        System.out.println("GreedBrain: Trader countered with: " + counterOffer);
                        // GreedBrain is unlikely to accept counters unless they are very good for it
                        // (e.g., trader offers even MORE gold for the same items).
                        // For simplicity, GreedBrain rejects most counters.
                        System.out.println("GreedBrain: Rejecting counter offer.");
                        trader.rejectTraderCounterOffer();
                        trader.reset();
                    } else {
                         System.out.println("GreedBrain: Trader did not make a counter-offer. Negotiation ends.");
                         trader.reset();
                    }
                }
            } else {
                System.out.println("GreedBrain: Cannot afford to make the intended offer: " + offer);
            }
        } else {
            System.out.println("GreedBrain: Decided not to make a trade offer (not enough excess or no gold to gain).");
        }
    }

    // isPathFeasible is inherited from Brain.java
    // followPath is inherited from Brain.java, which now uses player.canMove(dir, map) via movePlayer
}
