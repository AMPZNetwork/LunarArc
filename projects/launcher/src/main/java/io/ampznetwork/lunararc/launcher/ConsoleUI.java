package io.ampznetwork.lunararc.launcher;

import io.ampznetwork.lunararc.i18n.TranslationManager;

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

    public static void printLogo(String minecraftVersion) {
        System.out.println(TranslationManager.get("logo", minecraftVersion));
    }

    public static void printStep(String messageKey, Object... args) {
        System.out.println(BLUE + BOLD + "» " + RESET + WHITE + TranslationManager.get(messageKey, args) + RESET);
    }

    public static void printSuccess(String messageKey, Object... args) {
        System.out.println(GREEN + BOLD + "✔ " + RESET + WHITE + TranslationManager.get(messageKey, args) + RESET);
    }

    public static void printError(String messageKey, Object... args) {
        System.out.println(RED + BOLD + "✘ " + RESET + RED + TranslationManager.get(messageKey, args) + RESET);
    }

    public static void printHeader(String titleKey, Object... args) {
        System.out.println(PURPLE + BOLD + "--- " + TranslationManager.get(titleKey, args) + " ---" + RESET);
    }
}
