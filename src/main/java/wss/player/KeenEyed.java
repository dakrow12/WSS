package wss.player;

import wss.game.*;
import wss.items.*;

import java.util.List;

/**
 * Short-to-mid range (≤ 4) but *very* quick at picking the cheapest path.
 */
public class KeenEyed extends Vision {

    private static final int RANGE = 4;

    /* Basic dispatch – always pick cost-minimum path from the BFS list */
    @Override public Path closestFood   (Player p, Map m){ return top(food  (p,m)); }
    @Override public Path closestWater  (Player p, Map m){ return top(water (p,m)); }
    @Override public Path closestGold   (Player p, Map m){ return top(gold  (p,m)); }
    @Override public Path closestTrader (Player p, Map m){ return top(trader(p,m)); }

    @Override public Path secondClosestFood   (Player p, Map m){ return getNthPath(food  (p,m),2); }
    @Override public Path secondClosestWater  (Player p, Map m){ return getNthPath(water (p,m),2); }
    @Override public Path secondClosestGold   (Player p, Map m){ return getNthPath(gold  (p,m),2); }
    @Override public Path secondClosestTrader (Player p, Map m){ return getNthPath(trader(p,m),2); }

    @Override
    public Path easiestPath(Player player, Map map){
        return chooseCheapestAdjacent(player,map);
    }

    /* ---- helpers ---- */

    private Path top(List<Path> ls){ return ls.isEmpty()?null:ls.get(0); }

    private List<Path> food  (Player p,Map m){ return seek(p,m, sq->sq.getItems().stream().anyMatch(i->i instanceof FoodBonus)); }
    private List<Path> water (Player p,Map m){ return seek(p,m, sq->sq.getItems().stream().anyMatch(i->i instanceof WaterBonus));}
    private List<Path> gold  (Player p,Map m){ return seek(p,m, sq->sq.getItems().stream().anyMatch(i->i instanceof GoldBonus)); }
    private List<Path> trader(Player p,Map m){ return seek(p,m, Square::hasTrader); }

    private List<Path> seek(Player p, Map m, ItemTypePredicate pr){
        return findItemsBFS(p,m,pr,RANGE,6);
    }

    /** Single-step cheapest-cost helper shared by many visions */
    private Path chooseCheapestAdjacent(Player pl, Map map){
        int x = pl.getX(), y = pl.getY();
        Path best=null;
        int bestCost=Integer.MAX_VALUE;
        for(Direction d: Direction.values()){
            int nx = x+d.dx(), ny = y+d.dy();
            if(!map.inBounds(nx,ny)) continue;
            Square s = map.getSquare(nx,ny);
            int c  = s.getMovementCost()+s.getFoodCost()+s.getWaterCost();
            if(c < bestCost){
                bestCost = c;
                best = Path.of(d,s.getMovementCost(),s.getWaterCost(),s.getFoodCost());
            }
        }
        return best;
    }
}
