package wss.player;

import wss.player.Player;
import wss.game.Map;


public class ConservativeBrain extends Brain {
@Override
public void makeMove(Player player, Map map) {
    Path path = player.getVision().closestFood(player, map);

    if (path == null) {
        System.out.println("No path to food found.");
        return;
    }

    if (path.getSteps().isEmpty()) {
        System.out.println("Path is empty.");
        return;
    }

    Direction dir = path.getSteps().get(0);
    boolean success = player.move(dir, map.getGrid());

    System.out.println("Tried to move " + dir + ": " + (success ? "Success" : "Failed"));
}

}
