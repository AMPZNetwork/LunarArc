package io.ampznetwork.lunararc.launcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FabricInstaller {
    public static void install(Path workingDir, java.util.Properties versions) throws Exception {
        System.out.println("Installing Fabric...");
        Path installerJar = Paths.get("fabric-installer.jar");
        Path fabricServerJar = workingDir.resolve("fabric-server-launch.jar");
        Path minecraftServerJar = workingDir.resolve("server.jar");
        
        String mcVersion = versions.getProperty("minecraft", "1.21.1");
        String fabricVersion = versions.getProperty("fabric", "0.16.0");
        String installerVersion = versions.getProperty("fabricInstaller", "0.11.2");
        
        String installerUrl = String.format("https://maven.fabricmc.net/net/fabricmc/fabric-installer/%s/fabric-installer-%s.jar", installerVersion, installerVersion);
        
        if (!Files.exists(fabricServerJar) || !Files.exists(minecraftServerJar)) {
            System.out.println("Fabric or Minecraft server JAR missing. Starting installation...");
            if (!Files.exists(installerJar)) {
                Downloader.download(installerUrl, installerJar);
            }
            
            System.out.println("Running Fabric installer for version " + mcVersion + " (Loader: " + fabricVersion + ")...");
            ProcessBuilder pb = new ProcessBuilder(
                LauncherUtils.getJavaExecutable(), "-jar", installerJar.toAbsolutePath().toString(), "server", 
                "-mcversion", mcVersion, "-loader", fabricVersion, "-downloadMinecraft"
            );
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                System.err.println("Fabric installer failed with exit code: " + exitCode);
                return;
            }

            // Sometimes the installer creates 'fabric-server-launch.jar' which is just a tiny wrapper.
            // We want to ensure 'server.jar' exists because Fabric knot needs it.
            if (!Files.exists(minecraftServerJar)) {
                // Check if it was named minecraft_server.X.X.X.jar
                Path altJar = workingDir.resolve("minecraft_server." + mcVersion + ".jar");
                if (Files.exists(altJar)) {
                    Files.move(altJar, minecraftServerJar);
                }
            }

            System.out.println("Fabric installation complete!");
        }
        
        System.out.println("Fabric ready.");
        FabricLauncher.launch(workingDir);
    }
}
