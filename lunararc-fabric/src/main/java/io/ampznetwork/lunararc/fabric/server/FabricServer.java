package io.ampznetwork.lunararc.fabric.server;

import io.ampznetwork.lunararc.common.server.LunarArcServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.loot.LootTable;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FabricServer extends LunarArcServer {
    static {
        io.ampznetwork.lunararc.common.mod.util.VelocitySupport.setPacketProvider(buf -> new net.minecraft.network.protocol.login.custom.CustomQueryPayload() {
            @Override public net.minecraft.resources.ResourceLocation id() { return io.ampznetwork.lunararc.common.mod.util.VelocitySupport.PLAYER_INFO_CHANNEL; }
            @Override public void write(net.minecraft.network.FriendlyByteBuf b) { b.writeBytes(buf.slice()); }
        });
    }

    public FabricServer(MinecraftServer server) {
        super(server);
    }

    @Override
    public String getName() {
        return "LunarArc-Fabric";
    }

    @Override
    public @Nullable LootTable getLootTable(@NotNull NamespacedKey key) {
        // Implementation using NMS
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey());
        return null;
    }

    @Override
    public @NotNull BlockData createBlockData(@NotNull Material material, @Nullable String data) throws IllegalArgumentException {
        return null;
    }
}