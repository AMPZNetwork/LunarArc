package io.lunararcdevs.lunararc.common.mixin.core.recipe;

import io.lunararcdevs.lunararc.common.bridge.recipe.CopyDataComponentsBridge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SmithingTrimRecipe.class)
public abstract class SmithingTrimRecipeMixin implements CopyDataComponentsBridge {
    @Unique private boolean lunararc$copyDataComponents = true;

    @Override public boolean lunararc$copyDataComponents() { return this.lunararc$copyDataComponents; }
    @Override public void lunararc$copyDataComponents(boolean copy) { this.lunararc$copyDataComponents = copy; }

    @Redirect(
            method = "assemble(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copyWithCount(I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack lunararc$controlComponentCopy(ItemStack base, int count) {
        return this.lunararc$copyDataComponents ? base.copyWithCount(count) : new ItemStack(base.getItem(), count);
    }
}
