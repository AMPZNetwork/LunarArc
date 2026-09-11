package io.lunararcdevs.lunararc.common.bridge;

/** Narrow Paper compatibility state attached directly to the real NMS FoodData instance. */
public interface FoodDataBridge {
    int lunararc$getSaturatedRegenRate();
    void lunararc$setSaturatedRegenRate(int rate);
    int lunararc$getUnsaturatedRegenRate();
    void lunararc$setUnsaturatedRegenRate(int rate);
    int lunararc$getStarvationRate();
    void lunararc$setStarvationRate(int rate);
    void lunararc$setOwner(net.minecraft.world.entity.player.Player player);
}
