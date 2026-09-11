package io.lunararcdevs.lunararc.common.bridge.recipe;

/** Paper exact-choice state attached directly to the real NMS Ingredient. */
public interface IngredientBridge {
    boolean lunararc$isExact();
    void lunararc$setExact(boolean exact);
}
