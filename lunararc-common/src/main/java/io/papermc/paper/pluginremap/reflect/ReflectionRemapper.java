package io.papermc.paper.pluginremap.reflect;

import io.papermc.paper.util.MappingEnvironment;
import org.objectweb.asm.ClassVisitor;

public final class ReflectionRemapper {

    private ReflectionRemapper() {
    }

    /** Returns {@code parent} unchanged unless the runtime is reobfuscated, which it never is here. */
    public static ClassVisitor visitor(ClassVisitor parent) {
        return parent;
    }

    /** Returns {@code bytecode} unchanged; see the class javadoc for why there is nothing to do. */
    public static byte[] processClass(byte[] bytecode) {
        return bytecode;
    }

    /** Whether this remapper would do anything on the current runtime. */
    public static boolean enabled() {
        return !MappingEnvironment.DISABLE_PLUGIN_REMAPPING && MappingEnvironment.reobf();
    }
}
