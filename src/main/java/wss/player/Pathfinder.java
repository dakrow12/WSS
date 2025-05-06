package wss.game;

import java.util.*;

/**
 * Simple A* search that returns the cheapest {@link Path} (movement‑cost)
 * between two positions.  Water/food costs are accumulated in the returned
 * Path object but are not part of the heuristic weight.
 */
public final class PathFinder {

    /** Internal A* node. */
    private record Node(Position pos, Node prev,
                        int gMove, int gWater, int gFood, int f) {}

    private PathFinder() {}  // utility class – no instances

    /**
     * Compute the shortest (lowest‑movement‑cost) path from {@code start} to
     * {@code goal}. Returns {@code null} if no path exists.
     */
    public static Path shortestPath(Map map, Position start, Position goal) {
        if (start.getX() == goal.getX() && start.getY() == goal.getY()) {
            return new Path(List.of(), 0, 0, 0);  // already there
        }

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Set<Position> closed = new HashSet<>();

        open.add(new Node(start, null, 0, 0, 0, heuristic(start, goal)));

        while (!open.isEmpty()) {
            Node current = open.poll();

            if (current.pos.getX() == goal.getX() && current.pos.getY() == goal.getY()) {
                return buildPath(current);
            }
            if (!closed.add(current.pos)) continue;  // already processed

            for (Direction d : Direction.values()) {
                Position next = current.pos.move(d);
                if (!map.inBounds(next.getX(), next.getY())) continue;

                Square sq = map.getSquare(next);
                int moveCost  = sq.getMovementCost();
                int waterCost = sq.getWaterCost();
                int foodCost  = sq.getFoodCost();

                int gMove  = current.gMove  + moveCost;
                int gWater = current.gWater + waterCost;
                int gFood  = current.gFood  + foodCost;
                int f = gMove + heuristic(next, goal);   // movement drives heuristic

                open.add(new Node(next, current, gMove, gWater, gFood, f));
            }
        }
        return null;  // unreachable if goal is connected
    }

    /** Manhattan‑distance heuristic (works for rook + diagonal moves). */
    private static int heuristic(Position a, Position b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    /** Rebuild the Direction list and cumulative costs from a goal Node. */
    private static Path buildPath(Node node) {
        List<Direction> dirs = new ArrayList<>();
        Node cur = node;
        while (cur.prev != null) {
            dirs.add(0, cur.prev.pos.directionTo(cur.pos));
            cur = cur.prev;
        }
        return new Path(dirs, node.gMove, node.gWater, node.gFood);
    }
}
