package io.ampznetwork.lunararc.launcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NeoForgeLauncher {
    public static void launch(Path workingDir) throws Exception {
        System.out.println("Preparing NeoForge launch arguments...");

        Path libDir = Paths.get("libraries");
        // Find the args file. It's usually in libraries/net/neoforged/neoforge/<version>/win_args.txt
        Path argsFile = null;
        try (var stream = Files.walk(libDir)) {
            argsFile = stream.filter(p -> p.getFileName().toString().equals("win_args.txt"))
                             .findFirst()
                             .orElse(null);
        }

        if (argsFile == null) {
            System.err.println("Could not find win_args.txt! NeoForge installation might be corrupt.");
            return;
        }

        System.out.println("Using args file: " + argsFile);
        List<String> jvmArgs = Files.readAllLines(argsFile);
        
        List<String> command = new ArrayList<>();
        command.add("java");
        
        // Add Arclight/LunarArc specific flags here if needed
        // command.add("-Dlunararc.server=true");

        // Parse win_args.txt content
        for (String line : jvmArgs) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            
            // Split by space to handle "-p path" or other multi-part arguments
            String[] parts = line.split(" ");
            for (String part : parts) {
                if (part.isEmpty()) continue;
                // Only replace / with \ if it looks like a path and not a module export/open
                if (part.contains("/") && !part.contains("=")) {
                    command.add(part.replace("/", "\\"));
                } else {
                    command.add(part);
                }
            }
        }

        command.add("net.neoforged.bootstrap.BootstrapLauncher");
        command.add("--nogui");

        System.out.println("========================================");
        System.out.println("   " + System.getProperty("lunararc.name", "LunarArc") + " is now launching NeoForge...");
        System.out.println("========================================");

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        Process process = pb.start();
        System.exit(process.waitFor());
    }
}
