package wss.game;

import java.util.List;
import java.util.ArrayList;

public class Path {
    private List<Direction> steps;
    private int totalMovementCost;
    private int totalWaterCost;
    private int totalFoodCost;

    public Path(List<Direction> steps, int move, int water, int food) {
        this.steps = steps;
        this.totalMovementCost = move;
        this.totalWaterCost = water;
        this.totalFoodCost = food;
    }
public List<Direction> getDirections() {
    return steps;
}


    public List<Direction> getSteps() { return steps; }
    public int getTotalMovementCost() { return totalMovementCost; }
    public int getTotalWaterCost() { return totalWaterCost; }
    public int getTotalFoodCost() { return totalFoodCost; }

    // Static helper for one-step paths
    public static Path of(Direction dir, int move, int water, int food) {
        List<Direction> steps = new ArrayList<>();
        steps.add(dir);
        return new Path(steps, move, water, food);
    }
}
