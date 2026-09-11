package org.bukkit.craftbukkit.block;

import io.lunararcdevs.lunararc.common.server.LunarArcRegistryAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;

public final class CraftBiome {
    private CraftBiome() {}

    public static Biome minecraftHolderToBukkit(Holder<net.minecraft.world.level.biome.Biome> holder) {
        var server = io.lunararcdevs.lunararc.common.mod.server.LunarArcServer.minecraftServer();
        if (server == null) throw new IllegalStateException("MinecraftServer has not been attached to LunarArc yet");
        var registry = server.registryAccess().registryOrThrow(Registries.BIOME);
        ResourceLocation id = holder.unwrapKey().map(net.minecraft.resources.ResourceKey::location)
                .orElseGet(() -> registry.getKey(holder.value()));
        if (id == null) throw new IllegalArgumentException("Unregistered NMS biome " + holder.value());
        Biome biome = LunarArcRegistryAccess.INSTANCE.getRegistry(Biome.class)
                .get(new NamespacedKey(id.getNamespace(), id.getPath()));
        if (biome == null) throw new IllegalArgumentException("No Bukkit biome for " + id);
        return biome;
    }

    public static Holder<net.minecraft.world.level.biome.Biome> bukkitToMinecraftHolder(Biome biome) {
        if (biome == null) throw new IllegalArgumentException("biome cannot be null");
        var server = io.lunararcdevs.lunararc.common.mod.server.LunarArcServer.minecraftServer();
        if (server == null) throw new IllegalStateException("MinecraftServer has not been attached to LunarArc yet");
        var registry = server.registryAccess().registryOrThrow(Registries.BIOME);
        NamespacedKey key = biome.getKey();
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey());
        return registry.getHolder(net.minecraft.resources.ResourceKey.create(Registries.BIOME, id))
                .orElseThrow(() -> new IllegalArgumentException("Unknown biome " + key));
    }

    public static Biome minecraftToBukkit(net.minecraft.world.level.biome.Biome minecraft) {
        if (minecraft == null) throw new IllegalArgumentException("minecraft cannot be null");
        var server = io.lunararcdevs.lunararc.common.mod.server.LunarArcServer.minecraftServer();
        if (server == null) throw new IllegalStateException("MinecraftServer has not been attached to LunarArc yet");
        ResourceLocation id = server.registryAccess().registryOrThrow(Registries.BIOME).getKey(minecraft);
        if (id == null) return Biome.CUSTOM;
        Biome biome = LunarArcRegistryAccess.INSTANCE.getRegistry(Biome.class)
                .get(new NamespacedKey(id.getNamespace(), id.getPath()));
        return biome == null ? Biome.CUSTOM : biome;
    }

    public static net.minecraft.world.level.biome.Biome bukkitToMinecraft(Biome bukkit) {
        if (bukkit == null || bukkit == Biome.CUSTOM) return null;
        return bukkitToMinecraftHolder(bukkit).value();
    }
}
