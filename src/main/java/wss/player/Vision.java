package wss.player;

import wss.game.*;
import wss.items.Item;

import java.util.*;

/**
 * Scans the surrounding squares and returns candidate paths ordered
 * from most‑ to least‑desirable.
 */
public abstract class Vision {

    /** Manhattan radius (diamond shape) to scan around the player. */
    protected final int scanRadius;

    protected Vision(int scanRadius) {
        this.scanRadius = scanRadius;
    }

    /** Find and rank paths the player could follow. */
    public List<Path> findBestPaths(Player player, Map map) {
        Position start = player.getPosition();
        List<Path> paths       = new ArrayList<>();
        Map<Path, Double> score = new HashMap<>();

        for (int dx = -scanRadius; dx <= scanRadius; dx++) {
            for (int dy = -scanRadius; dy <= scanRadius; dy++) {
                if (Math.abs(dx) + Math.abs(dy) > scanRadius) continue;   // diamond
                int x = start.getX() + dx;
                int y = start.getY() + dy;
                if (!map.inBounds(x, y)) continue;

                Position target = new Position(x, y);
                double value = evaluateSquare(target, map);
                if (value <= 0) continue;                                // ignore dull squares

                Path p = Path
