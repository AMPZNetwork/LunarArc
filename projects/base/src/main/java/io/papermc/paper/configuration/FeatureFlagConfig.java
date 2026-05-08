package io.papermc.paper.configuration;

import java.util.Set;
import com.destroystokyo.paper.inventory.meta.ArmorStandMeta;

public interface FeatureFlagConfig {
    Set<Object> getFeatureFlags();
    boolean isFeatureFlagEnabled(Object flag);
}
