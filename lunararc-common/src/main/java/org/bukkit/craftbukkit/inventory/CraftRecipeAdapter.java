package org.bukkit.craftbukkit.inventory;

import com.google.common.collect.Maps;
import io.lunararcdevs.lunararc.common.bridge.recipe.IngredientBridge;
import io.lunararcdevs.lunararc.common.bridge.recipe.CopyDataComponentsBridge;
import io.lunararcdevs.lunararc.common.bridge.access.AbstractCookingRecipeAccessBridge;
import io.lunararcdevs.lunararc.common.bridge.access.ShapedRecipeAccessBridge;
import io.lunararcdevs.lunararc.common.bridge.access.ShapelessRecipeAccessBridge;
import io.lunararcdevs.lunararc.common.bridge.access.SingleItemRecipeAccessBridge;
import io.lunararcdevs.lunararc.common.bridge.access.SmithingTransformRecipeAccessBridge;
import io.lunararcdevs.lunararc.common.bridge.access.SmithingTrimRecipeAccessBridge;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.inventory.SmithingTrimRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.StonecuttingRecipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class CraftRecipeAdapter {
    private static final char[] SHAPE_SYMBOLS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    private CraftRecipeAdapter() {}

    public static ResourceLocation toMinecraft(NamespacedKey key) {
        Objects.requireNonNull(key, "key");
        return ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey());
    }

    public static NamespacedKey toBukkit(ResourceLocation key) {
        Objects.requireNonNull(key, "key");
        return new NamespacedKey(key.getNamespace(), key.getPath());
    }

    public static Ingredient toIngredient(RecipeChoice choice, boolean requireNotEmpty) {
        if (choice instanceof CraftNmsRecipeChoice nmsChoice) {
            return nmsChoice.getHandle();
        }

        final Ingredient ingredient;
        if (choice == null || choice == RecipeChoice.empty()) {
            ingredient = Ingredient.EMPTY;
        } else if (choice instanceof RecipeChoice.MaterialChoice materialChoice) {
            Stream<net.minecraft.world.item.ItemStack> values = materialChoice.getChoices().stream()
                    .filter(material -> material != Material.AIR)
                    .map(material -> CraftItemStack.asNMSCopy(new ItemStack(material)));
            ingredient = Ingredient.of(values);
        } else if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
            Stream<net.minecraft.world.item.ItemStack> values = exactChoice.getChoices().stream()
                    .map(CraftItemStack::asNMSCopy)
                    .filter(stack -> !stack.isEmpty());
            ingredient = Ingredient.of(values);
            ((IngredientBridge) (Object) ingredient).lunararc$setExact(true);
        } else {
            throw new IllegalArgumentException("Unsupported RecipeChoice implementation: " + choice.getClass().getName());
        }

        if (requireNotEmpty && ingredient.getItems().length == 0) {
            throw new IllegalArgumentException("Recipe requires at least one non-air choice");
        }
        return ingredient;
    }

    /**
     * Preserve the real NMS predicate for loader/custom ingredients. Exact
     * choices retain Paper's ExactChoice API shape; all other NMS ingredients
     * use a server-owned RecipeChoice that delegates test() to Ingredient.
     */
    public static RecipeChoice toBukkit(Ingredient ingredient) {
        Objects.requireNonNull(ingredient, "ingredient");
        net.minecraft.world.item.ItemStack[] stacks = ingredient.getItems();
        if (stacks.length == 0 && ingredient.isEmpty()) return RecipeChoice.empty();

        boolean exact = ((IngredientBridge) (Object) ingredient).lunararc$isExact();
        if (exact) {
            List<ItemStack> choices = new ArrayList<>(stacks.length);
            for (net.minecraft.world.item.ItemStack stack : stacks) {
                if (!stack.isEmpty()) choices.add(CraftItemStack.asBukkitCopy(stack));
            }
            if (!choices.isEmpty()) return new RecipeChoice.ExactChoice(choices);
        }

        return new CraftNmsRecipeChoice(ingredient);
    }

    private static CraftingBookCategory craftingCategory(org.bukkit.inventory.recipe.CraftingBookCategory category) {
        return CraftingBookCategory.valueOf(category.name());
    }

    private static org.bukkit.inventory.recipe.CraftingBookCategory bukkitCraftingCategory(CraftingBookCategory category) {
        return org.bukkit.inventory.recipe.CraftingBookCategory.valueOf(category.name());
    }

    private static CookingBookCategory cookingCategory(org.bukkit.inventory.recipe.CookingBookCategory category) {
        return CookingBookCategory.valueOf(category.name());
    }

    private static org.bukkit.inventory.recipe.CookingBookCategory bukkitCookingCategory(CookingBookCategory category) {
        return org.bukkit.inventory.recipe.CookingBookCategory.valueOf(category.name());
    }

    public static RecipeHolder<?> toMinecraft(Recipe recipe) {
        Objects.requireNonNull(recipe, "recipe");
        if (!(recipe instanceof Keyed keyed)) {
            throw new IllegalArgumentException("Bukkit recipe must implement Keyed: " + recipe.getClass().getName());
        }
        ResourceLocation id = toMinecraft(keyed.getKey());

        if (recipe instanceof ShapedRecipe shaped) {
            Map<Character, RecipeChoice> sourceChoices = new LinkedHashMap<>(shaped.getChoiceMap());
            String[] shape = shaped.getShape().clone();
            for (int row = 0; row < shape.length; row++) {
                StringBuilder normalized = new StringBuilder(shape[row].length());
                for (char symbol : shape[row].toCharArray()) {
                    normalized.append(sourceChoices.get(symbol) == null ? ' ' : symbol);
                }
                shape[row] = normalized.toString();
            }
            sourceChoices.values().removeIf(Objects::isNull);
            Map<Character, Ingredient> ingredients = Maps.transformValues(sourceChoices,
                    choice -> toIngredient(choice, false));
            ShapedRecipePattern pattern = ShapedRecipePattern.of(ingredients, shape);
            net.minecraft.world.item.crafting.ShapedRecipe nms = new net.minecraft.world.item.crafting.ShapedRecipe(
                    shaped.getGroup(), craftingCategory(shaped.getCategory()), pattern,
                    CraftItemStack.asNMSCopy(shaped.getResult()));
            return new RecipeHolder<>(id, nms);
        }

        if (recipe instanceof ShapelessRecipe shapeless) {
            List<RecipeChoice> choices = shapeless.getChoiceList();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(choices.size(), Ingredient.EMPTY);
            for (int i = 0; i < choices.size(); i++) {
                ingredients.set(i, toIngredient(choices.get(i), true));
            }
            net.minecraft.world.item.crafting.ShapelessRecipe nms = new net.minecraft.world.item.crafting.ShapelessRecipe(
                    shapeless.getGroup(), craftingCategory(shapeless.getCategory()),
                    CraftItemStack.asNMSCopy(shapeless.getResult()), ingredients);
            return new RecipeHolder<>(id, nms);
        }

        if (recipe instanceof BlastingRecipe blasting) {
            return new RecipeHolder<>(id, new net.minecraft.world.item.crafting.BlastingRecipe(
                    blasting.getGroup(), cookingCategory(blasting.getCategory()),
                    toIngredient(blasting.getInputChoice(), true), CraftItemStack.asNMSCopy(blasting.getResult()),
                    blasting.getExperience(), blasting.getCookingTime()));
        }
        if (recipe instanceof CampfireRecipe campfire) {
            return new RecipeHolder<>(id, new net.minecraft.world.item.crafting.CampfireCookingRecipe(
                    campfire.getGroup(), cookingCategory(campfire.getCategory()),
                    toIngredient(campfire.getInputChoice(), true), CraftItemStack.asNMSCopy(campfire.getResult()),
                    campfire.getExperience(), campfire.getCookingTime()));
        }
        if (recipe instanceof SmokingRecipe smoking) {
            return new RecipeHolder<>(id, new net.minecraft.world.item.crafting.SmokingRecipe(
                    smoking.getGroup(), cookingCategory(smoking.getCategory()),
                    toIngredient(smoking.getInputChoice(), true), CraftItemStack.asNMSCopy(smoking.getResult()),
                    smoking.getExperience(), smoking.getCookingTime()));
        }
        if (recipe instanceof FurnaceRecipe furnace) {
            return new RecipeHolder<>(id, new net.minecraft.world.item.crafting.SmeltingRecipe(
                    furnace.getGroup(), cookingCategory(furnace.getCategory()),
                    toIngredient(furnace.getInputChoice(), true), CraftItemStack.asNMSCopy(furnace.getResult()),
                    furnace.getExperience(), furnace.getCookingTime()));
        }
        if (recipe instanceof StonecuttingRecipe stonecutting) {
            return new RecipeHolder<>(id, new net.minecraft.world.item.crafting.StonecutterRecipe(
                    stonecutting.getGroup(), toIngredient(stonecutting.getInputChoice(), true),
                    CraftItemStack.asNMSCopy(stonecutting.getResult())));
        }
        if (recipe instanceof SmithingTransformRecipe smithing) {
            net.minecraft.world.item.crafting.SmithingTransformRecipe nms =
                    new net.minecraft.world.item.crafting.SmithingTransformRecipe(
                            toIngredient(smithing.getTemplate(), false),
                            toIngredient(smithing.getBase(), false),
                            toIngredient(smithing.getAddition(), false),
                            CraftItemStack.asNMSCopy(smithing.getResult()));
            ((CopyDataComponentsBridge) (Object) nms).lunararc$copyDataComponents(smithing.willCopyDataComponents());
            return new RecipeHolder<>(id, nms);
        }
        if (recipe instanceof SmithingTrimRecipe smithing) {
            net.minecraft.world.item.crafting.SmithingTrimRecipe nms =
                    new net.minecraft.world.item.crafting.SmithingTrimRecipe(
                            toIngredient(smithing.getTemplate(), false),
                            toIngredient(smithing.getBase(), false),
                            toIngredient(smithing.getAddition(), false));
            ((CopyDataComponentsBridge) (Object) nms).lunararc$copyDataComponents(smithing.willCopyDataComponents());
            return new RecipeHolder<>(id, nms);
        }
        if (recipe instanceof CraftComplexRecipe complex) {
            return complex.getHandle();
        }

        throw new UnsupportedOperationException("Unsupported Bukkit recipe type: " + recipe.getClass().getName());
    }

    /** Convert a real 1.21.1 NMS recipe into the corresponding Bukkit type. */
    public static Recipe toBukkit(RecipeHolder<?> holder) {
        Objects.requireNonNull(holder, "holder");
        NamespacedKey key = toBukkit(holder.id());
        net.minecraft.world.item.crafting.Recipe<?> nms = holder.value();

        if (nms instanceof net.minecraft.world.item.crafting.ShapedRecipe shaped) {
            ShapedRecipeAccessBridge access = (ShapedRecipeAccessBridge) (Object) shaped;
            ShapedRecipePattern pattern = access.lunararc$pattern();
            int width = pattern.width();
            int height = pattern.height();
            if (width < 1 || height < 1 || width > 3 || height > 3 || width * height > SHAPE_SYMBOLS.length) {
                return new CraftComplexRecipe(holder);
            }

            ShapedRecipe recipe = new ShapedRecipe(key, CraftItemStack.asBukkitCopy(access.lunararc$result()));
            recipe.setGroup(access.lunararc$group());
            recipe.setCategory(bukkitCraftingCategory(access.lunararc$category()));

            List<Ingredient> ingredients = pattern.ingredients();
            String[] shape = new String[height];
            int symbolIndex = 0;
            Map<Character, Ingredient> mapped = new LinkedHashMap<>();
            for (int y = 0; y < height; y++) {
                StringBuilder row = new StringBuilder(width);
                for (int x = 0; x < width; x++) {
                    int index = y * width + x;
                    Ingredient ingredient = index < ingredients.size() ? ingredients.get(index) : Ingredient.EMPTY;
                    if (ingredient == null || ingredient.isEmpty()) {
                        row.append(' ');
                    } else {
                        char symbol = SHAPE_SYMBOLS[symbolIndex++];
                        row.append(symbol);
                        mapped.put(symbol, ingredient);
                    }
                }
                shape[y] = row.toString();
            }
            recipe.shape(shape);
            mapped.forEach((symbol, ingredient) -> recipe.setIngredient(symbol, toBukkit(ingredient)));
            return recipe;
        }

        if (nms instanceof net.minecraft.world.item.crafting.ShapelessRecipe shapeless) {
            ShapelessRecipeAccessBridge access = (ShapelessRecipeAccessBridge) (Object) shapeless;
            ShapelessRecipe recipe = new ShapelessRecipe(key, CraftItemStack.asBukkitCopy(access.lunararc$result()));
            recipe.setGroup(access.lunararc$group());
            recipe.setCategory(bukkitCraftingCategory(access.lunararc$category()));
            for (Ingredient ingredient : access.lunararc$ingredients()) {
                if (ingredient != null && !ingredient.isEmpty()) recipe.addIngredient(toBukkit(ingredient));
            }
            return recipe;
        }

        if (nms instanceof AbstractCookingRecipe cooking) {
            AbstractCookingRecipeAccessBridge access = (AbstractCookingRecipeAccessBridge) (Object) cooking;
            ItemStack result = CraftItemStack.asBukkitCopy(access.lunararc$result());
            RecipeChoice input = toBukkit(access.lunararc$ingredient());
            String group = access.lunararc$group();
            float experience = access.lunararc$experience();
            int time = access.lunararc$cookingTime();
            org.bukkit.inventory.recipe.CookingBookCategory category = bukkitCookingCategory(access.lunararc$category());

            if (nms instanceof net.minecraft.world.item.crafting.BlastingRecipe) {
                BlastingRecipe recipe = new BlastingRecipe(key, result, input, experience, time);
                recipe.setGroup(group); recipe.setCategory(category); return recipe;
            }
            if (nms instanceof net.minecraft.world.item.crafting.CampfireCookingRecipe) {
                CampfireRecipe recipe = new CampfireRecipe(key, result, input, experience, time);
                recipe.setGroup(group); recipe.setCategory(category); return recipe;
            }
            if (nms instanceof net.minecraft.world.item.crafting.SmokingRecipe) {
                SmokingRecipe recipe = new SmokingRecipe(key, result, input, experience, time);
                recipe.setGroup(group); recipe.setCategory(category); return recipe;
            }
            if (nms instanceof net.minecraft.world.item.crafting.SmeltingRecipe) {
                FurnaceRecipe recipe = new FurnaceRecipe(key, result, input, experience, time);
                recipe.setGroup(group); recipe.setCategory(category); return recipe;
            }
            return new CraftComplexRecipe(holder);
        }

        if (nms instanceof net.minecraft.world.item.crafting.StonecutterRecipe stonecutter) {
            SingleItemRecipeAccessBridge access = (SingleItemRecipeAccessBridge) (Object) stonecutter;
            StonecuttingRecipe recipe = new StonecuttingRecipe(key,
                    CraftItemStack.asBukkitCopy(access.lunararc$result()), toBukkit(access.lunararc$ingredient()));
            recipe.setGroup(access.lunararc$group());
            return recipe;
        }

        if (nms instanceof net.minecraft.world.item.crafting.SmithingTransformRecipe smithing) {
            SmithingTransformRecipeAccessBridge access = (SmithingTransformRecipeAccessBridge) (Object) smithing;
            boolean copy = ((CopyDataComponentsBridge) (Object) smithing).lunararc$copyDataComponents();
            return new SmithingTransformRecipe(key, CraftItemStack.asBukkitCopy(access.lunararc$result()),
                    toBukkit(access.lunararc$template()), toBukkit(access.lunararc$base()),
                    toBukkit(access.lunararc$addition()), copy);
        }

        if (nms instanceof net.minecraft.world.item.crafting.SmithingTrimRecipe smithing) {
            SmithingTrimRecipeAccessBridge access = (SmithingTrimRecipeAccessBridge) (Object) smithing;
            boolean copy = ((CopyDataComponentsBridge) (Object) smithing).lunararc$copyDataComponents();
            return new SmithingTrimRecipe(key, toBukkit(access.lunararc$template()),
                    toBukkit(access.lunararc$base()), toBukkit(access.lunararc$addition()), copy);
        }

        // Vanilla special recipes and loader-defined imperative recipes have no
        // stable shaped/cooking representation. ComplexRecipe is Bukkit's
        // concrete contract for server-defined recipe behavior.
        return new CraftComplexRecipe(holder);
    }
}
