package io.lunararcdevs.lunararc.common.mixin.core.alchemy;

import io.lunararcdevs.lunararc.common.bridge.alchemy.PotionBrewingBridge;
import io.papermc.paper.potion.PotionMix;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionBrewing.class)
public abstract class PotionBrewingMixin implements PotionBrewingBridge {
    @Unique
    private final Map<NamespacedKey, LunarArcPotionMix> lunararc$customMixes = new LinkedHashMap<>();

    @Override
    public void lunararc$addPotionMix(PotionMix mix) {
        Objects.requireNonNull(mix, "mix");
        NamespacedKey key = Objects.requireNonNull(mix.getKey(), "mix key");
        if (this.lunararc$customMixes.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate potion mix ignored with ID " + key);
        }
        this.lunararc$customMixes.put(key, new LunarArcPotionMix(
                CraftItemStack.asNMSCopy(mix.getResult()),
                lunararc$predicate(mix.getInput()),
                lunararc$predicate(mix.getIngredient())));
    }

    @Override
    public boolean lunararc$removePotionMix(NamespacedKey key) {
        return this.lunararc$customMixes.remove(Objects.requireNonNull(key, "key")) != null;
    }

    @Override
    public void lunararc$clearPotionMixes() {
        this.lunararc$customMixes.clear();
    }

    @Override
    public boolean lunararc$isCustomInput(ItemStack stack) {
        for (LunarArcPotionMix mix : lunararc$mixesNewestFirst()) {
            if (mix.input().test(stack)) return true;
        }
        return false;
    }

    @Inject(method = "isIngredient", at = @At("HEAD"), cancellable = true)
    private void lunararc$isPaperIngredient(ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        for (LunarArcPotionMix mix : lunararc$mixesNewestFirst()) {
            if (mix.ingredient().test(ingredient)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "hasMix", at = @At("HEAD"), cancellable = true)
    private void lunararc$hasPaperMix(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        for (LunarArcPotionMix mix : lunararc$mixesNewestFirst()) {
            if (mix.input().test(input) && mix.ingredient().test(ingredient)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "mix", at = @At("HEAD"), cancellable = true)
    private void lunararc$mixPaperRecipe(ItemStack ingredient, ItemStack input,
            CallbackInfoReturnable<ItemStack> cir) {
        for (LunarArcPotionMix mix : lunararc$mixesNewestFirst()) {
            if (mix.input().test(input) && mix.ingredient().test(ingredient)) {
                cir.setReturnValue(mix.result().copy());
                return;
            }
        }
    }

    @Unique
    private List<LunarArcPotionMix> lunararc$mixesNewestFirst() {
        List<LunarArcPotionMix> values = new ArrayList<>(this.lunararc$customMixes.values());
        java.util.Collections.reverse(values);
        return values;
    }

    @Unique
    private static Predicate<ItemStack> lunararc$predicate(RecipeChoice choice) {
        Objects.requireNonNull(choice, "recipe choice");
        return stack -> choice.test(CraftItemStack.asBukkitCopy(stack));
    }

    @Unique
    private record LunarArcPotionMix(ItemStack result, Predicate<ItemStack> input, Predicate<ItemStack> ingredient) {}
}
