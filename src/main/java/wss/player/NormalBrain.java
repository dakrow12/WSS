package wss.player;

import wss.game.Direction;
import wss.game.Map;
import wss.game.Path;
import wss.game.Position;
import wss.game.Square;
import wss.items.Item;
import wss.trader.TradeOffer;
import wss.trader.TradeResponse;
import wss.trader.Trader;
import wss.util.GameLogger;
import wss.WSSGameEngine;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class NormalBrain extends Brain {
    // Simple thresholds for strength management
    private static final double CRITICAL_STRENGTH = 0.3;
    private int consecutiveMoves = 0;
    
    public NormalBrain(Player player, Map map) {
        super(player, map);
    }
    
    public NormalBrain(Player player, Map map, WSSGameEngine gameEngine) {
        super(player, map, gameEngine);
    }
    
    @Override
    public void makeMove() {
        GameLogger.section("Decision Making");
        
        // Check if we need to rest based on strength
        if (shouldRest()) {
            rest();
            consecutiveMoves = 0;
            return;
        }
        
        // Try to move east whenever possible
        boolean moved = false;
        
        // Direct east is best
        if (player.canMove(Direction.EAST)) {
            GameLogger.info("Moving EAST (optimal direction)");
            movePlayer(Direction.EAST);
            moved = true;
        }
        // Northeast and southeast are second best
        else if (player.canMove(Direction.NORTHEAST)) {
            GameLogger.info("Moving NORTHEAST (second best option)");
            movePlayer(Direction.NORTHEAST);
            moved = true;
        }
        else if (player.canMove(Direction.SOUTHEAST)) {
            GameLogger.info("Moving SOUTHEAST (second best option)");
            movePlayer(Direction.SOUTHEAST);
            moved = true;
        }
        // North and south to find a path around obstacles
        else if (player.canMove(Direction.NORTH)) {
            GameLogger.info("Moving NORTH (searching for path)");
            movePlayer(Direction.NORTH);
            moved = true;
        }
        else if (player.canMove(Direction.SOUTH)) {
            GameLogger.info("Moving SOUTH (searching for path)");
            movePlayer(Direction.SOUTH);
            moved = true;
        }
        
        if (moved) {
            consecutiveMoves++;
            GameLogger.info("Consecutive moves: " + consecutiveMoves);
        } else {
            // If no moves possible, rest
            GameLogger.warning("No valid moves available, resting instead");
            rest();
            consecutiveMoves = 0;
        }
        
        // Always collect items after moving
        Square currentSquare = map.getSquare(player.getX(), player.getY());
        currentSquare.collectItem(player);
        
        // Simple trading when encountering traders
        if (currentSquare.hasTrader()) {
            Trader trader = currentSquare.getTrader();
            if (trader.getCurrentState() == wss.trader.TraderState.NEGOTIATING) {
                initiateTrade(trader);
            }
        }
    }
    
    private boolean shouldRest() {
        // Rest if strength is critically low
        if (player.getCurrentStrength() < player.getMaxStrength() * CRITICAL_STRENGTH) {
            GameLogger.info("Strength critically low (" + player.getCurrentStrength() + "/" + 
                          player.getMaxStrength() + "), need to rest");
            return true;
        }
        
        // Rest after several consecutive moves
        if (consecutiveMoves >= 3) {
            GameLogger.info("Made " + consecutiveMoves + " consecutive moves, time to rest");
            return true;
        }
        
        return false;
    }
    
    @Override
    protected void rest() {
        GameLogger.section("Resting");
        GameLogger.info("Resting to recover strength");
        player.rest();
        consecutiveMoves = 0;
        GameLogger.success("Strength recovered: " + player.getCurrentStrength());
    }
    
    private TradeOffer createSimpleTradeOffer() {
        TradeOffer offer = new TradeOffer();
        
        // Simple trading - trade what we have more of for what we have less of
        if (player.getCurrentFood() > player.getCurrentWater() + 5) {
            // Trade excess food for water
            offer.setFoodOffered(3);
            offer.setWaterRequested(3);
            GameLogger.info("Trading strategy: excess food for water");
        } 
        else if (player.getCurrentWater() > player.getCurrentFood() + 5) {
            // Trade excess water for food
            offer.setWaterOffered(3);
            offer.setFoodRequested(3);
            GameLogger.info("Trading strategy: excess water for food");
        }
        // Use gold if we have it
        else if (player.getCurrentGold() > 0) {
            offer.setGoldOffered(1);
            
            // Ask for what we need most
            if (player.getCurrentFood() < player.getCurrentWater()) {
                offer.setFoodRequested(2);
                GameLogger.info("Trading strategy: gold for food");
            } else {
                offer.setWaterRequested(2);
                GameLogger.info("Trading strategy: gold for water");
            }
        }
        // Fallback trade
        else {
            // Just trade 1:1 if no clear advantage
            offer.setFoodOffered(2);
            offer.setWaterRequested(2);
            GameLogger.info("Trading strategy: fallback food for water");
        }
        
        return offer;
    }
    
    @Override
    protected boolean shouldTradeWith(Trader trader) {
        return true;
    }
    
    @Override
    protected void initiateTrade(Trader trader) {
        GameLogger.section("Trading");
        
        TradeOffer offer = createSimpleTradeOffer();
        GameLogger.info("Making offer: " + offer);
        
        TradeResponse response = trader.makeTrade(offer);
        
        if (response.isAccepted()) {
            GameLogger.success("Trade accepted!");
            player.finalizeTrade(response.getFinalOffer());
            GameLogger.info("Resources after trade: Food=" + player.getCurrentFood() + 
                           ", Water=" + player.getCurrentWater() + 
                           ", Gold=" + player.getCurrentGold());
        } else {
            GameLogger.warning("Trade rejected. Checking for counter-offer...");
            TradeOffer counter = trader.generateCounterOffer(offer);
            
            if (counter != null && counter.isValid() && player.canAffordOffer(counter)) {
                GameLogger.info("Received counter-offer: " + counter);
                GameLogger.info("Counter-offer is acceptable, accepting");
                trader.acceptTraderCounterOffer();
                player.finalizeTrade(counter);
                GameLogger.info("Resources after trade: Food=" + player.getCurrentFood() + 
                               ", Water=" + player.getCurrentWater() + 
                               ", Gold=" + player.getCurrentGold());
            } else if (counter == null) {
                GameLogger.warning("No counter-offer received");
            } else if (!counter.isValid()) {
                GameLogger.error("Counter-offer is invalid");
            } else {
                GameLogger.error("Cannot afford counter-offer");
            }
        }
    }
}

