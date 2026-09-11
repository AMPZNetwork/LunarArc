package io.lunararcdevs.lunararc.common.network;

import io.lunararcdevs.lunararc.common.LunarArcServerAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.bukkit.entity.Player;

/**
 * Single threading boundary for inbound Bukkit plugin messages.
 * Native loader transports decode/own their packets; Bukkit dispatch is always
 * transferred to the Minecraft server thread before plugin code is invoked.
 */
public final class LunarArcPluginMessageDispatcher {
    private LunarArcPluginMessageDispatcher() {}

    public static void dispatch(MinecraftServer server, Player player, ResourceLocation channel, byte[] data) {
        dispatch(server, player, channel.toString(), data);
    }

    public static void dispatch(MinecraftServer server, Player player, String channel, byte[] data) {
        byte[] stableData = data.clone();
        Runnable task = () -> LunarArcServerAccess.getCraftServer(server)
                .getMessenger()
                .dispatchIncomingMessage(player, channel, stableData);
        if (server.isSameThread()) {
            task.run();
        } else {
            server.execute(task);
        }
    }
}
