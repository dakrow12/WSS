package wss;

import wss.game.*;
import wss.items.FoodBonus;
import wss.player.*;
import wss.trader.Trader;
import java.util.Scanner;
import wss.items.*;
import wss.trader.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose mode:\n1 = Full game with engine\n2 = Small unit test map");
        int mode = scanner.nextInt();

        if (mode == 1) {
            runFullGame(scanner);
        } else {
            runUnitTest();
        }

        scanner.close();
    }

    private static void runFullGame(Scanner scanner) {
        System.out.print("Enter map width: ");
        int width = scanner.nextInt();
        System.out.print("Enter map height: ");
        int height = scanner.nextInt();

        System.out.print("Choose difficulty (EASY, MEDIUM, HARD): ");
        String diffInput = scanner.next().toUpperCase();
        WSSGameEngine.Difficulty difficulty;
        try {
            difficulty = WSSGameEngine.Difficulty.valueOf(diffInput);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid difficulty. Defaulting to EASY.");
            difficulty = WSSGameEngine.Difficulty.EASY;
        }

        System.out.print("Choose brain (NORMAL, GREED, CONSERVATIVE): ");
        String brainInput = scanner.next().toUpperCase();
        WSSGameEngine.BrainType brainType;
        try {
            brainType = WSSGameEngine.BrainType.valueOf(brainInput);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid brain. Defaulting to NORMAL.");
            brainType = WSSGameEngine.BrainType.NORMAL;
        }

        WSSGameEngine game = new WSSGameEngine(width, height, difficulty, brainType);
        game.runGame();
    }

    private static void runUnitTest() {
        // Step 1: Create a simple map (3x3)
        Square[][] grid = new Square[3][3];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                grid[x][y] = new Square("plains", 1, 1, 1);
            }
        }

        // Step 2: Place a FoodBonus east of the start
        grid[1][0].setItem(new FoodBonus(5, false));

        // Step 3: Create a map, player, and brain without circular dependencies
        Map map = new Map();
        map.setGrid(grid);
        
        // First create the player with null brain
        Vision vision = new Cautious();
        Player player = new Player(10, 10, 10, vision, null);
        
        // Then create the brain with the player
        Brain brain = new ConservativeBrain(player, map);
        
        // Finally set the brain on the player
        player.setBrain(brain);

        // Step 4: Display and make move
        System.out.println("Starting position: (" + player.getX() + ", " + player.getY() + ")");
        System.out.println("Food: " + player.getCurrentFood());

        brain.makeMove();

        System.out.println("After move: (" + player.getX() + ", " + player.getY() + ")");
        System.out.println("Food: " + player.getCurrentFood());
    }
}
