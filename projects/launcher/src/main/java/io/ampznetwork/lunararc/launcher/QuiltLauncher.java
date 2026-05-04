package io.ampznetwork.lunararc.launcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class QuiltLauncher {
    public static void launch(Path workingDir) throws Exception {
        System.out.println("Launching Quilt...");
        
        List<String> command = new ArrayList<>();
        command.add(LauncherUtils.getJavaExecutable());
        command.add("-jar");
        command.add("quilt-server-launch.jar");
        command.add("--nogui");

        System.out.println("========================================");
        System.out.println("   " + System.getProperty("lunararc.name", "LunarArc") + " is now launching Quilt...");
        System.out.println("========================================");

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        Process process = pb.start();
        System.exit(process.waitFor());
    }
}
