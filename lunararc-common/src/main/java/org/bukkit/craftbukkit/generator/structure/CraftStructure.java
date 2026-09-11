package org.bukkit.craftbukkit.generator.structure;

import org.bukkit.NamespacedKey;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructureType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class CraftStructure extends Structure {
    private final NamespacedKey key;
    private final net.minecraft.world.level.levelgen.structure.Structure handle;
    private final StructureType type;

    public CraftStructure(@NotNull NamespacedKey key,
                          @NotNull net.minecraft.world.level.levelgen.structure.Structure handle,
                          @NotNull StructureType type) {
        this.key = Objects.requireNonNull(key, "key");
        this.handle = Objects.requireNonNull(handle, "handle");
        this.type = Objects.requireNonNull(type, "type");
    }


    public static @NotNull Structure minecraftToBukkit(@NotNull net.minecraft.world.level.levelgen.structure.Structure minecraft) {
        java.util.Objects.requireNonNull(minecraft, "minecraft");
        net.minecraft.server.MinecraftServer server = ((org.bukkit.craftbukkit.CraftServer) org.bukkit.Bukkit.getServer()).getServer();
        if (server == null) throw new IllegalStateException("MinecraftServer is not initialized");
        net.minecraft.resources.ResourceLocation id = server.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(minecraft);
        if (id == null) throw new IllegalArgumentException("Unregistered Minecraft structure " + minecraft);
        Structure result = org.bukkit.Registry.STRUCTURE.get(
                new NamespacedKey(id.getNamespace(), id.getPath()));
        if (result == null) throw new IllegalStateException("No Bukkit structure wrapper for " + id);
        return result;
    }

    public static @NotNull net.minecraft.world.level.levelgen.structure.Structure bukkitToMinecraft(@NotNull Structure bukkit) {
        java.util.Objects.requireNonNull(bukkit, "bukkit");
        if (bukkit instanceof CraftStructure craft) return craft.getHandle();
        net.minecraft.server.MinecraftServer server = ((org.bukkit.craftbukkit.CraftServer) org.bukkit.Bukkit.getServer()).getServer();
        if (server == null) throw new IllegalStateException("MinecraftServer is not initialized");
        net.minecraft.resources.ResourceLocation id = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                bukkit.getKey().getNamespace(), bukkit.getKey().getKey());
        net.minecraft.world.level.levelgen.structure.Structure result = server.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .get(id);
        if (result == null) throw new IllegalArgumentException("Unregistered Bukkit structure " + bukkit.getKey());
        return result;
    }

    public @NotNull net.minecraft.world.level.levelgen.structure.Structure getHandle() { return this.handle; }
    @Override public @NotNull StructureType getStructureType() { return this.type; }
    @Override public @NotNull NamespacedKey getKey() { return this.key; }
    @Override public boolean equals(Object other) {
        return this == other || (other instanceof Structure structure && this.key.equals(structure.getKey()));
    }
    @Override public int hashCode() { return this.key.hashCode(); }
    @Override public String toString() { return "CraftStructure[" + this.key + "]"; }
}
