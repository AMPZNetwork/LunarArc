package io.lunararcdevs.lunararc.launcher;

import java.nio.file.Files;
import java.nio.file.Path;

public class QuiltLauncher {
    public static void launch(Path workingDir, Path selfPath) throws Exception {
        Path launchJar = workingDir.resolve("quilt-server-launch.jar");
        if (!Files.isRegularFile(launchJar)) {
            System.err.println("[LunarArc] Error: " + launchJar.toAbsolutePath()
                    + " is missing. The Quilt installer has not run, or did not finish.");
            return;
        }

        if (LunarArcAgent.instrumentation == null) {
            System.err.println("[LunarArc] Error: LunarArc's launch agent did not attach; "
                    + "same-JVM launch is required for Quilt and no fallback is available.");
            System.exit(1);
            return;
        }

        Path modFile = workingDir.resolve(".lunararc").resolve("mod_file").resolve("quilt.jar");
        if (Files.isRegularFile(modFile)) {
            System.setProperty("loader.addMods", modFile.toString());
        }

        LoaderSameJvmLaunch.launchFromManifestClassPath(
                launchJar, "org.quiltmc.loader.impl.launch.server.QuiltServerLauncher", selfPath, "Quilt");
    }
}
