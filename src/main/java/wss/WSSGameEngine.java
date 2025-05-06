package wss;

import wss.game.*;
import wss.items.*;
import wss.player.*;

public class WSSGameEngine {
    private Map map;
    private Player player;
    private Brain brain;
    private int turns;
    private boolean gameOver;

    public WSSGameEngine(int width, int height, Difficulty difficulty, BrainType brainType) {
        this.map = new Map();
map.generateMap(width, height, wss.game.Difficulty.valueOf(difficulty.name()));
        this.player = new Player(10, 10, 10, new Cautious(), createBrain(brainType));
        this.turns = 0;
        this.gameOver = false;
        System.out.println("\nGenerated Map:");
        map.printMap();
    }

    private Brain createBrain(BrainType type) {
        switch (type) {
            case GREED -> {
                return new GreedBrain(player, map);
            }
            case CONSERVATIVE -> {
                return new ConservativeBrain(player, map);
            }
            default -> {
                return new NormalBrain(player, map);
            }
        }
    }

    public void runGame() {
        while (!gameOver) {
            turns++;
            updateGameState();
            brain.makeMove();

            // Check win/loss conditions
            if (player.getX() >= map.getWidth() - 1) {
                System.out.println("\uD83C\uDF89 Player reached the east edge and won!");
                gameOver = true;
            } else if (player.getCurrentFood() <= 0 ||
                    player.getCurrentWater() <= 0 ||
                    player.getCurrentStrength() <= 0) {
                System.out.println("\uD83D\uDC80 Player ran out of resources. Game Over.");
                gameOver = true;
            } else if (turns >= 1000) {
                System.out.println("Max turns reached! Game Over.");
                gameOver = true;
            }
        }

        printFinalStats();
    }

    private void updateGameState() {
        player.consumeFood(1);
        player.consumeWater(1);
        player.updateVision();
    }

    private void printFinalStats() {
        System.out.println("\n=== Game Statistics ===");
        System.out.println("Turns taken: " + turns);
        System.out.println("Final position: (" + player.getX() + ", " + player.getY() + ")");
        System.out.println("Resources remaining:");
        System.out.println("  Food: " + player.getCurrentFood() + "/" + player.getMaxFood());
        System.out.println("  Water: " + player.getCurrentWater() + "/" + player.getMaxWater());
        System.out.println("  Strength: " + player.getCurrentStrength() + "/" + player.getMaxStrength());
        System.out.println("  Gold: " + player.getCurrentGold());
    }

    public enum BrainType {
        NORMAL, GREED, CONSERVATIVE
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}