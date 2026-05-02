package io.ampznetwork.lunararc.launcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NeoForgeInstaller {
    public static void install(Path workingDir, java.util.Properties versions) throws Exception {
        System.out.println("Installing NeoForge...");
        Path installerJar = workingDir.resolve("neoforge-installer.jar");
        
        String neoforgeVersion = versions.getProperty("neoforge", "21.1.228");
        String url = String.format("https://maven.neoforged.net/releases/net/neoforged/neoforge/%s/neoforge-%s-installer.jar", neoforgeVersion, neoforgeVersion);
        
        if (!Files.exists(installerJar)) {
            Downloader.download(url, installerJar);
        }
        
        Path libDir = Paths.get("libraries");
        if (!Files.exists(libDir)) {
            System.out.println("Running NeoForge installer (this may take a few minutes)...");
            
            // We need to run the installer JAR as a separate process to perform the 'server' installation
            ProcessBuilder pb = new ProcessBuilder(
                "java", "-jar", installerJar.toAbsolutePath().toString(), "--installServer"
            );
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                System.err.println("NeoForge installer failed with exit code: " + exitCode);
                return;
            }
        }
        
        System.out.println("NeoForge libraries and arguments ready.");
        NeoForgeLauncher.launch(workingDir);
    }
}
