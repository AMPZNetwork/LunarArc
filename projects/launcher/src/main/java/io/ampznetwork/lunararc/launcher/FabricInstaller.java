package io.ampznetwork.lunararc.launcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FabricInstaller {
    public static void install(Path workingDir, java.util.Properties versions) throws Exception {
        System.out.println("Installing Fabric...");
        Path fabricServerJar = workingDir.resolve("fabric-server-launch.jar");
        Path minecraftServerJar = workingDir.resolve("server.jar");
        Path versionSentinel = workingDir.resolve(".lunararc-fabric-version");

        String mcVersion = LauncherUtils.requireVersion(versions, "minecraft");
        String fabricVersion = LauncherUtils.requireVersion(versions, "fabric");
        String installerVersion = LauncherUtils.requireVersion(versions, "fabricInstaller");

        Path installerJar = Paths.get("fabric-" + mcVersion + "-" + fabricVersion + "-installer.jar");

        String installerUrl = String.format(
                "https://maven.fabricmc.net/net/fabricmc/fabric-installer/%s/fabric-installer-%s.jar", installerVersion,
                installerVersion);

        String combinedVersion = fabricVersion + ":" + installerVersion;
        boolean needsInstall = !Files.exists(fabricServerJar) || !Files.exists(minecraftServerJar);

        if (!needsInstall && Files.exists(versionSentinel)) {
            String installedVersion = Files.readString(versionSentinel).trim();
            if (!installedVersion.equals(combinedVersion)) {
                needsInstall = true;
            }
        }

        if (needsInstall) {
            System.out.println("Fabric or Minecraft server JAR missing. Starting installation...");
            if (!Files.exists(installerJar)) {
                Downloader.download(installerUrl, installerJar);
            }

            System.out.println(
                    "Running Fabric installer for version " + mcVersion + " (Loader: " + fabricVersion + ")...");
            ProcessBuilder pb = new ProcessBuilder(
                    LauncherUtils.getJavaExecutable(), "-jar", installerJar.toAbsolutePath().toString(), "server",
                    "-mcversion", mcVersion, "-loader", fabricVersion, "-downloadMinecraft");
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("Fabric installer failed with exit code: " + exitCode);
                return;
            }

            if (!Files.exists(minecraftServerJar)) {
                Path altJar = workingDir.resolve("minecraft_server." + mcVersion + ".jar");
                if (Files.exists(altJar)) {
                    Files.move(altJar, minecraftServerJar);
                }
            }

            System.out.println("Fabric installation complete!");
            Files.writeString(versionSentinel, combinedVersion);
        }

        System.out.println("Fabric ready.");
        FabricLauncher.launch(workingDir);
    }
}
