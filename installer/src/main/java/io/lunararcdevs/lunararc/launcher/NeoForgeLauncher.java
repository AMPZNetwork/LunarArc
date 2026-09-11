package io.lunararcdevs.lunararc.launcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NeoForgeLauncher {

    public static void launch(Path workingDir, Path selfPath) throws Exception {
        removeLegacyBridgeCopy();

        Path libDir = Paths.get("libraries");
        if (!Files.exists(libDir)) {
            System.err.println("[LunarArc] Error: 'libraries' folder missing. Installation may have failed.");
            return;
        }

        Path argsFile = LauncherUtils.findArgsFile(libDir, "net/neoforged/neoforge");
        if (argsFile == null) {
            System.err.println("[LunarArc] Error: Could not find NeoForge args file.");
            return;
        }

        if (LunarArcAgent.instrumentation == null) {
            System.err.println("[LunarArc] Error: LunarArc's launch agent did not attach; "
                    + "same-JVM launch is required for NeoForge and no fallback is available.");
            System.exit(1);
            return;
        }

        LoaderSameJvmLaunch.launchFromArgsFile(workingDir, selfPath, argsFile, "NeoForge");
    }

    private static void removeLegacyBridgeCopy() {
        try {
            Path oldBridge = Paths.get("mods", "lunararc-bridge.jar");
            if (Files.deleteIfExists(oldBridge)) {
                System.out.println("[LunarArc] Removed legacy mods/lunararc-bridge.jar; LunarArc now boots from the loader bootstrap layer.");
            }
        } catch (Exception e) {
            System.err.println("[LunarArc] Warning: could not remove legacy mods/lunararc-bridge.jar: " + e.getMessage());
        }
    }
}
