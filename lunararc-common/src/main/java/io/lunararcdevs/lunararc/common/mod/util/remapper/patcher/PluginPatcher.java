package io.lunararcdevs.lunararc.common.mod.util.remapper.patcher;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

public interface PluginPatcher {

    void handleClass(ClassNode node, ClassRepo classRepo);

    /** Priority of this patcher instance. Lower priority runs first. */
    default int priority() {
        return 0;
    }

    default String version() {
        String implVersion = getClass().getPackage().getImplementationVersion();
        return implVersion == null ? "unknown" : implVersion;
    }

    interface ClassRepo {

        /**
         * Find a class's bytecode without running the plugin transformer on it — i.e. this
         * must never itself invoke LunarArcRemapper, or a patcher looking up a related class
         * would recurse back into transformation.
         *
         * @param internalName   internal form of the class name (e.g. "com/example/Foo")
         * @param parsingOptions {@link org.objectweb.asm.ClassReader#accept(ClassVisitor, int)}
         * @return class node, or null if nothing is found
         */
        ClassNode findClass(String internalName, int parsingOptions);
    }
}
