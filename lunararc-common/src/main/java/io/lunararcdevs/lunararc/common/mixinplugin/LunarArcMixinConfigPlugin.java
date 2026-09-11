package io.lunararcdevs.lunararc.common.mixinplugin;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class LunarArcMixinConfigPlugin implements IMixinConfigPlugin {

    private static final String MINECRAFT_SERVER = "net/minecraft/server/MinecraftServer";
    private static final String SERVER_ACCESS =
            "io/lunararcdevs/lunararc/common/LunarArcServerAccess";
    private static final String GET_SERVER_DESC = "()L" + MINECRAFT_SERVER + ";";

    private static final String PLAYER_INFO_PACKET =
            "net/minecraft/network/protocol/game/ClientboundPlayerInfoUpdatePacket";
    private static final String PLAYER_INFO_ENTRY = PLAYER_INFO_PACKET + "$Entry";
    // The offline dev-reference jar (minecraft-merged-srg-patched.jar) uses SRG names
    // ("f_244436_"), but a live boot proved the actual runtime class this hook receives keeps
    // the real Mojang name "entries" - confirmed via a real NoSuchFieldError when f_244436_
    // was used here ("does not have member field 'java.util.List f_244436_'").
    private static final String ENTRIES_FIELD = "entries";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // Nothing here may throw. This runs inside mixin application, where an escaping exception
        // is not a failed feature but a server that never starts.
        try {
            if (MINECRAFT_SERVER.equals(targetClass.name)) {
                addStaticGetServer(targetClass);
            } else if (PLAYER_INFO_PACKET.equals(targetClass.name)) {
                addSingleEntryConstructor(targetClass);
            }
        } catch (Throwable ignored) {
            // A missing accessor is a plugin-compatibility gap; a thrown one is a dead server.
        }
    }

    private static void addSingleEntryConstructor(ClassNode targetClass) {
        String desc = "(Ljava/util/EnumSet;L" + PLAYER_INFO_ENTRY + ";)V";
        for (MethodNode existing : targetClass.methods) {
            if ("<init>".equals(existing.name) && desc.equals(existing.desc)) return;
        }

        MethodNode ctor = new MethodNode(Opcodes.ASM9, Opcodes.ACC_PUBLIC, "<init>", desc, null, null);
        ctor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        ctor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        ctor.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC, "java/util/Collections", "emptyList", "()Ljava/util/List;", false));
        ctor.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESPECIAL, PLAYER_INFO_PACKET, "<init>",
                "(Ljava/util/EnumSet;Ljava/util/Collection;)V", false));
        ctor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        ctor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        ctor.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC, "java/util/List", "of", "(Ljava/lang/Object;)Ljava/util/List;", true));
        ctor.instructions.add(new FieldInsnNode(
                Opcodes.PUTFIELD, PLAYER_INFO_PACKET, ENTRIES_FIELD, "Ljava/util/List;"));
        ctor.instructions.add(new InsnNode(Opcodes.RETURN));
        ctor.maxStack = 3;
        ctor.maxLocals = 3;
        targetClass.methods.add(ctor);
    }

    /**
     * Declares {@code public static MinecraftServer getServer()}, returning LunarArc's attached
     * server instance - the same thing CraftBukkit's own static returns.
     */
    private static void addStaticGetServer(ClassNode targetClass) {
        // MinecraftServer carries several mixins, so postApply runs more than once for it.
        for (MethodNode existing : targetClass.methods) {
            if ("getServer".equals(existing.name) && GET_SERVER_DESC.equals(existing.desc)) return;
        }

        MethodNode getServer = new MethodNode(
                Opcodes.ASM9,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "getServer",
                GET_SERVER_DESC,
                null,
                null);
        getServer.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC, SERVER_ACCESS, "getMinecraftServer", GET_SERVER_DESC, false));
        getServer.instructions.add(new InsnNode(Opcodes.ARETURN));
        getServer.maxStack = 1;
        getServer.maxLocals = 0;
        targetClass.methods.add(getServer);
    }

}
