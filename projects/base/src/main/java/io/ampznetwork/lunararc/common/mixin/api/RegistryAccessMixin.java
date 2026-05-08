package io.ampznetwork.lunararc.common.mixin.api;

import io.papermc.paper.registry.RegistryAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = RegistryAccess.class, remap = false)
public interface RegistryAccessMixin {

    /**
     * @author LunarArc
     * @reason Redirect to our custom RegistryAccess implementation
     */
    @Overwrite
    static RegistryAccess registryAccess() {
        return io.ampznetwork.lunararc.common.server.LunarArcRegistryAccess.INSTANCE;
    }
}
