package io.ampznetwork.lunararc.launcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ForgeInstaller {
    public static void install(Path workingDir, java.util.Properties versions) throws Exception {
        System.out.println("Installing Forge...");

        String mcVersion = LauncherUtils.requireVersion(versions, "minecraft");
        String forgeVersion = LauncherUtils.requireVersion(versions, "forge");
        Path installerJar = Paths.get("forge-" + mcVersion + "-" + forgeVersion + "-installer.jar");
        String url = String.format(
                "https://maven.minecraftforge.net/net/minecraftforge/forge/%s-%s/forge-%s-%s-installer.jar", mcVersion,
                forgeVersion, mcVersion, forgeVersion);

        if (!Files.exists(installerJar)) {
            Downloader.download(url, installerJar);
        }

        Path libDir = Paths.get("libraries");
        Path versionSentinel = libDir.resolve(".lunararc-forge-version");
        boolean needsInstall = true;

        if (Files.exists(versionSentinel)) {
            String installedVersion = Files.readString(versionSentinel).trim();
            if (installedVersion.equals(forgeVersion)) {
                needsInstall = false;
            }
        }

        if (needsInstall) {
            System.out.println("Running Forge installer (this may take a few minutes)...");

            ProcessBuilder pb = new ProcessBuilder(
                    LauncherUtils.getJavaExecutable(), "-jar", installerJar.toAbsolutePath().toString(), "--installServer");
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.print("Forge installer failed with exit code: ");
                System.err.println(exitCode);
                return;
            }
            System.out.println("Forge installation complete!");
            Files.writeString(versionSentinel, forgeVersion);
        }

        System.out.println("Forge libraries ready.");
        // Forge launch logic is similar to NeoForge but might have different entry
        // point
        ForgeLauncher.launch(workingDir);
    }
}
