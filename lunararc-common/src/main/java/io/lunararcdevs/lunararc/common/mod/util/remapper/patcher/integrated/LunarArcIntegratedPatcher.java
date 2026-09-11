package io.lunararcdevs.lunararc.common.mod.util.remapper.patcher.integrated;

import io.lunararcdevs.lunararc.common.mod.util.remapper.patcher.PluginPatcher;
import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class LunarArcIntegratedPatcher implements PluginPatcher {

    private static final Map<String, BiConsumer<ClassNode, ClassRepo>> SPECIFIC = new HashMap<>();

    @Override
    public void handleClass(ClassNode node, ClassRepo classRepo) {
        BiConsumer<ClassNode, ClassRepo> consumer = SPECIFIC.get(node.name);
        if (consumer != null) {
            consumer.accept(node, classRepo);
        }
    }

    @Override
    public String version() {
        return "LunarArc integrated patcher (no entries registered yet)";
    }
}
