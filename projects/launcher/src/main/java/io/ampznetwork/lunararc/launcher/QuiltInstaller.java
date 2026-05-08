package io.ampznetwork.lunararc.launcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class QuiltInstaller {
    public static void install(Path workingDir, java.util.Properties versions) throws Exception {
        System.out.println("Installing Quilt...");
        Path quiltServerJar = workingDir.resolve("quilt-server-launch.jar");
        Path minecraftServerJar = workingDir.resolve("server.jar");
        Path versionSentinel = workingDir.resolve(".lunararc-quilt-version");

        String mcVersion = LauncherUtils.requireVersion(versions, "minecraft");
        String loaderVersion = LauncherUtils.requireVersion(versions, "quilt");
        String installerVersion = LauncherUtils.requireVersion(versions, "quiltInstaller");

        Path installerJar = Paths.get("quilt-" + mcVersion + "-" + loaderVersion + "-installer.jar");

        String installerUrl = String.format(
                "https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-installer/%s/quilt-installer-%s.jar",
                installerVersion, installerVersion);

        String combinedVersion = loaderVersion + ":" + installerVersion;
        boolean needsInstall = !Files.exists(quiltServerJar) || !Files.exists(minecraftServerJar);

        if (!needsInstall && Files.exists(versionSentinel)) {
            String installedVersion = Files.readString(versionSentinel).trim();
            if (!installedVersion.equals(combinedVersion)) {
                needsInstall = true;
            }
        }

        if (needsInstall) {
            System.out.println("Quilt or Minecraft server JAR missing. Starting installation...");
            if (!Files.exists(installerJar)) {
                Downloader.download(installerUrl, installerJar);
            }

            System.out.println("Running Quilt installer for version " + mcVersion + "...");
            ProcessBuilder pb = new ProcessBuilder(
                    LauncherUtils.getJavaExecutable(), "-jar", installerJar.toAbsolutePath().toString(), "install",
                    "server",
                    mcVersion, "--loader-version", loaderVersion, "--download-minecraft");
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("Quilt installer failed with exit code: " + exitCode);
                return;
            }

            if (!Files.exists(minecraftServerJar)) {
                Path altJar = workingDir.resolve("minecraft_server." + mcVersion + ".jar");
                if (Files.exists(altJar)) {
                    Files.move(altJar, minecraftServerJar);
                }
            }

            System.out.println("Quilt installation complete!");
            Files.writeString(versionSentinel, combinedVersion);
        }

        System.out.println("Quilt ready.");
        QuiltLauncher.launch(workingDir);
    }
}
