package io.ampznetwork.lunararc.common.server;

import net.minecraft.server.level.ServerPlayer;

public final class LunarArcContext {
    private static final ThreadLocal<ServerPlayer> CURRENT_PLAYER = new ThreadLocal<>();

    private LunarArcContext() {}

    private static ServerPlayer FAKE_PLAYER;

    public static void setCurrentPlayer(ServerPlayer player) {
        CURRENT_PLAYER.set(player);
    }

    public static ServerPlayer getCurrentPlayer() {
        return CURRENT_PLAYER.get();
    }

    public static ServerPlayer getFakePlayer(net.minecraft.server.level.ServerLevel level) {
        if (FAKE_PLAYER == null) {
            com.mojang.authlib.GameProfile profile = new com.mojang.authlib.GameProfile(
                java.util.UUID.fromString("41C82CFA-7D36-4BDE-94F3-3221944886E6"), "[LunarArc]"
            );
            FAKE_PLAYER = new net.minecraft.server.level.ServerPlayer(level.getServer(), level, profile, net.minecraft.server.level.ClientInformation.createDefault());
        }
        return FAKE_PLAYER;
    }

    public static void clear() {
        CURRENT_PLAYER.remove();
    }
}
