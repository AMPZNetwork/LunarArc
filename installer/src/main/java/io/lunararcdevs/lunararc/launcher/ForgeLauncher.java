package io.lunararcdevs.lunararc.launcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ForgeLauncher {
    public static void launch(Path workingDir, Path selfPath) throws Exception {
        removeLegacyBridgeCopy();

        Path libDir = Paths.get("libraries");
        Path argsFile = LauncherUtils.findArgsFile(libDir, "net/minecraftforge/forge");
        if (argsFile == null) {
            System.err.println("[LunarArc] Error: Could not find Forge's args file under "
                    + libDir.toAbsolutePath().resolve("net/minecraftforge/forge")
                    + ". The Forge installer has not run, or did not finish.");
            return;
        }

        if (LunarArcAgent.instrumentation == null) {
            System.err.println("[LunarArc] Error: LunarArc's launch agent did not attach; "
                    + "same-JVM launch is required for Forge and no fallback is available.");
            System.exit(1);
            return;
        }

        LoaderSameJvmLaunch.launchFromArgsFile(workingDir, selfPath, argsFile, "Forge");
    }

    private static void removeLegacyBridgeCopy() {
        try {
            Path oldBridge = Paths.get(".lunararc", "mods", "lunararc-bridge.jar");
            if (Files.deleteIfExists(oldBridge)) {
                System.out.println("[LunarArc] Removed legacy .lunararc/mods/lunararc-bridge.jar; "
                        + "LunarArc now boots from the loader bootstrap layer.");
            }
        } catch (Exception e) {
            System.err.println("[LunarArc] Warning: could not remove legacy "
                    + ".lunararc/mods/lunararc-bridge.jar: " + e.getMessage());
        }
    }
}
