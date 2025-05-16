package wss.player;

import wss.game.*;
import wss.items.*;

import java.util.List;

/**
 * Can scan a *very* long distance ( ≈ 8 tiles ) in every direction.
 */
public class FarSight extends Vision {

    private static final int RANGE = 8;

    /* ---- public API ---- */

    @Override public Path closestFood  (Player p, Map m){ return first(food  (p,m)); }
    @Override public Path closestWater (Player p, Map m){ return first(water (p,m)); }
    @Override public Path closestGold  (Player p, Map m){ return first(gold  (p,m)); }
    @Override public Path closestTrader(Player p, Map m){ return first(trader(p,m)); }

    @Override public Path secondClosestFood   (Player p, Map m){ return getNthPath(food  (p,m),2); }
    @Override public Path secondClosestWater  (Player p, Map m){ return getNthPath(water (p,m),2); }
    @Override public Path secondClosestGold   (Player p, Map m){ return getNthPath(gold  (p,m),2); }
    @Override public Path secondClosestTrader (Player p, Map m){ return getNthPath(trader(p,m),2); }

    @Override
    public Path easiestPath(Player player, Map map){
        return super.getNthPath(food(player,map),1);                 // fallback: cheapest food path
    }

    /* ---- private helpers ---- */

    private Path first(List<Path> list){ return list.isEmpty()?null:list.get(0); }

    private List<Path> food  (Player p,Map m){ return search(p,m,sq->sq.getItems().stream().anyMatch(i->i instanceof FoodBonus)); }
    private List<Path> water (Player p,Map m){ return search(p,m,sq->sq.getItems().stream().anyMatch(i->i instanceof WaterBonus));}
    private List<Path> gold  (Player p,Map m){ return search(p,m,sq->sq.getItems().stream().anyMatch(i->i instanceof GoldBonus)); }
    private List<Path> trader(Player p,Map m){ return search(p,m, Square::hasTrader); }

    private List<Path> search(Player p, Map m, ItemTypePredicate pr){
        return findItemsBFS(p,m,pr,RANGE,10);
    }
}
