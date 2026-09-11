package io.lunararcdevs.lunararc.common.bridge.access;

import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface MerchantOfferAccessBridge {
    ItemCost lunararc$baseCostA();
    void lunararc$baseCostA(ItemCost value);
    Optional<ItemCost> lunararc$costB();
    void lunararc$costB(Optional<ItemCost> value);
    ItemStack lunararc$result();
    int lunararc$uses();
    void lunararc$uses(int value);
    int lunararc$maxUses();
    void lunararc$maxUses(int value);
    boolean lunararc$rewardExp();
    void lunararc$rewardExp(boolean value);
    int lunararc$specialPriceDiff();
    void lunararc$specialPriceDiff(int value);
    int lunararc$demand();
    void lunararc$demand(int value);
    float lunararc$priceMultiplier();
    void lunararc$priceMultiplier(float value);
    int lunararc$xp();
    void lunararc$xp(int value);
}
