package io.lunararcdevs.lunararc.common.mixin.core.command;

import io.lunararcdevs.lunararc.common.bridge.CommandSourceStackBridge;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper's Adventure resolver can temporarily bypass entity-selector permission checks.
 * The normal loader/vanilla selector check remains authoritative whenever the flag is false.
 */
@Mixin(net.minecraft.commands.arguments.selector.EntitySelector.class)
public abstract class EntitySelectorMixin {
    @Inject(method = "checkPermissions", at = @At("HEAD"), cancellable = true, require = 0)
    private void lunararc$bypassSelectorPermissions(CommandSourceStack source, CallbackInfo ci) {
        if (((CommandSourceStackBridge) (Object) source).lunararc$bypassSelectorPermissions()) {
            ci.cancel();
        }
    }
}
