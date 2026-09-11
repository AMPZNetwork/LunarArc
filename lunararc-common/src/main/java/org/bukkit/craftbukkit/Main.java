package org.bukkit.craftbukkit;

public class Main {
    public static final java.time.Instant BOOT_TIME = java.time.Instant.now();
    public static boolean useJline = true;
    public static boolean useConsole = true;

    public static void main(String[] args) {
        try {
            Class.forName("io.lunararcdevs.lunararc.launcher.Launcher")
                    .getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (java.lang.reflect.InvocationTargetException error) {
            Throwable cause = error.getCause();
            if (cause instanceof RuntimeException runtime) throw runtime;
            if (cause instanceof Error fatal) throw fatal;
            throw new IllegalStateException("LunarArc launcher failed", cause);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Run the LunarArc hybrid JAR with its bundled launcher", error);
        }
    }
}
