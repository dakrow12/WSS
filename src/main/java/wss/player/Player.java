package wss.player;

import wss.game.Direction;
import wss.game.Square;
import wss.items.Item;
import wss.trader.Trader;

public class Player {
    private int maxStrength, currentStrength;
    private int maxFood, currentFood;
    private int maxWater, currentWater;
    private int currentGold;
    private int positionX, positionY;

    private Vision vision;
    private Brain brain;

    public Player(int strength, int food, int water, Vision vision, Brain brain) {
        this.maxStrength = this.currentStrength = strength;
        this.maxFood = this.currentFood = food;
        this.maxWater = this.currentWater = water;
        this.currentGold = 0;
        this.vision = vision;
        this.brain = brain;
        this.positionX = 0;
        this.positionY = 0;
    }

    public boolean move(Direction dir, Square[][] map) {
        int newX = positionX + dir.dx();
        int newY = positionY + dir.dy();

        if (newX < 0 || newY < 0 || newX >= map.length || newY >= map[0].length)
            return false;

        Square target = map[newX][newY];
        int moveCost = target.getMovementCost();
        int foodCost = target.getFoodCost();
        int waterCost = target.getWaterCost();

        if (currentStrength < moveCost || currentFood < foodCost || currentWater < waterCost)
            return false;

        currentStrength -= moveCost;
        currentFood -= foodCost;
        currentWater -= waterCost;

        positionX = newX;
        positionY = newY;
        return true;
    }

    public void rest() {
        currentStrength = Math.min(maxStrength, currentStrength + 2);
        currentFood = Math.max(0, currentFood - 1);
        currentWater = Math.max(0, currentWater - 1);
    }

    public void trade(Trader trader) {
        // TODO: Call trade interaction logic here
    }

    public void collectItem(Item item) {
        item.activate(this);
    }

    // Getters and Setters
    public int getX() { return positionX; }
    public int getY() { return positionY; }

    public Vision getVision() { return vision; }
    public Brain getBrain() { return brain; }

    public void addFood(int amount) {
        currentFood = Math.min(currentFood + amount, maxFood);
    }

    public void addWater(int amount) {
        currentWater = Math.min(currentWater + amount, maxWater);
    }

    public void addGold(int amount) {
        currentGold += amount;
    }

    public int getCurrentFood() { return currentFood; }
    public int getCurrentWater() { return currentWater; }
    public int getCurrentStrength() { return currentStrength; }
}
