package wss.player;

import wss.game.Square;
import wss.game.Direction;
import wss.game.Path;
import wss.game.Map;
import wss.player.Player;


public class Cautious extends Vision {
@Override
public Path closestFood(Player player, Map map) {
    int x = player.getX();
    int y = player.getY();
    Square squareEast = map.getSquare(x + 1, y);

    if (map.inBounds(x + 1, y) && squareEast.hasFoodBonus()) {
        return Path.of(Direction.EAST, squareEast.getMovementCost(), squareEast.getWaterCost(), squareEast.getFoodCost());
    }

    return null;
}


	@Override
	public Path closestWater(Player player, Map map) {
		return null;
	}

	@Override
	public Path closestGold(Player player, Map map) {
		return null;
	}

	@Override
	public Path closestTrader(Player player, Map map) {
		return null;
	}

	@Override
	public Path easiestPath(Player player, Map map) {
		return null;
	}

	@Override
	public Path secondClosestFood(Player player, Map map) {
		return null;
	}

	@Override
	public Path secondClosestWater(Player player, Map map) {
		return null;
	}

	@Override
	public Path secondClosestGold(Player player, Map map) {
		return null;
	}

	@Override
	public Path secondClosestTrader(Player player, Map map) {
		return null;
	}
}
