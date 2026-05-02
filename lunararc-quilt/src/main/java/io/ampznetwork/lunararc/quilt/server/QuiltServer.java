package io.ampznetwork.lunararc.quilt.server;

import io.ampznetwork.lunararc.common.server.LunarArcServer;
import net.minecraft.server.MinecraftServer;

public class QuiltServer extends LunarArcServer {
    static {
        io.ampznetwork.lunararc.common.mod.util.VelocitySupport.setPacketProvider(buf -> new net.minecraft.network.protocol.login.custom.CustomQueryPayload() {
            @Override public net.minecraft.resources.ResourceLocation id() { return io.ampznetwork.lunararc.common.mod.util.VelocitySupport.PLAYER_INFO_CHANNEL; }
            @Override public void write(net.minecraft.network.FriendlyByteBuf b) { b.writeBytes(buf.slice()); }
        });
    }

    public QuiltServer(MinecraftServer server) {
        super(server);
    }

    @Override
    public String getName() {
        return "LunarArc-Quilt";
    }
}
