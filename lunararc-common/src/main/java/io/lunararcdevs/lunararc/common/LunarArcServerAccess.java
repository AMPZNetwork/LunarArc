package io.lunararcdevs.lunararc.common;

import io.lunararcdevs.lunararc.common.bridge.MinecraftServerBridge;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

import java.util.Objects;

public final class LunarArcServerAccess {
    private LunarArcServerAccess() {}

    public static CraftServer getCraftServer(MinecraftServer minecraftServer) {
        Objects.requireNonNull(minecraftServer, "minecraftServer");
        if (!(minecraftServer instanceof MinecraftServerBridge bridge)) {
            throw new IllegalStateException("MinecraftServer does not expose the LunarArc server bridge");
        }
        return bridge.lunararc$requireCraftServer();
    }

    public static CraftServer getCraftServer() {
        Server server = Bukkit.getServer();
        if (!(server instanceof CraftServer craftServer)) {
            throw new IllegalStateException("LunarArc CraftServer is not initialized");
        }
        return craftServer;
    }

    public static MinecraftServer getMinecraftServer() {
        return getCraftServer().getServer();
    }

    /**
     * Real Paper's Level.getWorld() is a literal method it adds directly to Level's source, so
     * any other Paper-side file can call level.getWorld() and javac sees it at compile time.
     * LevelMixin.getWorld() (same real logic) only exists via Mixin bytecode weaving, so
     * separately-compiled files can't call it that way — this is the same real logic exposed as
     * a normal static method any file can actually call.
     */
    public static CraftWorld getCraftWorld(Level level) {
        Objects.requireNonNull(level, "level");
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException("Bukkit world requested from a non-server level");
        }
        return getCraftServer(serverLevel.getServer()).getCraftWorld(serverLevel);
    }
}
