package io.lunararcdevs.lunararc.common.mixin.bukkit;

import org.bukkit.NamespacedKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(org.bukkit.Material.class)
public abstract class MaterialMixin {

    @Shadow(remap = false)
    public abstract boolean isLegacy();

    @Inject(method = "getKey", at = @At("HEAD"), cancellable = true, remap = false)
    private void lunararc$onGetKey(CallbackInfoReturnable<NamespacedKey> cir) {
        org.bukkit.Material self = (org.bukkit.Material) (Object) this;
        NamespacedKey dynamicKey = io.lunararcdevs.lunararc.common.server.LunarArcDynamicBukkitEnums.materialKey(self);
        if (dynamicKey != null) {
            cir.setReturnValue(dynamicKey);
            return;
        }
        String enumName = ((Enum<?>) (Object) this).name();
        if (enumName.startsWith("LEGACY_")) {
            throw new IllegalArgumentException("Cannot get key of Legacy Material");
        }
        cir.setReturnValue(NamespacedKey.minecraft(enumName.toLowerCase(Locale.ROOT)));
    }

    @Inject(method = "isItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void lunararc$isItem(CallbackInfoReturnable<Boolean> cir) {
        if (isLegacy()) {
            cir.setReturnValue(false);
            return;
        }
        try {
            org.bukkit.Material self = (org.bukkit.Material) (Object) this;
            NamespacedKey dynamicKey = io.lunararcdevs.lunararc.common.server.LunarArcDynamicBukkitEnums.materialKey(self);
            net.minecraft.resources.ResourceLocation rl = dynamicKey == null
                    ? net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", ((Enum<?>) (Object) this).name().toLowerCase(Locale.ROOT))
                    : net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(dynamicKey.getNamespace(), dynamicKey.getKey());
            cir.setReturnValue(net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(rl));
        } catch (Exception ignored) {}
    }

    @Inject(method = "isBlock", at = @At("HEAD"), cancellable = true, remap = false)
    private void lunararc$isBlock(CallbackInfoReturnable<Boolean> cir) {
        if (isLegacy()) {
            cir.setReturnValue(false);
            return;
        }
        try {
            org.bukkit.Material self = (org.bukkit.Material) (Object) this;
            NamespacedKey dynamicKey = io.lunararcdevs.lunararc.common.server.LunarArcDynamicBukkitEnums.materialKey(self);
            net.minecraft.resources.ResourceLocation rl = dynamicKey == null
                    ? net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", ((Enum<?>) (Object) this).name().toLowerCase(Locale.ROOT))
                    : net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(dynamicKey.getNamespace(), dynamicKey.getKey());
            cir.setReturnValue(net.minecraft.core.registries.BuiltInRegistries.BLOCK.containsKey(rl));
        } catch (Exception ignored) {}
    }
}
