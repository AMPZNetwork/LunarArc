package io.lunararcdevs.lunararc.common.mod.util.remapper.patcher;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.MixinService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class LunarArcGlobalClassRepo implements PluginPatcher.ClassRepo {

    public static final LunarArcGlobalClassRepo INSTANCE = new LunarArcGlobalClassRepo();

    private final ConcurrentMap<String, ClassNode> cache = new ConcurrentHashMap<>();

    private LunarArcGlobalClassRepo() {}

    @Override
    public ClassNode findClass(String internalName, int parsingOptions) {
        if (parsingOptions == ClassReader.SKIP_CODE) {
            return this.cache.computeIfAbsent(internalName, this::findMinecraft);
        }
        // A caller asking for full bytecode (method bodies included) wants a fresh read,
        // not the SKIP_CODE-shaped entry the plain lookup above would cache.
        return findMinecraft(internalName);
    }

    private ClassNode findMinecraft(String internalName) {
        try {
            // Verified against the real Arclight GlobalClassRepo: Mixin's bytecode provider
            // returns a ClassNode directly (pre-transform), not raw bytes.
            return MixinService.getService().getBytecodeProvider().getClassNode(internalName);
        } catch (Exception e) {
            return null;
        }
    }
}
