package io.ampznetwork.lunararc.common.mixin.mod.util;

import io.ampznetwork.lunararc.common.mod.util.VelocityForwardQuery;
import net.minecraft.network.protocol.login.custom.CustomQueryPayload;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(VelocityForwardQuery.class)
public abstract class VelocityForwardQueryMixin implements CustomQueryPayload {
}
