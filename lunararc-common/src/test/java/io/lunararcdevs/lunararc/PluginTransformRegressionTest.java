package io.lunararcdevs.lunararc;

import io.lunararcdevs.lunararc.common.mod.LunarArcRemapper;
import io.lunararcdevs.lunararc.common.server.LunarArcPluginFixManager;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public final class PluginTransformRegressionTest {
    public static class Connection {
        public void send(String packet) {}
    }

    public static class Listener extends Connection {
        public int protocol() { return 0; }
    }

    public static void run() throws Exception {
        for (boolean isStatic : new boolean[]{true, false}) {
            String name = "com.Acrobot.ChestShop.Listeners.Block.BlockPlace";
            var writer = new ClassWriter(0);
            writer.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, name.replace('.', '/'), null, "java/lang/Object", null);
            var method = writer.visitMethod(Opcodes.ACC_PUBLIC | (isStatic ? Opcodes.ACC_STATIC : 0),
                    "onHopperDropperPlace", "(Lorg/bukkit/event/block/BlockPlaceEvent;)V", null, null);
            method.visitCode();
            method.visitInsn(Opcodes.RETURN);
            method.visitMaxs(0, isStatic ? 1 : 2);
            method.visitEnd();
            writer.visitEnd();
            byte[] patched = LunarArcPluginFixManager.injectPluginFix(name, writer.toByteArray());
            Class<?> type = new ClassLoader(PluginTransformRegressionTest.class.getClassLoader()) {
                Class<?> define() { return defineClass(name, patched, 0, patched.length); }
            }.define();
            type.getDeclaredMethods();
        }
        var field = LunarArcRemapper.class.getDeclaredField("METHOD_NAME_MAP");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        var names = (java.util.Map<Object, String>) field.get(null);
        Class<?> keyClass = Class.forName(LunarArcRemapper.class.getName() + "$MemberNameKey");
        var constructor = keyClass.getDeclaredConstructor(String.class, String.class);
        constructor.setAccessible(true);
        Object child = constructor.newInstance(Listener.class.getName().replace('.', '/'), "regressionAlias");
        Object parent = constructor.newInstance(Connection.class.getName().replace('.', '/'), "regressionAlias");
        names.put(child, "protocol");
        names.put(parent, "send");
        try {
            String resolved = new LunarArcRemapper().mapRuntimeMethodName(Listener.class, "regressionAlias", new Class<?>[]{String.class});
            if (!resolved.equals("send")) throw new AssertionError("wrong overload hid inherited packet method: " + resolved);
        } finally {
            names.remove(child);
            names.remove(parent);
        }
        String packetMethod = new LunarArcRemapper().mapRuntimeMethodName(
                net.minecraft.server.network.ServerGamePacketListenerImpl.class, "b",
                new Class<?>[]{net.minecraft.network.protocol.Packet.class});
        if (!packetMethod.equals("send")) throw new AssertionError("1.21.1 packet-send mapping: " + packetMethod);
        var registry = io.lunararcdevs.lunararc.common.mod.LunarArcReflectionBridge.getMethod(
                net.minecraft.server.dedicated.DedicatedServer.class, "getDefaultRegistryAccess", new Class<?>[0]);
        if (!registry.getName().equals("getMinecraftRegistry") || registry.getReturnType() != net.minecraft.core.RegistryAccess.class) {
            throw new AssertionError("registry compatibility method mismatch");
        }
        org.bukkit.inventory.meta.LeatherArmorMeta leather = new org.bukkit.craftbukkit.inventory.CraftMetaColorableArmor(null);
        leather.setColor(org.bukkit.Color.BLUE);
        if (!leather.isDyed() || !leather.clone().getColor().equals(org.bukkit.Color.BLUE)) throw new AssertionError("leather color lost");
        leather.setColor(null);
        if (leather.isDyed() || leather.getColor().asRGB() != 0xA06540) throw new AssertionError("leather color reset failed");
        System.out.println("Plugin frame and reflection regressions passed");
    }
}
