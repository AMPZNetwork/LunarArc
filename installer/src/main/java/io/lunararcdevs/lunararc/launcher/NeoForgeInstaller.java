package io.lunararcdevs.lunararc.launcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NeoForgeInstaller {
    public static void install(Path workingDir, java.util.Properties versions, Path selfPath) throws Exception {
        String mcVersion = LauncherUtils.requireVersion(versions, "minecraft");
        String neoforgeVersion = LauncherUtils.requireVersion(versions, "neoforge");
        Path installerJar = Paths.get("neoforge-" + mcVersion + "-" + neoforgeVersion + "-installer.jar");

        String url = String.format(
                "https://maven.neoforged.net/releases/net/neoforged/neoforge/%s/neoforge-%s-installer.jar",
                neoforgeVersion, neoforgeVersion);

        if (!Files.exists(installerJar)) {
            ConsoleUI.printStep("Downloading missing libraries...");
            Downloader.download(url, installerJar);
        }

        Path libDir = Paths.get("libraries");
        Path versionSentinel = libDir.resolve(".lunararc-neoforge-version");
        boolean needsInstall = true;

        if (Files.exists(versionSentinel)) {
            String installedVersion = Files.readString(versionSentinel).trim();
            if (installedVersion.equals(neoforgeVersion)) {
                needsInstall = false;
            }
        }

        if (needsInstall) {
            ConsoleUI.printStep("NeoForge installation is starting, please wait...");

            ProcessBuilder pb = new ProcessBuilder(
                    LauncherUtils.getJavaExecutable(), "-jar", installerJar.toAbsolutePath().toString(),
                    "--installServer");
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                ConsoleUI.printError("NeoForge installer failed with exit code: " + exitCode);
                return;
            }
            Files.writeString(versionSentinel, neoforgeVersion);
        }

        NeoForgeLauncher.launch(workingDir, selfPath);
    }
}
