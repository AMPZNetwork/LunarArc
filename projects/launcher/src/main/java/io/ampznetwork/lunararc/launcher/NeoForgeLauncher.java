package io.ampznetwork.lunararc.launcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class NeoForgeLauncher {
    public static void launch(Path workingDir) throws Exception {
        Path libDir = Paths.get("libraries");
        if (!Files.exists(libDir)) {
            System.err.println("[LunarArc] Error: 'libraries' folder missing. Installation may have failed.");
            return;
        }
        Path argsFile = null;
        try (var stream = Files.walk(libDir)) {
            argsFile = stream.filter(p -> p.getFileName().toString().equals("win_args.txt"))
                    .findFirst()
                    .orElse(null);
        }

        if (argsFile == null) {
            System.err.println("[LunarArc] Error: Could not find NeoForge win_args.txt.");
            return;
        }

        List<String> jvmArgs = Files.readAllLines(argsFile);
        List<String> command = new ArrayList<>();
        command.add(LauncherUtils.getJavaExecutable());

        // Parse NeoForge arguments from win_args.txt
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

        System.out.println("[LunarArc] Booting NeoForge Core...");
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        Process process = pb.start();
        System.exit(process.waitFor());
    }
}
