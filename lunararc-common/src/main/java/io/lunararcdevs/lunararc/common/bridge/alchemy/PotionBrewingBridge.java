package io.lunararcdevs.lunararc.common.bridge.alchemy;

import io.papermc.paper.potion.PotionMix;
import org.bukkit.NamespacedKey;

/** Paper custom-mix state attached directly to the loader-owned PotionBrewing. */
public interface PotionBrewingBridge {
    void lunararc$addPotionMix(PotionMix mix);
    boolean lunararc$removePotionMix(NamespacedKey key);
    void lunararc$clearPotionMixes();
    boolean lunararc$isCustomInput(net.minecraft.world.item.ItemStack stack);
}
