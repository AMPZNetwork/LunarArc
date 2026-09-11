package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.server.level.ServerPlayer;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface PlayerListAccessBridge {
    void lunararc$invokeSave(ServerPlayer player);
}
