package wss;

import wss.game.*;
import wss.items.FoodBonus;
import wss.player.*;
import wss.trader.Trader;

public class Main {
    public static void main(String[] args) {
        // Step 1: Create a simple map (3x3)
        Square[][] map = new Square[3][3];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                map[x][y] = new Square("plains", 1, 1, 1); // simple terrain
            }
        }

        // Step 2: Place a FoodBonus east of starting position
        map[1][0].setItem(new FoodBonus(5));

        // Step 3: Create player at (0,0)
        Vision vision = new Cautious();
        Brain brain = new ConservativeBrain();
        Player player = new Player(10, 10, 10, vision, brain);

        System.out.println("Starting position: (" + player.getX() + ", " + player.getY() + ")");
        System.out.println("Food: " + player.getCurrentFood());

        // Step 4: Let the brain make a move
        brain.makeMove(player, new Map(map));

        System.out.println("After move: (" + player.getX() + ", " + player.getY() + ")");
        System.out.println("Food: " + player.getCurrentFood());
    }
}
