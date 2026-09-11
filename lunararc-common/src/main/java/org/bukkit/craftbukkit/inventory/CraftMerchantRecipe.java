package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import io.lunararcdevs.lunararc.common.bridge.trading.MerchantOfferBridge;
import io.lunararcdevs.lunararc.common.bridge.access.MerchantOfferAccessBridge;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

/** Bukkit view over the actual NMS MerchantOffer. */
public final class CraftMerchantRecipe extends MerchantRecipe {
    private final MerchantOffer handle;

    public CraftMerchantRecipe(MerchantOffer handle) {
        super(CraftItemStack.asBukkitCopy(((MerchantOfferAccessBridge) (Object) handle).lunararc$result()),
                ((MerchantOfferAccessBridge) (Object) handle).lunararc$uses(),
                ((MerchantOfferAccessBridge) (Object) handle).lunararc$maxUses(),
                ((MerchantOfferAccessBridge) (Object) handle).lunararc$rewardExp(),
                ((MerchantOfferAccessBridge) (Object) handle).lunararc$xp(),
                ((MerchantOfferAccessBridge) (Object) handle).lunararc$priceMultiplier(),
                ((MerchantOfferAccessBridge) (Object) handle).lunararc$demand(),
                ((MerchantOfferAccessBridge) (Object) handle).lunararc$specialPriceDiff(),
                ((MerchantOfferBridge) (Object) handle).lunararc$ignoreDiscounts());
        this.handle = handle;
        MerchantOfferAccessBridge accessor = (MerchantOfferAccessBridge) (Object) handle;
        addIngredient(CraftItemStack.asBukkitCopy(accessor.lunararc$baseCostA().itemStack()));
        accessor.lunararc$costB().ifPresent(cost -> addIngredient(CraftItemStack.asBukkitCopy(cost.itemStack())));
    }

    public static CraftMerchantRecipe fromBukkit(MerchantRecipe recipe) {
        if (recipe instanceof CraftMerchantRecipe craft) return craft;
        List<ItemStack> ingredients = recipe.getIngredients();
        Preconditions.checkArgument(!ingredients.isEmpty(), "MerchantRecipe requires at least one ingredient");
        Preconditions.checkArgument(ingredients.size() <= 2, "MerchantRecipe supports at most two ingredients");

        net.minecraft.world.item.ItemStack first = CraftItemStack.asNMSCopy(ingredients.get(0));
        ItemCost firstCost = itemCost(first);
        Optional<ItemCost> second = ingredients.size() == 2
                ? Optional.of(itemCost(CraftItemStack.asNMSCopy(ingredients.get(1))))
                : Optional.empty();
        MerchantOffer offer = new MerchantOffer(firstCost, second, CraftItemStack.asNMSCopy(recipe.getResult()),
                recipe.getUses(), recipe.getMaxUses(), recipe.getVillagerExperience(), recipe.getPriceMultiplier(), recipe.getDemand());
        MerchantOfferAccessBridge accessor = (MerchantOfferAccessBridge) (Object) offer;
        accessor.lunararc$rewardExp(recipe.hasExperienceReward());
        accessor.lunararc$specialPriceDiff(recipe.getSpecialPrice());
        ((MerchantOfferBridge) (Object) offer).lunararc$ignoreDiscounts(recipe.shouldIgnoreDiscounts());
        return new CraftMerchantRecipe(offer);
    }

    private static ItemCost itemCost(net.minecraft.world.item.ItemStack stack) {
        DataComponentPredicate predicate = DataComponentPredicate.allOf(
                PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, stack.getComponentsPatch()));
        return new ItemCost(stack.getItemHolder(), stack.getCount(), predicate, stack.copy());
    }

    public MerchantOffer getHandle() { return this.handle; }

    @Override public int getSpecialPrice() { return accessor().lunararc$specialPriceDiff(); }
    @Override public void setSpecialPrice(int value) { accessor().lunararc$specialPriceDiff(value); }
    @Override public int getDemand() { return accessor().lunararc$demand(); }
    @Override public void setDemand(int value) { accessor().lunararc$demand(value); }
    @Override public int getUses() { return accessor().lunararc$uses(); }
    @Override public void setUses(int value) { accessor().lunararc$uses(value); }
    @Override public int getMaxUses() { return accessor().lunararc$maxUses(); }
    @Override public void setMaxUses(int value) { accessor().lunararc$maxUses(value); }
    @Override public boolean hasExperienceReward() { return accessor().lunararc$rewardExp(); }
    @Override public void setExperienceReward(boolean value) { accessor().lunararc$rewardExp(value); }
    @Override public int getVillagerExperience() { return accessor().lunararc$xp(); }
    @Override public void setVillagerExperience(int value) { accessor().lunararc$xp(value); }
    @Override public float getPriceMultiplier() { return accessor().lunararc$priceMultiplier(); }
    @Override public void setPriceMultiplier(float value) { accessor().lunararc$priceMultiplier(value); }
    @Override public boolean shouldIgnoreDiscounts() { return ((MerchantOfferBridge) (Object) handle).lunararc$ignoreDiscounts(); }
    @Override public void setIgnoreDiscounts(boolean value) { ((MerchantOfferBridge) (Object) handle).lunararc$ignoreDiscounts(value); }

    public MerchantOffer toMinecraft() {
        List<ItemStack> ingredients = getIngredients();
        Preconditions.checkState(!ingredients.isEmpty(), "No offered ingredients");
        MerchantOfferAccessBridge accessor = accessor();
        accessor.lunararc$baseCostA(itemCost(CraftItemStack.asNMSCopy(ingredients.get(0))));
        accessor.lunararc$costB(ingredients.size() > 1
                ? Optional.of(itemCost(CraftItemStack.asNMSCopy(ingredients.get(1)))) : Optional.empty());
        return handle;
    }

    private MerchantOfferAccessBridge accessor() { return (MerchantOfferAccessBridge) (Object) handle; }
}
