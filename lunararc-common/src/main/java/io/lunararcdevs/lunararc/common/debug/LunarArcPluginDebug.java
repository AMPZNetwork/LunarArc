package io.lunararcdevs.lunararc.common.debug;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Set;

public final class LunarArcPluginDebug {
    private static final Object LOCK = new Object();
    private static final Path OUTPUT = Path.of("logs", "lunararc-plugin-debug.log");
    private static boolean initialized;

    private LunarArcPluginDebug() {
    }

    public static void startSession() {
        if (!io.lunararcdevs.lunararc.common.LunarArcDebug.PLUGIN) return;
        try {
            synchronized (LOCK) {
                initialize();
            }
        } catch (Throwable ignored) {
            // Debug output must never prevent server startup.
        }
    }

    public static void report(Plugin plugin, String phase, Throwable error) {
        report(plugin, phase, error, null);
    }

    public static void report(Plugin plugin, String phase, Throwable error, String context) {
        if (!io.lunararcdevs.lunararc.common.LunarArcDebug.PLUGIN) return;
        PluginDescriptionFile description = plugin == null ? null : plugin.getDescription();
        File source = sourceOf(plugin);
        String loader = loaderInfo(plugin);
        String combined = context;
        if (loader != null) combined = (combined == null || combined.isBlank()) ? loader : context + ", " + loader;
        write(description, source, phase, error, combined);
    }

    public static void report(PluginDescriptionFile description, File source, String phase,
                              Throwable error, String context) {
        write(description, source, phase, error, context);
    }

    public static void reportTransform(PluginDescriptionFile description, File source,
                                       String className, String mappingNamespace,
                                       boolean nmsRemap, boolean legacyCraftBukkitAdapter,
                                       Throwable error) {
        String context = "class=" + className
                + ", mappingNamespace=" + mappingNamespace
                + ", nmsRemap=" + nmsRemap
                + ", legacyCraftBukkitAdapter=" + legacyCraftBukkitAdapter;
        write(description, source, "CLASS_TRANSFORM", error, context);
    }

    public static void reportDependency(Plugin plugin, String reason) {
        write(plugin == null ? null : plugin.getDescription(), sourceOf(plugin),
                "DEPENDENCY", null, reason);
    }

