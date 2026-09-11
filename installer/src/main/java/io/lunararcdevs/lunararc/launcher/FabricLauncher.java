package io.lunararcdevs.lunararc.launcher;

import java.nio.file.Files;
import java.nio.file.Path;

public class FabricLauncher {
    public static void launch(Path workingDir, Path selfPath) throws Exception {
        Path launchJar = workingDir.resolve("fabric-server-launch.jar");
        if (!Files.isRegularFile(launchJar)) {
            System.err.println("[LunarArc] Error: " + launchJar.toAbsolutePath()
                    + " is missing. The Fabric installer has not run, or did not finish.");
            return;
        }

        if (LunarArcAgent.instrumentation == null) {
            System.err.println("[LunarArc] Error: LunarArc's launch agent did not attach; "
                    + "same-JVM launch is required for Fabric and no fallback is available.");
            System.exit(1);
            return;
        }

        Path modFile = workingDir.resolve(".lunararc").resolve("mod_file").resolve("fabric.jar");
        if (Files.isRegularFile(modFile)) {
            System.setProperty("fabric.addMods", modFile.toString());
        }

        LoaderSameJvmLaunch.launchFromManifestClassPath(
                launchJar, "net.fabricmc.loader.impl.launch.server.FabricServerLauncher", selfPath, "Fabric");
    }
}
