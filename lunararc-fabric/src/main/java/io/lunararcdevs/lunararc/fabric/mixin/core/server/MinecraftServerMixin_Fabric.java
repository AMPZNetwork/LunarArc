package io.lunararcdevs.lunararc.fabric.mixin.core.server;

import io.lunararcdevs.lunararc.common.bridge.MinecraftServerBridge;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin_Fabric implements MinecraftServerBridge {
    @Override
    public void lunararc$loaderLevelLoad(ServerLevel level) {
        ServerWorldEvents.LOAD.invoker().onWorldLoad((MinecraftServer) (Object) this, level);
    }

    @Override
    public void lunararc$loaderLevelUnload(ServerLevel level) {
        ServerWorldEvents.UNLOAD.invoker().onWorldUnload((MinecraftServer) (Object) this, level);
    }

    @Override
    public boolean lunararc$loaderHandlesBlockBreakEvent() {
        return true;
    }
}
