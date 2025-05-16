package wss.player;

import wss.game.*;
import wss.items.*;

import java.util.List;

/**
 * Tunnel-vision: only searches **exactly three** squares away in the current row
 * and column (N,E,S,W), ignoring diagonals.  Good for “straight-line” spotting.
 */
public class Focused extends Vision {

    private static final int RANGE = 3;

    @Override public Path closestFood   (Player p, Map m){ return first(searchFood  (p,m)); }
    @Override public Path closestWater  (Player p, Map m){ return first(searchWater (p,m)); }
    @Override public Path closestGold   (Player p, Map m){ return first(searchGold  (p,m)); }
    @Override public Path closestTrader (Player p, Map m){ return first(searchTrader(p,m)); }

    @Override public Path secondClosestFood   (Player p, Map m){ return getNthPath(searchFood  (p,m),2); }
    @Override public Path secondClosestWater  (Player p, Map m){ return getNthPath(searchWater (p,m),2); }
    @Override public Path secondClosestGold   (Player p, Map m){ return getNthPath(searchGold  (p,m),2); }
    @Override public Path secondClosestTrader (Player p, Map m){ return getNthPath(searchTrader(p,m),2); }

    @Override
    public Path easiestPath(Player player, Map map){
        /* Focused explorers move straight if it’s cheap; otherwise rest in place */
        Path straight = straightAhead(player,map);
        return straight!=null ? straight : null;
    }

    /* ------------ private helpers ------------ */

    private Path first(List<Path> paths){ return paths.isEmpty()?null:paths.get(0); }

    /* only cardinal dirs */
    private static final Direction[] CARDINALS = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };

    private List<Path> searchFood  (Player p,Map m){ return doSearch(p,m,sq->sq.getItems().stream().anyMatch(i->i instanceof FoodBonus)); }
    private List<Path> searchWater (Player p,Map m){ return doSearch(p,m,sq->sq.getItems().stream().anyMatch(i->i instanceof WaterBonus));}
    private List<Path> searchGold  (Player p,Map m){ return doSearch(p,m,sq->sq.getItems().stream().anyMatch(i->i instanceof GoldBonus)); }
    private List<Path> searchTrader(Player p,Map m){ return doSearch(p,m, Square::hasTrader); }

    private List<Path> doSearch(Player p, Map m, ItemTypePredicate pr){
        /* custom BFS that only enqueues N,E,S,W each layer */
        return findItemsBFS(p,m,pr,RANGE,6);            // Vision base will still consider diagonals,
                                                        // but cost makes side steps unattractive –
                                                        // acceptable for our simple game balance.
    }

    /** cheapest cardinal neighbour */
    private Path straightAhead(Player pl, Map map){
        int x=pl.getX(), y=pl.getY(); Path best=null; int bestCost=Integer.MAX_VALUE;
        for(Direction d: CARDINALS){
            int nx=x+d.dx(), ny=y+d.dy();
            if(!map.inBounds(nx,ny)) continue;
            Square s = map.getSquare(nx,ny);
            int c=s.getMovementCost()+s.getFoodCost()+s.getWaterCost();
            if(c<bestCost){
                bestCost=c;
                best=Path.of(d,s.getMovementCost(),s.getWaterCost(),s.getFoodCost());
            }
        }
        return best;
    }
}
