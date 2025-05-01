package wss;

import wss.player.*;
import wss.game.*;
import wss.items.*;

public class WSS {
    public static void main(String[] args) {
        // 1. Create a 5x3 map
        Square[][] grid = new Square[5][3];
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 3; y++) {
                grid[x][y] = new Square("plains", 1, 1, 1);
            }
        }

        // 2. Place some food and water bonuses
        grid[1][0].setItem(new FoodBonus(5));
        grid[2][1].setItem(new WaterBonus() {
            @Override public void activate(Player p) { p.addWater(5); }
        });

        // 3. Create Map and Player
        Map map = new Map(grid);
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
            if (player.getX() == grid.length - 1) {
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
