package io.lunararcdevs.lunararc.common.mixin.core.trading;

import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read/write access required by the concrete CraftMerchantRecipe adapter. */
@Mixin(MerchantOffer.class)
public interface MerchantOfferAccessor extends io.lunararcdevs.lunararc.common.bridge.access.MerchantOfferAccessBridge {
    @Accessor("baseCostA") ItemCost lunararc$baseCostA();
    @Accessor("baseCostA") void lunararc$baseCostA(ItemCost value);
    @Accessor("costB") Optional<ItemCost> lunararc$costB();
    @Accessor("costB") void lunararc$costB(Optional<ItemCost> value);
    @Accessor("result") ItemStack lunararc$result();
    @Accessor("uses") int lunararc$uses();
    @Accessor("uses") void lunararc$uses(int value);
    @Accessor("maxUses") int lunararc$maxUses();
    @Accessor("maxUses") void lunararc$maxUses(int value);
    @Accessor("rewardExp") boolean lunararc$rewardExp();
    @Accessor("rewardExp") void lunararc$rewardExp(boolean value);
    @Accessor("specialPriceDiff") int lunararc$specialPriceDiff();
    @Accessor("specialPriceDiff") void lunararc$specialPriceDiff(int value);
    @Accessor("demand") int lunararc$demand();
    @Accessor("demand") void lunararc$demand(int value);
    @Accessor("priceMultiplier") float lunararc$priceMultiplier();
    @Accessor("priceMultiplier") void lunararc$priceMultiplier(float value);
    @Accessor("xp") int lunararc$xp();
    @Accessor("xp") void lunararc$xp(int value);
}
