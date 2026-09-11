package io.lunararcdevs.lunararc.common.mixin.core.inventory;

import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import org.jetbrains.annotations.Nullable;

@Mixin(MerchantContainer.class)
public interface MerchantContainerAccessor extends io.lunararcdevs.lunararc.common.bridge.access.MerchantContainerAccessBridge {
    @Accessor("selectionHint") int lunararc$getSelectionHint();
    @Accessor("activeOffer") @Nullable MerchantOffer lunararc$getActiveOffer();
}
