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