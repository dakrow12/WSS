package wss.util;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * A utility class for formatting and displaying game output with consistent styling.
 */
public class GameLogger {
    // ANSI color codes for terminal colors
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    
    // Text style
    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_UNDERLINE = "\u001B[4m";
    
    // Background colors
    public static final String ANSI_BG_BLACK = "\u001B[40m";
    public static final String ANSI_BG_RED = "\u001B[41m";
    public static final String ANSI_BG_GREEN = "\u001B[42m";
    public static final String ANSI_BG_YELLOW = "\u001B[43m";
    public static final String ANSI_BG_BLUE = "\u001B[44m";
    public static final String ANSI_BG_PURPLE = "\u001B[45m";
    public static final String ANSI_BG_CYAN = "\u001B[46m";
    public static final String ANSI_BG_WHITE = "\u001B[47m";
    
    private static boolean useColors = true;
    private static boolean timestampEnabled = false;
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    
    /**
     * Enable or disable colored output
     */
    public static void setUseColors(boolean enable) {
        useColors = enable;
    }
    
    /**
     * Enable or disable timestamp prefix
     */
    public static void setTimestampEnabled(boolean enable) {
        timestampEnabled = enable;
    }
    
    /**
     * Print a header with a title
     */
    public static void header(String title) {
        String border = "═".repeat(title.length() + 10);
        System.out.println("\n╔" + border + "╗");
        System.out.println("║" + " ".repeat(5) + title + " ".repeat(5) + "║");
        System.out.println("╚" + border + "╝");
    }
    
    /**
     * Print a section header
     */
    public static void section(String title) {
        System.out.println("\n" + (useColors ? ANSI_BOLD + ANSI_CYAN : "") + 
                         "┌─── " + title + " " + "─".repeat(50 - title.length()) + 
                         (useColors ? ANSI_RESET : ""));
    }
    
    /**
     * Print successful action result
     */
    public static void success(String message) {
        log((useColors ? ANSI_GREEN : "") + "✓ " + message + (useColors ? ANSI_RESET : ""));
    }
    
    /**
     * Print a warning message
     */
    public static void warning(String message) {
        log((useColors ? ANSI_YELLOW : "") + "⚠ " + message + (useColors ? ANSI_RESET : ""));
    }
    
    /**
     * Print an error message
     */
    public static void error(String message) {
        log((useColors ? ANSI_RED : "") + "✗ " + message + (useColors ? ANSI_RESET : ""));
    }
    
    /**
     * Print an info message
     */
    public static void info(String message) {
        log((useColors ? ANSI_CYAN : "") + "ℹ " + message + (useColors ? ANSI_RESET : ""));
    }
    
    /**
     * Print a game event message
     */
    public static void event(String message) {
        log((useColors ? ANSI_PURPLE : "") + "» " + message + (useColors ? ANSI_RESET : ""));
    }
    
    /**
     * Print standard log message
     */
    public static void log(String message) {
        if (timestampEnabled) {
            System.out.println("[" + timeFormat.format(new Date()) + "] " + message);
        } else {
            System.out.println(message);
        }
    }
    
    /**
     * Print player stats in a formatted way
     */
    public static void playerStats(int food, int water, int strength, int gold, int x, int y) {
        String stats = String.format(
            "Position: (%d, %d) | Food: %d | Water: %d | Strength: %d | Gold: %d",
            x, y, food, water, strength, gold
        );
        
        System.out.println((useColors ? ANSI_BLUE : "") + "┌─" + "─".repeat(stats.length()) + "─┐");
        System.out.println("│ " + stats + " │");
        System.out.println("└─" + "─".repeat(stats.length()) + "─┘" + (useColors ? ANSI_RESET : ""));
    }
    
    /**
     * Print a horizontal divider
     */
    public static void divider() {
        System.out.println((useColors ? ANSI_CYAN : "") + 
                         "─".repeat(80) + 
                         (useColors ? ANSI_RESET : ""));
    }
    
    /**
     * Print a turn header
     */
    public static void turn(int turnNumber) {
        String turnStr = " TURN " + turnNumber + " ";
        int padding = (80 - turnStr.length()) / 2;
        
        System.out.println("\n" + (useColors ? ANSI_BOLD + ANSI_YELLOW : "") +
                         "=".repeat(padding) + turnStr + "=".repeat(padding) +
                         (useColors ? ANSI_RESET : ""));
    }
    
    /**
     * Print trader interaction header
     */
    public static void traderEncounter(String traderType) {
        String message = " TRADER ENCOUNTER: " + traderType + " ";
        int padding = (80 - message.length()) / 2;
        
        System.out.println("\n" + (useColors ? ANSI_BOLD + ANSI_PURPLE : "") +
                         "*".repeat(padding) + message + "*".repeat(padding) +
                         (useColors ? ANSI_RESET : ""));
    }
    
    /**
     * Print item collection message
     */
    public static void itemCollection(String itemType, String effect) {
        log((useColors ? ANSI_GREEN : "") + "[ITEM] Collected " + itemType + ": " + effect + (useColors ? ANSI_RESET : ""));
    }
    
    /**
     * Print movement message
     */
    public static void movement(String direction, int x, int y) {
        log((useColors ? ANSI_BLUE : "") + "[MOVE] Moved " + direction + " to position (" + x + ", " + y + ")" + (useColors ? ANSI_RESET : ""));
    }
    
    /**
     * Print game over message
     */
    public static void gameOver(boolean victory, String reason) {
        if (victory) {
            String message = " VICTORY! ";
            int padding = (80 - message.length()) / 2;
            
            System.out.println("\n" + (useColors ? ANSI_BOLD + ANSI_GREEN : "") +
                             "★".repeat(padding) + message + "★".repeat(padding) +
                             (useColors ? ANSI_RESET : ""));
            
            success(reason);
        } else {
            String message = " GAME OVER ";
            int padding = (80 - message.length()) / 2;
            
            System.out.println("\n" + (useColors ? ANSI_BOLD + ANSI_RED : "") +
                             "×".repeat(padding) + message + "×".repeat(padding) +
                             (useColors ? ANSI_RESET : ""));
            
            error(reason);
        }
    }
} 