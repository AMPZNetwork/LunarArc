package io.ampznetwork.lunararc.launcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ForgeLauncher {
    public static void launch(Path workingDir) throws Exception {
        System.out.println("Preparing Forge launch arguments...");

        Path libDir = Paths.get("libraries");
        Path argsFile = null;
        try (var stream = Files.walk(libDir)) {
            argsFile = stream.filter(p -> p.getFileName().toString().equals("win_args.txt"))
                             .findFirst()
                             .orElse(null);
        }

        if (argsFile == null) {
            System.err.println("Could not find win_args.txt! Forge installation might be corrupt.");
            return;
        }

        System.out.println("Using args file: " + argsFile);
        List<String> jvmArgs = Files.readAllLines(argsFile);
        
        List<String> command = new ArrayList<>();
        command.add(LauncherUtils.getJavaExecutable());

        Path selfPath = null;
        try {
            selfPath = Paths.get(ForgeLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toAbsolutePath();
        } catch (Exception ignored) {}

        // Parse win_args.txt content
        for (String line : jvmArgs) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split(" ");
            for (String part : parts) {
                if (part.isEmpty()) continue;
                
                if (part.contains("/") && !part.contains("=")) {
                    command.add(part.replace("/", "\\"));
                } else {
                    command.add(part);
                }
            }
        }

        command.add("--nogui");

        System.out.println("========================================");
        System.out.println("   " + System.getProperty("lunararc.name", "LunarArc") + " is now launching Forge...");
        System.out.println("========================================");

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        Process process = pb.start();
        System.exit(process.waitFor());
    }
}
