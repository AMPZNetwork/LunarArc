package io.lunararcdevs.lunararc.common.mod.util.remapper.patcher.integrated;

import io.lunararcdevs.lunararc.common.mod.util.remapper.patcher.PluginPatcher;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class LunarArcIntegratedPatcher implements PluginPatcher {

    private static final Map<String, BiConsumer<ClassNode, ClassRepo>> SPECIFIC = new HashMap<>();

    // See LunarArcWorldEditRegenLevel for why this exists: WorldEdit's PaperweightAdapter builds
    // a throwaway ServerLevel for //regen through a Paper-only constructor overload this platform
    // doesn't have and Mixin can't add. Retargeting the NEW/INVOKESPECIAL pair to our subclass at
    // plugin-load time keeps WorldEdit's own bytecode otherwise untouched.
    private static final String WORLDEDIT_REGEN_ADAPTER = "com/sk89q/worldedit/bukkit/adapter/impl/v1_21/PaperweightAdapter";
    private static final String SERVER_LEVEL = "net/minecraft/server/level/ServerLevel";
    private static final String REGEN_LEVEL = "io/lunararcdevs/lunararc/common/compat/worldedit/LunarArcWorldEditRegenLevel";

    // WorldEdit's own NoOpWorldLoadListener was compiled against Paper's ChunkProgressListener,
    // where onStatusChange(ChunkPos, ChunkStatus) has a default no-op body - it never overrides
    // it. Vanilla's interface keeps that method abstract, so calling it here throws
    // AbstractMethodError from a background chunk-generation task thread, killing that task and
    // wedging //regen's chunk queue forever (confirmed live: reproduces identically with Sable
    // removed, ruling that mod out as the cause of the hang entirely). Redirect construction to
    // our own listener that actually implements all four methods.
    private static final String WORLDEDIT_NOOP_LISTENER =
            WORLDEDIT_REGEN_ADAPTER + "$NoOpWorldLoadListener";
    private static final String LUNARARC_NOOP_LISTENER =
            "io/lunararcdevs/lunararc/common/compat/worldedit/LunarArcNoOpChunkProgressListener";

    private static final String REGEN_CTOR_DESC =
            "(Lnet/minecraft/server/MinecraftServer;"
                    + "Ljava/util/concurrent/Executor;"
                    + "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;"
                    + "Lnet/minecraft/world/level/storage/PrimaryLevelData;"
                    + "Lnet/minecraft/resources/ResourceKey;"
                    + "Lnet/minecraft/world/level/dimension/LevelStem;"
                    + "Lnet/minecraft/server/level/progress/ChunkProgressListener;"
                    + "ZJLjava/util/List;ZLnet/minecraft/world/RandomSequences;"
                    + "Lorg/bukkit/World$Environment;"
                    + "Lorg/bukkit/generator/ChunkGenerator;"
                    + "Lorg/bukkit/generator/BiomeProvider;)V";

    // WorldEdit's cleanup calls chunkSource.close(false) - a Paper-only overload of
    // ServerChunkCache.close() that takes a "save" flag. Vanilla only has the plain no-arg
    // close() (which this platform's chunk source already is), so this throws NoSuchMethodError
    // right after a successful regen, in the finally block that's supposed to tear down the
    // throwaway level - the actual block copy already succeeded by then, but WorldEdit reports
    // the command as failed anyway since the exception propagates out of doRegen(). Strip the
    // boolean argument and retarget the call to the real no-arg close().
    private static final String CLOSE_NAME = "close";
    private static final String CLOSE_WITH_SAVE_DESC = "(Z)V";
    private static final String CLOSE_NO_ARG_DESC = "()V";

    static {
        SPECIFIC.put(WORLDEDIT_REGEN_ADAPTER, LunarArcIntegratedPatcher::patchWorldEditRegen);
    }

    @Override
    public void handleClass(ClassNode node, ClassRepo classRepo) {
        BiConsumer<ClassNode, ClassRepo> consumer = SPECIFIC.get(node.name);
        if (consumer != null) {
            consumer.accept(node, classRepo);
        }
    }

    @Override
    public String version() {
        return "LunarArc integrated patcher (WorldEdit //regen bridge)";
    }

    private static void patchWorldEditRegen(ClassNode node, ClassRepo classRepo) {
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn : method.instructions) {
                if (!(insn instanceof MethodInsnNode call)) continue;

                if (call.getOpcode() == Opcodes.INVOKESPECIAL && "<init>".equals(call.name)) {
                    String replacement;
                    if (SERVER_LEVEL.equals(call.owner) && REGEN_CTOR_DESC.equals(call.desc)) {
                        replacement = REGEN_LEVEL;
                    } else if (WORLDEDIT_NOOP_LISTENER.equals(call.owner) && "()V".equals(call.desc)) {
                        replacement = LUNARARC_NOOP_LISTENER;
                    } else {
                        continue;
                    }

                    TypeInsnNode newInsn = findMatchingNew(call);
                    if (newInsn == null) continue;
                    newInsn.desc = replacement;
                    call.owner = replacement;
                    continue;
                }

                if ((call.getOpcode() == Opcodes.INVOKEVIRTUAL || call.getOpcode() == Opcodes.INVOKEINTERFACE)
                        && CLOSE_NAME.equals(call.name) && CLOSE_WITH_SAVE_DESC.equals(call.desc)) {
                    AbstractInsnNode argPush = previousReal(call);
                    if (argPush == null || (argPush.getOpcode() != Opcodes.ICONST_0 && argPush.getOpcode() != Opcodes.ICONST_1)) {
                        continue;
                    }
                    method.instructions.remove(argPush);
                    call.desc = CLOSE_NO_ARG_DESC;
                }
            }
        }
    }

    private static AbstractInsnNode previousReal(AbstractInsnNode insn) {
        AbstractInsnNode current = insn.getPrevious();
        while (current != null && current.getOpcode() < 0) current = current.getPrevious();
        return current;
    }

    /**
     * Walks backward from a constructor call to the NEW instruction that allocated its receiver,
     * treating every other complete NEW/INVOKESPECIAL<init> pair encountered along the way as a
     * balanced, already-closed argument expression (e.g. a nested {@code new LevelStem(...)}) to
     * skip over rather than mistake for our own - simple bracket-style matching, valid because
     * javac never emits an unmatched NEW.
     */
    private static TypeInsnNode findMatchingNew(MethodInsnNode ctorCall) {
        int pending = 0;
        for (AbstractInsnNode current = ctorCall.getPrevious(); current != null; current = current.getPrevious()) {
            if (current instanceof MethodInsnNode m && current.getOpcode() == Opcodes.INVOKESPECIAL
                    && "<init>".equals(m.name)) {
                pending++;
            } else if (current instanceof TypeInsnNode t && current.getOpcode() == Opcodes.NEW) {
                if (pending == 0) return t;
                pending--;
            }
        }
        return null;
    }
}
