package io.lunararcdevs.lunararc.common.server;

import net.minecraft.server.level.ServerPlayer;

/** Short-lived server/player call context. */
public final class LunarArcContext {
    private static final ThreadLocal<ServerPlayer> CURRENT_PLAYER = new ThreadLocal<>();
    private static final java.util.Map<net.minecraft.server.level.ServerLevel, java.lang.ref.WeakReference<ServerPlayer>>
            FAKE_PLAYERS = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    private LunarArcContext() {}

    public static void setCurrentPlayer(ServerPlayer player) {
        if (player == null) CURRENT_PLAYER.remove();
        else CURRENT_PLAYER.set(player);
    }

    public static ServerPlayer getCurrentPlayer() {
        return CURRENT_PLAYER.get();
    }

    public static ServerPlayer getFakePlayer(net.minecraft.server.level.ServerLevel level) {
        java.util.Objects.requireNonNull(level, "level");
        synchronized (FAKE_PLAYERS) {
            java.lang.ref.WeakReference<ServerPlayer> reference = FAKE_PLAYERS.get(level);
            ServerPlayer existing = reference != null ? reference.get() : null;
            if (existing != null && existing.serverLevel() == level) return existing;

            com.mojang.authlib.GameProfile profile = new com.mojang.authlib.GameProfile(
                    java.util.UUID.fromString("41C82CFA-7D36-4BDE-94F3-3221944886E6"), "[LunarArc]");
            ServerPlayer created = new net.minecraft.server.level.ServerPlayer(
                    level.getServer(), level, profile, net.minecraft.server.level.ClientInformation.createDefault());
            FAKE_PLAYERS.put(level, new java.lang.ref.WeakReference<>(created));
            return created;
        }
    }

    /** Clear thread-local call state and stale weak cache entries. */
    public static void clear() {
        CURRENT_PLAYER.remove();
        synchronized (FAKE_PLAYERS) {
            FAKE_PLAYERS.entrySet().removeIf(entry -> entry.getValue().get() == null);
        }
    }

    /** Explicit shutdown/reload hook; does not own or stop loader/NMS objects. */
    public static void clearServerReferences() {
        CURRENT_PLAYER.remove();
        synchronized (FAKE_PLAYERS) {
            FAKE_PLAYERS.clear();
        }
    }
}
