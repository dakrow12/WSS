package wss;


import java.util.Scanner;
import wss.WSSGameEngine;



public class WSS {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
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

        scanner.close();

        WSSGameEngine game = new WSSGameEngine(width, height, difficulty, brainType);
        game.runGame();
    }
}
