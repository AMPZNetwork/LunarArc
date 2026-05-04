package io.ampznetwork.lunararc.launcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NeoForgeInstaller {
    public static void install(Path workingDir, java.util.Properties versions) throws Exception {
        ConsoleUI.printStep("Checking NeoForge installation...");
        Path installerJar = Paths.get("neoforge-installer.jar");
        
        String neoforgeVersion = versions.getProperty("neoforge", "21.1.228");
        String url = String.format("https://maven.neoforged.net/releases/net/neoforged/neoforge/%s/neoforge-%s-installer.jar", neoforgeVersion, neoforgeVersion);
        
        if (!Files.exists(installerJar)) {
            ConsoleUI.printStep("Downloading NeoForge Installer v" + neoforgeVersion + "...");
            Downloader.download(url, installerJar);
        }
        
        Path libDir = Paths.get("libraries");
        boolean needsInstall = true;
        if (Files.exists(libDir)) {
            try (var stream = Files.walk(libDir)) {
                if (stream.anyMatch(p -> p.getFileName().toString().equals("win_args.txt"))) {
                    needsInstall = false;
                }
            }
        }
        
        if (needsInstall) {
            ConsoleUI.printStep("Running headless installer (server mode)...");
            
            // We need to run the installer JAR as a separate process to perform the 'server' installation
            ProcessBuilder pb = new ProcessBuilder(
                LauncherUtils.getJavaExecutable(), "-jar", installerJar.toAbsolutePath().toString(), "--installServer"
            );
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                ConsoleUI.printError("NeoForge installer failed with exit code: " + exitCode);
                return;
            }
            ConsoleUI.printSuccess("Installation complete.");
        }
        
        ConsoleUI.printSuccess("NeoForge environment ready.");
        NeoForgeLauncher.launch(workingDir);
    }
}
