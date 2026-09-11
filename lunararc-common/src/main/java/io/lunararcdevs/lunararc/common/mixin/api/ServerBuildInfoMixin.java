package io.lunararcdevs.lunararc.common.mixin.api;

import io.lunararcdevs.lunararc.common.server.LunarArcServerBuildInfo;
import io.papermc.paper.ServerBuildInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ServerBuildInfo.class, remap = false)
public interface ServerBuildInfoMixin {


    /**
     * Replaces Paper's own build-info lookup, which reads a manifest LunarArc does not ship and
     * would fail outright. Overwrite rather than inject because there is nothing of Paper's to keep
     * here: the whole answer differs on a hybrid.
     *
     * @author LunarArc
     * @reason Paper resolves its build info from a jar manifest that only its own distribution has.
     */
    @Overwrite
    static ServerBuildInfo buildInfo() {
        return LunarArcServerBuildInfo.INSTANCE;
    }
}
