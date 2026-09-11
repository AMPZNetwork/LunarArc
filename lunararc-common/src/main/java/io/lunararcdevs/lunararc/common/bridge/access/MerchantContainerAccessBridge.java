package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface MerchantContainerAccessBridge {
    int lunararc$getSelectionHint();
    @Nullable MerchantOffer lunararc$getActiveOffer();
}
