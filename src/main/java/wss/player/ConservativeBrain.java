package wss.player;

import wss.player.Player;
import wss.game.Map;
import wss.game.Path;
import wss.game.Direction;



public class ConservativeBrain extends Brain {
@Override
public void makeMove(Player player, Map map) {
    Path path = player.getVision().closestFood(player, map);

    if (path == null) {
        path = player.getVision().closestWater(player, map);
    }

    if (path == null) {
        path = player.getVision().easiestPath(player, map);
    }

    if (path != null && !path.getSteps().isEmpty()) {
        Direction dir = path.getSteps().get(0);
        boolean moved = player.move(dir, map.getGrid());
        System.out.println("Tried to move " + dir + ": " + (moved ? "Success" : "Failed"));
    } else {
        System.out.println("No path found. Resting...");
        player.rest();
    }
}


}
