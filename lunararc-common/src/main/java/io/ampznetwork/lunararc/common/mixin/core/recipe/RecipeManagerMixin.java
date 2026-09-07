package io.ampznetwork.lunararc.common.mixin.core.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.ampznetwork.lunararc.common.bridge.recipe.RecipeManagerBridge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** CraftBukkit recipe mutation hooks applied to the real Minecraft RecipeManager. */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin implements RecipeManagerBridge {
    @Shadow private Multimap<RecipeType<?>, RecipeHolder<?>> byType;
    @Shadow private Map<ResourceLocation, RecipeHolder<?>> byName;

    @org.spongepowered.asm.mixin.injection.ModifyVariable(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @org.spongepowered.asm.mixin.injection.At("HEAD"), argsOnly = true)
    private Map<ResourceLocation, com.google.gson.JsonElement> lunararc$filterOptionalMarketRecipes(
            Map<ResourceLocation, com.google.gson.JsonElement> recipes) {
        var filtered = io.ampznetwork.lunararc.common.compat.MarketRecipeFilter.filter(
                recipes, net.minecraft.core.registries.BuiltInRegistries.ITEM::containsKey);
        int skipped = recipes.size() - filtered.size();
        if (skipped > 0) org.slf4j.LoggerFactory.getLogger("LunarArc").info(
                "Skipped {} optional market recipes whose result items are not registered", skipped);
        return filtered;
    }

    @Override
    public synchronized Collection<RecipeHolder<?>> lunararc$recipes() {
        return new ArrayList<>(this.byName.values());
    }

    @Override
    public synchronized RecipeHolder<?> lunararc$recipe(ResourceLocation id) {
        return this.byName.get(id);
    }

    @Override
    public synchronized boolean lunararc$addRecipe(RecipeHolder<?> recipe) {
        if (this.byName.containsKey(recipe.id())) return false;
        Multimap<RecipeType<?>, RecipeHolder<?>> mutableByType = LinkedHashMultimap.create(this.byType);
        Map<ResourceLocation, RecipeHolder<?>> mutableByName = new HashMap<>(this.byName);
        mutableByType.put(recipe.value().getType(), recipe);
        mutableByName.put(recipe.id(), recipe);
        this.byType = ImmutableMultimap.copyOf(mutableByType);
        this.byName = ImmutableMap.copyOf(mutableByName);
        return true;
    }

    @Override
    public synchronized boolean lunararc$removeRecipe(ResourceLocation id) {
        if (!this.byName.containsKey(id)) return false;
        Multimap<RecipeType<?>, RecipeHolder<?>> mutableByType = LinkedHashMultimap.create(this.byType);
        Map<ResourceLocation, RecipeHolder<?>> mutableByName = new HashMap<>(this.byName);
        mutableByType.values().removeIf(holder -> holder.id().equals(id));
        mutableByName.remove(id);
        this.byType = ImmutableMultimap.copyOf(mutableByType);
        this.byName = ImmutableMap.copyOf(mutableByName);
        return true;
    }

    @Override
    public synchronized void lunararc$clearRecipes() {
        this.byType = ImmutableMultimap.of();
        this.byName = ImmutableMap.of();
    }
}
