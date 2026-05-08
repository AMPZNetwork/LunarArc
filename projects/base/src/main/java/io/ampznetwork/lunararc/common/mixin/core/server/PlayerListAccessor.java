package io.ampznetwork.lunararc.common.mixin.core.server;

import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerList.class)
public interface PlayerListAccessor {
    @Accessor("ops")
    ServerOpList getOps();
}
