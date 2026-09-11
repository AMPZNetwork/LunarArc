package io.lunararcdevs.lunararc.common.mixin.core.trading;

import io.lunararcdevs.lunararc.common.bridge.trading.MerchantOfferBridge;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MerchantOffer.class)
public abstract class MerchantOfferMixin implements MerchantOfferBridge {
    @Unique private boolean lunararc$ignoreDiscounts;

    @Override public boolean lunararc$ignoreDiscounts() { return this.lunararc$ignoreDiscounts; }
    @Override public void lunararc$ignoreDiscounts(boolean ignoreDiscounts) { this.lunararc$ignoreDiscounts = ignoreDiscounts; }
}
