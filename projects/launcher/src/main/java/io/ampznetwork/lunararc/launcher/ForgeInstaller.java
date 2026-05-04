package io.ampznetwork.lunararc.launcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ForgeInstaller {
    public static void install(Path workingDir, java.util.Properties versions) throws Exception {
        System.out.println("Installing Forge...");
        Path installerJar = Paths.get("forge-installer.jar");

        String mcVersion = versions.getProperty("minecraft", "1.21.1");
        String forgeVersion = versions.getProperty("forge", "52.1.14");
        String url = String.format(
                "https://maven.minecraftforge.net/net/minecraftforge/forge/%s-%s/forge-%s-%s-installer.jar", mcVersion,
                forgeVersion, mcVersion, forgeVersion);

        if (!Files.exists(installerJar)) {
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
            System.out.println("Running Forge installer (this may take a few minutes)...");

            ProcessBuilder pb = new ProcessBuilder(
                    "java", "-jar", installerJar.toAbsolutePath().toString(), "--installServer");
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.print("Forge installer failed with exit code: ");
                System.err.println(exitCode);
                return;
            }
            System.out.println("Forge installation complete! You may now safely delete "
                    + installerJar.getFileName().toString() + ".");
        }

        System.out.println("Forge libraries ready.");
        // Forge launch logic is similar to NeoForge but might have different entry
        // point
        ForgeLauncher.launch(workingDir);
    }
}
