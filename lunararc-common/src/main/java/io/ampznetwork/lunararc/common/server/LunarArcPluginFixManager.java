package io.ampznetwork.lunararc.common.server;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public final class LunarArcPluginFixManager {

    private static final String REPLACEMENT = "lunararc";

    private LunarArcPluginFixManager() {}

    /**
     * Applies the per-plugin settings that have to be in place before a plugin's first class is
     * even loaded, keyed on the plugin's name rather than on any one class.
     *
     * <p>Called from the plugin's classloader constructor rather than from
     * {@link #injectPluginFix(String, byte[])}. MohistMC/Youer does this from inside its
     * class-transform hook, which works there because Youer transforms every class on every
     * start; LunarArc caches transformed classes on disk, so on the second and every later start
     * the transform is skipped entirely and a side effect living inside it would simply never
     * happen. Anchoring it to classloader construction makes it happen exactly once per plugin
     * per start, cache or no cache.</p>
     */
    public static void applyPluginProperties(String pluginName) {
        if (pluginName == null) return;

        if (pluginName.equals("WorldEdit")) {
            if (System.getProperty("worldedit.bukkit.adapter") == null) {
                System.setProperty("worldedit.bukkit.adapter",
                        "com.sk89q.worldedit.bukkit.adapter.impl.v1_21.PaperweightAdapter");
            }
        }
    }

    public static byte[] injectPluginFix(String className, byte[] clazz) {
        Consumer<ClassNode> patcher = switch (className) {
            case "com.sk89q.worldedit.util.translation.TranslationManager" ->
                    LunarArcPluginFixManager::fixEmptyWorldEditTranslations;
            case "com.sk89q.worldedit.bukkit.adapter.impl.v1_21.PaperweightAdapter",
                 "com.sk89q.worldedit.bukkit.adapter.ext.fawe.v1_21_R1.PaperweightAdapter" ->
                    node -> helloWorld(node, "org.spigotmc.WatchdogThread", REPLACEMENT);
            case "com.sk89q.worldedit.bukkit.paperlib.PaperLib" -> node -> {
                removePaper0(node);
                if (System.getProperty("paperlib.shown-benefits") == null) {
                    System.setProperty("paperlib.shown-benefits", "1");
                }
            };
            case "org.mvplugins.multiverse.external.paperlib.PaperLib",
                 "me.SuperRonanCraft.BetterRTP.lib.paperlib.PaperLib",
                 "com.plotsquared.bukkit.paperlib.PaperLib" ->
                    LunarArcPluginFixManager::removePaper0;
            case "com.fastasyncworldedit.bukkit.util.MinecraftVersion" ->
                    node -> redirectMethodToGetNMSVersion(node, "getPackageVersion");
            case "com.ghostchu.quickshop.platform.spigot.AbstractSpigotPlatform" ->
                    node -> redirectMethodToGetNMSVersion(node, "getNMSVersion");
            case "com.earth2me.essentials.items.FlatItemDb" ->
                    LunarArcPluginFixManager::fixEssentialsModdedMaterials;
                case "com.sk89q.worldguard.bukkit.util.Materials" ->
                    LunarArcPluginFixManager::fixWorldGuardMaterials;
                case "com.Acrobot.ChestShop.Listeners.Block.BlockPlace" ->
                    LunarArcPluginFixManager::guardChestShopMaterialSwitch;
            default -> null;
        };
        return patcher == null ? clazz : patch(clazz, patcher);
    }

    private static void fixEmptyWorldEditTranslations(ClassNode node) {
        for (MethodNode method : node.methods) {
            if (!method.name.equals("putTranslationData")
                    || !method.desc.equals("(Ljava/util/Map;Ljava/io/InputStream;)V")) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode call)
                        || !call.owner.equals("java/util/Map") || !call.name.equals("entrySet")
                        || !call.desc.equals("()Ljava/util/Set;")) continue;
                method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        Type.getInternalName(LunarArcPluginFixManager.class), "translationEntries",
                        "(Ljava/util/Map;)Ljava/util/Set;", false));
            }
        }
    }

    public static java.util.Set<Map.Entry<String, String>> translationEntries(Map<String, String> translations) {
        return translations == null ? java.util.Set.of() : translations.entrySet();
    }

    private static void fixEssentialsModdedMaterials(ClassNode node) {
        for (MethodNode method : node.methods) {
            if (!method.name.equals("get")
                    || !method.desc.equals("(Ljava/lang/String;Z)Lorg/bukkit/inventory/ItemStack;")) {
                continue;
            }

            InsnList normalizeName = new InsnList();
            normalizeName.add(new VarInsnNode(Opcodes.ALOAD, 1));
            normalizeName.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    Type.getInternalName(LunarArcPluginFixManager.class),
                    "normalizeEssentialsItemName",
                    "(Ljava/lang/String;)Ljava/lang/String;",
                    false));
            normalizeName.add(new VarInsnNode(Opcodes.ASTORE, 1));
            method.instructions.insert(normalizeName);

            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode call)) continue;
                if (call.getOpcode() != Opcodes.INVOKEVIRTUAL
                        || !call.name.equals("getMaterial")
                        || !call.desc.equals("()Lorg/bukkit/Material;")) {
                    continue;
                }

                InsnList replacement = new InsnList();
                replacement.add(new VarInsnNode(Opcodes.ALOAD, 1));
                replacement.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        Type.getInternalName(LunarArcPluginFixManager.class),
                        "resolveEssentialsMaterial",
                        "(Lorg/bukkit/Material;Ljava/lang/String;)Lorg/bukkit/Material;",
                        false));
                method.instructions.insert(call, replacement);
                method.maxStack = Math.max(method.maxStack, 2);
                return;
            }
        }
    }

    private static void fixWorldGuardMaterials(ClassNode node) {
        for (MethodNode method : node.methods) {
            if (!method.name.equals("getEntitySpawnEgg")
                    || !method.desc.equals("(Lorg/bukkit/Material;)Lorg/bukkit/entity/EntityType;")) {
                continue;
            }

            InsnList replacement = new InsnList();
            replacement.add(new VarInsnNode(Opcodes.ALOAD, 0));
            replacement.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    Type.getInternalName(LunarArcPluginFixManager.class),
                    "worldGuardSpawnEgg",
                    "(Lorg/bukkit/Material;)Lorg/bukkit/entity/EntityType;",
                    false));
            replacement.add(new InsnNode(Opcodes.ARETURN));
            method.instructions = replacement;
            method.tryCatchBlocks.clear();
            return;
        }
    }

    private static void guardChestShopMaterialSwitch(ClassNode node) {
        for (MethodNode method : node.methods) {
            if (!method.name.equals("onHopperDropperPlace")
                    || !method.desc.equals("(Lorg/bukkit/event/block/BlockPlaceEvent;)V")) {
                continue;
            }

            InsnList guard = new InsnList();
            org.objectweb.asm.tree.LabelNode vanillaMaterial = new org.objectweb.asm.tree.LabelNode();
            guard.add(new VarInsnNode(Opcodes.ALOAD, (method.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1));
            guard.add(new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "org/bukkit/event/block/BlockPlaceEvent",
                    "getBlockPlaced",
                    "()Lorg/bukkit/block/Block;",
                    false));
            guard.add(new MethodInsnNode(
                    Opcodes.INVOKEINTERFACE,
                    "org/bukkit/block/Block",
                    "getType",
                    "()Lorg/bukkit/Material;",
                    true));
            guard.add(new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "org/bukkit/Material",
                    "getKey",
                    "()Lorg/bukkit/NamespacedKey;",
                    false));
            guard.add(new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "org/bukkit/NamespacedKey",
                    "getNamespace",
                    "()Ljava/lang/String;",
                    false));
            guard.add(new LdcInsnNode("minecraft"));
            guard.add(new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/String",
                    "equals",
                    "(Ljava/lang/Object;)Z",
                    false));
            guard.add(new org.objectweb.asm.tree.JumpInsnNode(Opcodes.IFNE, vanillaMaterial));
            guard.add(new InsnNode(Opcodes.RETURN));
            guard.add(vanillaMaterial);
            guard.add(new org.objectweb.asm.tree.FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            method.instructions.insert(guard);
            method.maxStack = Math.max(method.maxStack, 2);
            return;
        }
    }

    public static org.bukkit.entity.EntityType worldGuardSpawnEgg(org.bukkit.Material material) {
        if (material == null || material.getKey() == null
                || !"minecraft".equals(material.getKey().getNamespace())) {
            return null;
        }
        String name = material.name();
        if (!name.endsWith("_SPAWN_EGG")) return null;
        return org.bukkit.entity.EntityType.fromName(name.substring(0, name.length() - "_SPAWN_EGG".length()));
    }

    public static String normalizeEssentialsItemName(String itemName) {
        if (itemName == null) return null;

        String requested = itemName.trim().toLowerCase(Locale.ROOT);
        if (requested.startsWith("minecraft:") || requested.indexOf(':') <= 0) return itemName;

        net.minecraft.resources.ResourceLocation id =
                net.minecraft.resources.ResourceLocation.tryParse(requested);
        if (id == null || LunarArcDynamicBukkitEnums.material(id) == null) return itemName;

        return (id.getNamespace() + "_" + id.getPath()).toLowerCase(Locale.ROOT);
    }

    public static org.bukkit.Material resolveEssentialsMaterial(org.bukkit.Material material, String itemName) {
        if (material != null || itemName == null) return material;

        String requested = itemName.trim().toLowerCase(Locale.ROOT);
        if (requested.isEmpty()) return null;
        return LunarArcEssentialsItemBridge.resolveAlias(requested);
    }

    private static void removePaper0(ClassNode node) {
        helloWorld(node, "com.destroystokyo.paper.PaperConfig", REPLACEMENT);
        helloWorld(node, "io.papermc.paper.configuration.Configuration", REPLACEMENT);
    }

    private static void redirectMethodToGetNMSVersion(ClassNode node, String methodName) {
        for (MethodNode methodNode : node.methods) {
            if (methodNode.name.equals(methodName) && methodNode.desc.equals("()Ljava/lang/String;")) {
                InsnList toInject = new InsnList();
                toInject.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        Type.getInternalName(LunarArcPluginFixManager.class),
                        "getNMSVersion",
                        "()Ljava/lang/String;"));
                toInject.add(new InsnNode(Opcodes.ARETURN));
                methodNode.instructions = toInject;
                methodNode.tryCatchBlocks.clear();
            }
        }
    }

    public static String getNMSVersion() {
        return "v1_21_R1";
    }

    private static byte[] patch(byte[] basicClass, Consumer<ClassNode> handler) {
        org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(basicClass);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        handler.accept(node);
        org.objectweb.asm.ClassWriter writer = new org.objectweb.asm.ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static void helloWorld(ClassNode node, String a, String b) {
        node.methods.forEach(method -> {
            for (AbstractInsnNode next : method.instructions) {
                if (next instanceof LdcInsnNode ldcInsnNode) {
                    if (ldcInsnNode.cst instanceof String str) {
                        if (a.equals(str)) {
                            ldcInsnNode.cst = b;
                        }
                    }
                }
            }
        });
    }
}
