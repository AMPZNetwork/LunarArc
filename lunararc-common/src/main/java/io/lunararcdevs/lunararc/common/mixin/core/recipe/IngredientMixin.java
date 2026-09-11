package io.lunararcdevs.lunararc.common.mixin.core.recipe;

import io.lunararcdevs.lunararc.common.bridge.recipe.IngredientBridge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Paper exact RecipeChoice semantics without replacing the loader's Ingredient class. */
@Mixin(Ingredient.class)
public abstract class IngredientMixin implements IngredientBridge {
    @Unique private boolean lunararc$exact;

    @Override public boolean lunararc$isExact() { return this.lunararc$exact; }
    @Override public void lunararc$setExact(boolean exact) { this.lunararc$exact = exact; }

    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void lunararc$testExact(ItemStack candidate, CallbackInfoReturnable<Boolean> cir) {
        if (!this.lunararc$exact) return;
        if (candidate == null || candidate.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }
        for (ItemStack accepted : ((Ingredient) (Object) this).getItems()) {
            if (ItemStack.isSameItemSameComponents(accepted, candidate)) {
                cir.setReturnValue(true);
                return;
            }
        }
        cir.setReturnValue(false);
    }
}
