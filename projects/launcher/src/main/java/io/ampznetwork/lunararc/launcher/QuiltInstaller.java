package io.ampznetwork.lunararc.launcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class QuiltInstaller {
    public static void install(Path workingDir, java.util.Properties versions) throws Exception {
        System.out.println("Installing Quilt...");
        Path installerJar = Paths.get("quilt-installer.jar");
        Path quiltServerJar = workingDir.resolve("quilt-server-launch.jar");
        Path minecraftServerJar = workingDir.resolve("server.jar");
        
        String mcVersion = versions.getProperty("minecraft", "1.21.1");
        String loaderVersion = versions.getProperty("quilt", "0.26.3");
        String installerVersion = versions.getProperty("quiltInstaller", "0.9.2");
        
        String installerUrl = String.format("https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-installer/%s/quilt-installer-%s.jar", installerVersion, installerVersion);
        
        if (!Files.exists(quiltServerJar) || !Files.exists(minecraftServerJar)) {
            System.out.println("Quilt or Minecraft server JAR missing. Starting installation...");
            if (!Files.exists(installerJar)) {
                Downloader.download(installerUrl, installerJar);
            }
            
            System.out.println("Running Quilt installer for version " + mcVersion + "...");
            ProcessBuilder pb = new ProcessBuilder(
                LauncherUtils.getJavaExecutable(), "-jar", installerJar.toAbsolutePath().toString(), "install", "server", 
                mcVersion, "--loader-version", loaderVersion, "--download-minecraft"
            );
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                System.err.println("Quilt installer failed with exit code: " + exitCode);
                return;
            }

            if (!Files.exists(minecraftServerJar)) {
                // Quilt installer might name it minecraft_server.X.X.X.jar
                Path altJar = workingDir.resolve("minecraft_server." + mcVersion + ".jar");
                if (Files.exists(altJar)) {
                    Files.move(altJar, minecraftServerJar);
                }
            }

            System.out.println("Quilt installation complete!");
        }
        
        System.out.println("Quilt ready.");
        QuiltLauncher.launch(workingDir);
    }
}
