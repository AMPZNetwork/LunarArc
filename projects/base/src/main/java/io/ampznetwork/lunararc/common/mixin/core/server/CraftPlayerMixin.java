package io.ampznetwork.lunararc.common.mixin.core.server;

import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftPlayer.class, remap = false)
public abstract class CraftPlayerMixin {

    @Shadow(remap = false)
    private net.minecraft.server.level.ServerPlayer player;

    @Inject(method = "isOp", at = @At("HEAD"), cancellable = true, remap = false)
    private void lunararc$onIsOpInit(CallbackInfoReturnable<Boolean> cir) {
        if (this.player == null) {
            cir.setReturnValue(false);
        }
    }
}