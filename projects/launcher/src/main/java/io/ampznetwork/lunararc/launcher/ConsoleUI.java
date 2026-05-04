package io.ampznetwork.lunararc.launcher;

/**
 * Utility for ANSI colors and rich console formatting.
 */
public class ConsoleUI {
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String ITALIC = "\u001B[3m";
    
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    
    public static final String BG_BLACK = "\u001B[40m";
    
    public static void printLogo() {
        System.out.println(CYAN + BOLD + "    __                              ___" + RESET);
        System.out.println(CYAN + BOLD + "   / /   __  ______  ____ ______   /   |  __________" + RESET);
        System.out.println(CYAN + BOLD + "  / /   / / / / __ \\/ __ `/ ___/  / /| | / ___/ ___/" + RESET);
        System.out.println(CYAN + BOLD + " / /___/ /_/ / / / / /_/ / /     / ___ |/ /  / /__" + RESET);
        System.out.println(CYAN + BOLD + "/_____/\\__,_/_/ /_/\\__,_/_/     /_/  |_/_/   \\___/" + RESET);
        System.out.println(PURPLE + "    Unified Hybrid Server Environment - v1.21.1" + RESET);
        System.out.println();
    }

    public static void printStep(String message) {
        System.out.println(BLUE + BOLD + "» " + RESET + WHITE + message + RESET);
    }

    public static void printSuccess(String message) {
        System.out.println(GREEN + BOLD + "✔ " + RESET + WHITE + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(RED + BOLD + "✘ " + RESET + RED + message + RESET);
    }

    public static void printHeader(String title) {
        System.out.println(PURPLE + BOLD + "--- " + title + " ---" + RESET);
    }
}