    private static void write(PluginDescriptionFile description, File source, String phase,
                              Throwable error, String context) {
        if (!io.lunararcdevs.lunararc.common.LunarArcDebug.PLUGIN) return;
        try {
            synchronized (LOCK) {
                initialize();
                try (BufferedWriter writer = Files.newBufferedWriter(OUTPUT, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                    writer.write("\n================================================================================\n");
                    writer.write("time: " + Instant.now() + "\n");
                    writer.write("phase: " + safe(phase) + "\n");
                    writer.write("plugin: " + pluginName(description) + "\n");
                    if (description != null) {
                        writer.write("version: " + safe(description.getVersion()) + "\n");
                        writer.write("main: " + safe(description.getMain()) + "\n");
                        writer.write("api-version: " + safe(description.getAPIVersion()) + "\n");
                    }
                    writer.write("source: " + (source == null ? "<unknown>" : source.getAbsolutePath()) + "\n");
                    if (context != null && !context.isBlank()) writer.write("context: " + context + "\n");

                    if (error != null) {
                        Throwable root = rootCause(error);
                        writer.write("exception: " + error.getClass().getName() + ": " + safe(error.getMessage()) + "\n");
                        if (root != error) {
                            writer.write("root-cause: " + root.getClass().getName() + ": " + safe(root.getMessage()) + "\n");
                        }
                        writer.write("category: " + classify(root) + "\n");
                        String surface = compatibilitySurface(root);
                        writer.write("compatibility-surface: " + surface + "\n");
                        String missingSymbol = missingSymbol(root);
                        if (missingSymbol != null) writer.write("missing-symbol: " + missingSymbol + "\n");
                        StackTraceElement location = bestLocation(root, description);
                        writer.write("likely-location: " + (location == null ? "<unknown>" : location.toString()) + "\n");
                        writer.write("compatibility-hints: " + hints(root) + "\n");
                        writer.write("stacktrace:\n" + stackTrace(error));
                    } else {
                        writer.write("category: DEPENDENCY_ORDER\n");
                        writer.write("likely-location: plugin dependency metadata / previous dependency failure\n");
                    }
                    writer.flush();
                }
            }
        } catch (Throwable ignored) {
            // Diagnostics must never make a plugin failure worse or alter server behaviour.
        }
    }

    private static void initialize() throws IOException {
        if (initialized) return;
        Files.createDirectories(OUTPUT.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(OUTPUT, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("LunarArc temporary plugin compatibility diagnostics\n");
            writer.write("session-start: " + Instant.now() + "\n");
            writer.write("file: " + OUTPUT.toAbsolutePath() + "\n");
            writer.write("This file is passive diagnostics only; it does not suppress or rewrite failures.\n");
        }
        initialized = true;
    }

    private static String loaderInfo(Plugin plugin) {
        if (plugin == null) return null;
        ClassLoader loader = plugin.getClass().getClassLoader();
        if (loader instanceof org.bukkit.plugin.java.PluginClassLoader pcl) {
            return "mappingNamespace=" + pcl.getMappingNamespace().name()
                    + ", nmsRemap=" + pcl.isNmsRemappingEnabled();
        }
        return "classLoader=" + (loader == null ? "bootstrap" : loader.getClass().getName());
    }

    private static File sourceOf(Plugin plugin) {
        if (plugin == null) return null;
        try {
            if (plugin.getClass().getProtectionDomain() != null
                    && plugin.getClass().getProtectionDomain().getCodeSource() != null
                    && plugin.getClass().getProtectionDomain().getCodeSource().getLocation() != null) {
                return new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static Throwable rootCause(Throwable error) {
        Throwable current = error;
        Set<Throwable> seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        while (current.getCause() != null && seen.add(current)) current = current.getCause();
        return current;
    }

    private static StackTraceElement bestLocation(Throwable error, PluginDescriptionFile description) {
        if (error == null) return null;
        String main = description == null ? null : description.getMain();
        String pluginPackage = null;
        if (main != null && main.contains(".")) pluginPackage = main.substring(0, main.lastIndexOf('.'));

        StackTraceElement fallback = null;
        for (StackTraceElement frame : error.getStackTrace()) {
            String owner = frame.getClassName();
            if (pluginPackage != null && owner.startsWith(pluginPackage)) return frame;
            if (fallback == null && !isInfrastructure(owner)) fallback = frame;
        }
        return fallback != null ? fallback : (error.getStackTrace().length == 0 ? null : error.getStackTrace()[0]);
    }

    private static boolean isInfrastructure(String owner) {
        return owner.startsWith("java.") || owner.startsWith("jdk.") || owner.startsWith("sun.")
                || owner.startsWith("org.bukkit.") || owner.startsWith("io.papermc.paper.")
                || owner.startsWith("io.lunararcdevs.lunararc.") || owner.startsWith("org.spongepowered.asm.");
    }

    private static String classify(Throwable error) {
        if (error == null) return "UNKNOWN";

        String surface = compatibilitySurface(error);
        boolean craft = "CRAFTBUKKIT".equals(surface);
        boolean paper = "PAPER".equals(surface);

        if (error instanceof ClassNotFoundException || error instanceof NoClassDefFoundError) {
            if (craft) return "MISSING_CRAFTBUKKIT_CLASS";
            if (paper) return "MISSING_PAPER_CLASS";
            return "CLASSLOADER_OR_REMAP";
        }
        if (error instanceof NoSuchMethodError || error instanceof NoSuchMethodException) {
            if (craft) return "MISSING_CRAFTBUKKIT_METHOD";
            if (paper) return "MISSING_PAPER_METHOD";
            return "METHOD_ABI_OR_REFLECTION";
        }
        if (error instanceof NoSuchFieldError || error instanceof NoSuchFieldException) {
            if (craft) return "MISSING_CRAFTBUKKIT_FIELD";
            if (paper) return "MISSING_PAPER_FIELD";
            return "FIELD_ABI_OR_REFLECTION";
        }
        if (error instanceof AbstractMethodError) {
            if (craft) return "CRAFTBUKKIT_API_IMPLEMENTATION_GAP";
            if (paper) return "PAPER_API_IMPLEMENTATION_GAP";
            return "ABSTRACT_METHOD_IMPLEMENTATION_GAP";
        }
        if (error instanceof UnsupportedOperationException) {
            if (craft) return "CRAFTBUKKIT_API_IMPLEMENTATION_GAP";
            if (paper) return "PAPER_API_IMPLEMENTATION_GAP";
            return "UNSUPPORTED_API_PATH";
        }
        if (error instanceof IllegalAccessError || error instanceof IllegalAccessException) return "PAPER_NMS_ACCESS";
        if (error instanceof ClassCastException) return "TYPE_BRIDGE_OR_RUNTIME_TYPE";
        if (error instanceof LinkageError) return "JVM_LINKAGE";
        String message = String.valueOf(error.getMessage());
        if (message.contains("Mixin") || error.getClass().getName().contains("mixin")) return "MIXIN_TARGET";
        if (message.contains("remap") || message.contains("mapping")) return "REMAP_OR_MAPPING";
        return "PLUGIN_RUNTIME";
    }

    private static String compatibilitySurface(Throwable error) {
        if (error == null) return "UNKNOWN";
        StringBuilder explicit = new StringBuilder();
        StringBuilder frames = new StringBuilder();
        Set<Throwable> seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        Throwable current = error;
        while (current != null && seen.add(current)) {
            explicit.append(' ').append(current.getClass().getName());
            if (current.getMessage() != null) explicit.append(' ').append(current.getMessage());
            for (StackTraceElement frame : current.getStackTrace()) {
                frames.append(' ').append(frame.getClassName());
            }
            current = current.getCause();
        }

        String direct = explicit.toString().replace('/', '.');
        if (direct.contains("io.papermc.paper.") || direct.contains("com.destroystokyo.paper.")) return "PAPER";
        if (direct.contains("org.bukkit.craftbukkit.")) return "CRAFTBUKKIT";
        if (direct.contains("net.minecraft.")) return "NMS";
        if (direct.contains("org.bukkit.")) return "BUKKIT_API";

        String trace = frames.toString().replace('/', '.');
        if (trace.contains("io.papermc.paper.") || trace.contains("com.destroystokyo.paper.")) return "PAPER";
        if (trace.contains("org.bukkit.craftbukkit.")) return "CRAFTBUKKIT";
        if (trace.contains("net.minecraft.")) return "NMS";
        if (trace.contains("org.bukkit.")) return "BUKKIT_API";
        return "OTHER";
    }

    private static String missingSymbol(Throwable error) {
        if (error == null) return null;
        if (!(error instanceof ClassNotFoundException)
                && !(error instanceof NoClassDefFoundError)
                && !(error instanceof NoSuchMethodError)
                && !(error instanceof NoSuchMethodException)
                && !(error instanceof NoSuchFieldError)
                && !(error instanceof NoSuchFieldException)
                && !(error instanceof AbstractMethodError)
                && !(error instanceof UnsupportedOperationException)) {
            return null;
        }
        String message = error.getMessage();
        if (message == null || message.isBlank()) return error.getClass().getSimpleName();
        return safe(message);
    }

    private static String hints(Throwable error) {
        if (error == null) return "none";
        if (error instanceof IllegalAccessError || error instanceof IllegalAccessException) {
            return "Check whether the plugin expects a Paper-widened NMS member; prefer a narrow LunarArc accessor/bridge.";
        }
        String surface = compatibilitySurface(error);
        if ((error instanceof ClassNotFoundException || error instanceof NoClassDefFoundError)
                && "CRAFTBUKKIT".equals(surface)) {
            return "Missing CraftBukkit compatibility class. Check LunarArc-owned Craft class coverage and legacy CraftBukkit package remapping.";
        }
        if ((error instanceof ClassNotFoundException || error instanceof NoClassDefFoundError)
                && "PAPER".equals(surface)) {
            return "Missing Paper API/runtime compatibility class. Check whether LunarArc exposes the Paper 1.21.1 class expected by the plugin.";
        }
        if ((error instanceof NoSuchMethodError || error instanceof NoSuchMethodException)
                && "CRAFTBUKKIT".equals(surface)) {
            return "Missing CraftBukkit method/descriptor. Compare the expected Craft 1.21.1 signature with LunarArc's concrete Craft implementation.";
        }
        if ((error instanceof NoSuchMethodError || error instanceof NoSuchMethodException)
                && "PAPER".equals(surface)) {
            return "Missing Paper method/descriptor. Implement the supported Paper 1.21.1 API surface directly rather than masking the failure.";
        }
        if ((error instanceof NoSuchFieldError || error instanceof NoSuchFieldException)
                && "CRAFTBUKKIT".equals(surface)) {
            return "Missing CraftBukkit field ABI. Check whether the plugin relies on a Craft implementation detail that LunarArc must provide or remap.";
        }
        if ((error instanceof NoSuchFieldError || error instanceof NoSuchFieldException)
                && "PAPER".equals(surface)) {
            return "Missing Paper field ABI. Check Paper-patched API/runtime fields and add only the narrow compatibility bridge actually required.";
        }
        if (error instanceof AbstractMethodError && ("CRAFTBUKKIT".equals(surface) || "PAPER".equals(surface))) {
            return "A CraftBukkit/Paper interface method exists but LunarArc's concrete implementation is missing it. Implement the method with loader-owned Minecraft semantics.";
        }
        if (error instanceof NoSuchFieldError || error instanceof NoSuchFieldException) {
            return "Check Paper-vs-loader field ABI and reflection against runtime-added Bukkit enum/registry values.";
        }
        if (error instanceof NoSuchMethodError || error instanceof NoSuchMethodException) {
            return "Check Paper-vs-loader method signature/descriptor and whether remapping changed the owner or descriptor.";
        }
        if (error instanceof ClassNotFoundException || error instanceof NoClassDefFoundError) {
            return "Check plugin dependency visibility, mapping namespace, CraftBukkit version references, and transformed library classloaders.";
        }
        if (error instanceof UnsupportedOperationException) {
            if ("CRAFTBUKKIT".equals(surface) || "PAPER".equals(surface)) {
                return "CraftBukkit/Paper API reached an unsupported LunarArc implementation. Verify Paper 1.21.1 semantics and implement it if upstream supports it.";
            }
            return "Verify whether Paper 1.21.1 supports this API; if it does, replace the LunarArc unsupported path with a concrete implementation.";
        }
        if (error instanceof LinkageError) {
            return "Inspect the referenced NMS owner/member against loader-owned 1.21.1 bytecode before adding any remap rule.";
        }
        return "Follow the first plugin-owned frame and the root cause before changing remapping or adding a bridge.";
    }

    private static String stackTrace(Throwable error) {
        StringWriter out = new StringWriter();
        error.printStackTrace(new PrintWriter(out));
        return out.toString();
    }

    private static String pluginName(PluginDescriptionFile description) {
        return description == null ? "<unknown>" : safe(description.getFullName());
    }

    private static String safe(Object value) {
        return value == null ? "<none>" : String.valueOf(value).replace('\n', ' ').replace('\r', ' ');
    }
}
