package io.lunararcdevs.lunararc.common.mixin.core.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Narrow bridge for Bukkit's explicit player save API. */
@Mixin(PlayerList.class)
public interface PlayerListAccessor extends io.lunararcdevs.lunararc.common.bridge.access.PlayerListAccessBridge {
    @Invoker("save") void lunararc$invokeSave(ServerPlayer player);
}
