package io.lunararcdevs.lunararc.common.mixin.core.inventory;

import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MerchantMenu.class)
public interface MerchantMenuAccessor extends io.lunararcdevs.lunararc.common.bridge.access.MerchantMenuAccessBridge {
    @Accessor("tradeContainer") MerchantContainer lunararc$getTradeContainer();
    @Accessor("trader") Merchant lunararc$getTrader();
}
