package io.lunararcdevs.lunararc.common.bridge.entity;

import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffers;

/** Narrow access to protected/private AbstractVillager trade state. */
public interface AbstractVillagerBridge {
    void lunararc$resetOffers();
    void lunararc$replaceOffers(MerchantOffers offers);
    void lunararc$addOffers(MerchantOffers offers, VillagerTrades.ItemListing[] listings, int amount);
}
