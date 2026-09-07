package io.ampznetwork.lunararc.common.compat;

import com.google.gson.JsonElement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;

public final class MarketRecipeFilter {
    private MarketRecipeFilter() {}

    public static Map<ResourceLocation, JsonElement> filter(Map<ResourceLocation, JsonElement> recipes,
            Predicate<ResourceLocation> itemExists) {
        Map<ResourceLocation, JsonElement> filtered = new LinkedHashMap<>(recipes);
        filtered.entrySet().removeIf(entry -> {
            ResourceLocation id = entry.getKey();
            if (!id.getNamespace().equals("farmingforblockheads") || !id.getPath().startsWith("market/")) return false;
            if (!entry.getValue().isJsonObject()) return false;
            var recipe = entry.getValue().getAsJsonObject();
            if (!recipe.has("type") || !recipe.get("type").isJsonPrimitive()
                    || !"farmingforblockheads:market".equals(recipe.get("type").getAsString())) return false;
            if (!recipe.has("result") || !recipe.get("result").isJsonObject()) return false;
            var result = recipe.getAsJsonObject("result");
            if (!result.has("item") || !result.get("item").isJsonPrimitive()
                    || !result.getAsJsonPrimitive("item").isString()) return false;
            ResourceLocation item = ResourceLocation.tryParse(result.get("item").getAsString());
            return item != null && !itemExists.test(item);
        });
        return filtered;
    }
}
