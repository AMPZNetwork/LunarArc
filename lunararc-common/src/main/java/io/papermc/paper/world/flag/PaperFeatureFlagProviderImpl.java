package io.papermc.paper.world.flag;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import org.bukkit.FeatureFlag;

public class PaperFeatureFlagProviderImpl implements FeatureFlagProvider {
    public static final BiMap<FeatureFlag, net.minecraft.world.flag.FeatureFlag> FLAGS = ImmutableBiMap.of(
            FeatureFlag.VANILLA, FeatureFlags.VANILLA, FeatureFlag.BUNDLE, FeatureFlags.BUNDLE,
            FeatureFlag.TRADE_REBALANCE, FeatureFlags.TRADE_REBALANCE);

    public static java.util.Set<FeatureFlag> fromNms(FeatureFlagSet flags) {
        java.util.Set<FeatureFlag> result = new java.util.HashSet<>();
        FLAGS.forEach((bukkit, nativeFlag) -> { if (flags.contains(nativeFlag)) result.add(bukkit); });
        return java.util.Collections.unmodifiableSet(result);
    }

    @Override public java.util.Set<FeatureFlag> requiredFeatures(FeatureDependant dependant) {
        if (dependant instanceof org.bukkit.entity.EntityType entity) {
            return fromNms(org.bukkit.craftbukkit.entity.CraftEntityType.bukkitToMinecraft(entity).requiredFeatures());
        }
        if (dependant instanceof org.bukkit.potion.PotionType potion) {
            return fromNms(org.bukkit.craftbukkit.potion.CraftPotionType.bukkitToMinecraft(potion).requiredFeatures());
        }
        throw new IllegalArgumentException("Unsupported feature dependant: " + dependant);
    }
}
