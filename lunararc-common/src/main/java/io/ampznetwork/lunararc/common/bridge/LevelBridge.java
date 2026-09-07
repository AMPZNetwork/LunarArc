package io.ampznetwork.lunararc.common.bridge;

/** Exposes {@code LevelMixin}'s injected methods to plain Java source files that hold a real
 * {@code net.minecraft.world.level.Level} reference — Mixin-injected methods aren't visible to
 * other compilation units at compile time unless surfaced through a real interface like this one. */
public interface LevelBridge {
    org.bukkit.craftbukkit.CraftWorld lunararc$getWorld();
    io.papermc.paper.configuration.WorldConfiguration lunararc$getPaperConfiguration();

    /** Binds the Bukkit world to {@code Level.world}; see LevelMixin for why that field exists. */
    void lunararc$attachBukkitWorld(org.bukkit.craftbukkit.CraftWorld world);
    org.bukkit.craftbukkit.CraftServer lunararc$getCraftServer();
}
