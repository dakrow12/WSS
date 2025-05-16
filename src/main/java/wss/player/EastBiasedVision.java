package wss.player;

import wss.game.*;
import wss.items.*;

import java.util.Comparator;
import java.util.List;

/**
 * “Compass-headed” eyes – always prefers things lying to the EAST
 * (larger x-coordinate) and will look up to 5 squares away.
 */
public class EastBiasedVision extends Vision {

    /* Search range for this vision type */
    private static final int RANGE = 5;

    /* ---------- public API ---------- */

    @Override public Path closestFood   (Player p, Map m){ return chooseEast(findFood (p,m)); }
    @Override public Path closestWater  (Player p, Map m){ return chooseEast(findWater(p,m)); }
    @Override public Path closestGold   (Player p, Map m){ return chooseEast(findGold (p,m)); }
    @Override public Path closestTrader (Player p, Map m){ return chooseEast(findTrader(p,m)); }
    @Override public Path secondClosestFood   (Player p, Map m){ return getNthPath(findFood  (p,m),2); }
    @Override public Path secondClosestWater  (Player p, Map m){ return getNthPath(findWater (p,m),2); }
    @Override public Path secondClosestGold   (Player p, Map m){ return getNthPath(findGold  (p,m),2); }
    @Override public Path secondClosestTrader (Player p, Map m){ return getNthPath(findTrader(p,m),2); }

    /** Chooses the lowest-cost path; if more than one path shares the same
     *  cost, the one that ends furthest EAST (highest x) wins.               */
    private Path chooseEast(List<Path> candidates) {
        if (candidates==null || candidates.isEmpty()) return null;

        return candidates.stream()
            .min( Comparator
                    .comparingInt(Path::getTotalMovementCost)
                    .thenComparingInt(Path::getTotalFoodCost)
                    .thenComparingInt(Path::getTotalWaterCost)
                    /* break ties by “more east” – we reconstruct end-pos */
                    .thenComparingInt(p -> finalX(p)) // higher X after reversing → larger negative
                )
            .orElse(null);
    }

    /* Utility: simulate the path to get its end X coordinate */
    private int finalX(Path path) {
        int x = 0;                       // local origin is fine for ordering
        for(Direction d : path.getDirections()) x += d.dx();
        return -x;                       // negate so Comparator.min prefers bigger X
    }

    @Override
    public Path easiestPath(Player player, Map map){
        return super.getNthPath(
                findItemsBFS(player,map,sq->true,1,Direction.values().length),1);
    }

    /* ---------- internal search helpers ---------- */

    private List<Path> findFood  (Player p,Map m){ return baseSearch(p,m, sq->sq.getItems().stream().anyMatch(i->i instanceof FoodBonus)); }
    private List<Path> findWater (Player p,Map m){ return baseSearch(p,m, sq->sq.getItems().stream().anyMatch(i->i instanceof WaterBonus));}
    private List<Path> findGold  (Player p,Map m){ return baseSearch(p,m, sq->sq.getItems().stream().anyMatch(i->i instanceof GoldBonus)); }
    private List<Path> findTrader(Player p,Map m){ return baseSearch(p,m, Square::hasTrader); }

    private List<Path> baseSearch(Player p, Map m, ItemTypePredicate pred){
        return findItemsBFS(p,m,pred,RANGE,8);   // grab a handful, sort later
    }
}
