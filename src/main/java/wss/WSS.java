package wss;

import java.util.Scanner;
import wss.game.*;
import wss.items.*;
import wss.player.*;

public class WSS {
    public static void main(String[] args) {
        // Get map dimensions and difficulty from the user
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the width of the map: ");
        int width = scanner.nextInt();
        System.out.print("Enter the height of the map: ");
        int height = scanner.nextInt();
        System.out.print("Choose difficulty (EASY, MEDIUM, HARD): ");
        String difficultyInput = scanner.next().toUpperCase();
        Difficulty difficulty;
        try {
            difficulty = Difficulty.valueOf(difficultyInput);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid difficulty. Using default EASY.");
            difficulty = Difficulty.EASY;
        }
        scanner.close();

        // 1. Generate the map
        Map map = new Map(); 
        map.generateMap(width, height, difficulty);
        System.out.println("\nGenerated Map:");
        map.printMap(); // This line will print the map to the terminal

        // To place traders --- square.setItem(new Trader(TraderType.GREEDY));.
        // You can change personalities

        // 2. Place some food and water bonuses (using the generated map)
        if (map.getHeight() > 0 && map.getWidth() > 1) {
            map.getSquare(1, 0).addItem(new FoodBonus(5, true));
        }
        /*
        if (map.getHeight() > 1 && map.getWidth() > 2) {
            map.getSquare(2, 1).addItem(new WaterBonus(false) {
                @Override
                public void activate(Player p) {
                    p.addWater(5);
                }
            });
        }
        */    

        // 3. Create Map and Player
        Player player = new Player(10, 10, 10, new Cautious(), new ConservativeBrain());

        // 4. Turn loop
        int turn = 1;
        while (true) {
            System.out.println("\n--- Turn " + turn + " ---");
            System.out.println("Position: (" + player.getX() + ", " + player.getY() + ")");
            System.out.println("Strength: " + player.getCurrentStrength());
            System.out.println("Food: " + player.getCurrentFood());
            System.out.println("Water: " + player.getCurrentWater());

            // Check for end conditions
            if (player.getX() == map.getWidth() - 1) {
                System.out.println("🎉 Player reached the east edge and won!");
                break;
            }

            if (player.getCurrentStrength() <= 0 || player.getCurrentFood() <= 0 || player.getCurrentWater() <= 0) {
                System.out.println("💀 Player ran out of resources. Game Over.");
                break;
            }

            // Player takes action
            player.getBrain().makeMove(player, map);

            // Collect item if available
            Square current = map.getSquare(player.getX(), player.getY());
            if (current.getItem() != null) {
                player.collectItem(current.getItem());
                current.setItem(null); // item is one-time use
                System.out.println("Player collected a bonus!");
            }

            turn++;
        }
    }
}