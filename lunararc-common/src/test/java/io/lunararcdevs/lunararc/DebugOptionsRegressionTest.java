package io.lunararcdevs.lunararc;

import io.lunararcdevs.lunararc.common.LunarArcDebug;
import io.lunararcdevs.lunararc.common.debug.LunarArcPluginDebug;
import io.lunararcdevs.lunararc.common.server.LunarArcTimings;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DebugOptionsRegressionTest {
    public static void main(String[] args) throws Exception {
        String mode = args[0];
        boolean all = mode.equals("all");
        boolean timing = all || mode.equals("timing");
        boolean reflect = all || mode.equals("reflect,command");
        if (LunarArcDebug.TIMING != timing || LunarArcDebug.REFLECT != reflect
                || LunarArcDebug.COMMAND != reflect || LunarArcDebug.PLUGIN != all
                || LunarArcDebug.FLUID != all) throw new AssertionError("debug channel selection failed");
        LunarArcDebug.fluid("disabled channel probe");
        LunarArcDebug.reflect("reflection probe");
        LunarArcPluginDebug.startSession();
        long start = LunarArcTimings.phaseStart();
        LunarArcTimings.recordStartupPhase("probe", start);
        LunarArcTimings.logStartupSummary();
        var entries = LunarArcTimings.class.getDeclaredField("startupEntries");
        entries.setAccessible(true);
        if (((java.util.List<?>) entries.get(null)).isEmpty() == timing) throw new AssertionError("timing collection gate failed");
        if (Files.exists(Path.of("logs/lunararc-plugin-debug.log")) != all) throw new AssertionError("plugin debug gate failed");
        if (Files.exists(Path.of("logs/lunararc-debug.log")) != !mode.equals("off")) throw new AssertionError("debug file gate failed");
        var launcher = Class.forName("io.lunararcdevs.lunararc.launcher.StartupTimer");
        launcher.getMethod("record", String.class, long.class).invoke(null, "probe", 1000L);
        var phases = launcher.getDeclaredField("PHASES");
        phases.setAccessible(true);
        if (((java.util.List<?>) phases.get(null)).isEmpty() == timing) throw new AssertionError("launcher timing gate failed");
        if (mode.equals("off")) {
            var error = new UnsupportedClassVersionError("class file version 69.0");
            var loader = io.lunararcdevs.lunararc.common.server.LunarArcPluginLoader.class;
            io.lunararcdevs.lunararc.common.server.LunarArcPluginLoader.warnJavaVersionOnce(Path.of("first.jar"), error);
            io.lunararcdevs.lunararc.common.server.LunarArcPluginLoader.warnJavaVersionOnce(Path.of("./first.jar"), error);
            io.lunararcdevs.lunararc.common.server.LunarArcPluginLoader.warnJavaVersionOnce(Path.of("second.jar"), error);
            var warnings = loader.getDeclaredField("JAVA_VERSION_WARNINGS");
            warnings.setAccessible(true);
            if (((java.util.Set<?>) warnings.get(null)).size() != 2) throw new AssertionError("warning deduplication failed");
        }
        System.out.println("Debug options passed: " + mode);
    }
}
