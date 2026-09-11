package io.lunararcdevs.lunararc.common.mixin.api;

import io.papermc.paper.registry.RegistryAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = RegistryAccess.class, remap = false)
public interface RegistryAccessMixin {


    /**
     * Replaces Paper's registry-access lookup, which reaches for a server instance LunarArc builds
     * differently. Overwrite rather than inject because the entire result is LunarArc's, not a
     * modification of Paper's.
     *
     * @author LunarArc
     * @reason Paper resolves registries through its own server holder, which does not exist here.
     */
    @Overwrite
    static RegistryAccess registryAccess() {
        return io.lunararcdevs.lunararc.common.server.LunarArcRegistryAccess.INSTANCE;
    }
}
