package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.item.trading.Merchant;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface MerchantMenuAccessBridge {
    MerchantContainer lunararc$getTradeContainer();
    Merchant lunararc$getTrader();
}
