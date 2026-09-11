package org.bukkit.craftbukkit.scoreboard;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.RenderType;

public final class CraftCriteria implements Criteria {
    static final Map<String, CraftCriteria> DEFAULTS;
    static final CraftCriteria DUMMY;

    static {
        ImmutableMap.Builder<String, CraftCriteria> defaults = ImmutableMap.builder();
        for (Map.Entry<String, ObjectiveCriteria> entry : ObjectiveCriteria.CRITERIA_CACHE.entrySet()) {
            defaults.put(entry.getKey(), new CraftCriteria(entry.getValue()));
        }
        DEFAULTS = defaults.build();
        DUMMY = java.util.Objects.requireNonNull(DEFAULTS.get("dummy"), "Minecraft dummy scoreboard criteria");
    }

    final ObjectiveCriteria criteria;
    final String bukkitName;

    private CraftCriteria(String bukkitName) {
        this.bukkitName = bukkitName;
        this.criteria = DUMMY.criteria;
    }

    private CraftCriteria(ObjectiveCriteria criteria) {
        this.criteria = java.util.Objects.requireNonNull(criteria, "criteria");
        this.bukkitName = criteria.getName();
    }

    @Override
    public String getName() {
        return this.bukkitName;
    }

    @Override
    public boolean isReadOnly() {
        return this.criteria.isReadOnly();
    }

    @Override
    public RenderType getDefaultRenderType() {
        return CraftScoreboardTranslations.toBukkitRender(this.criteria.getDefaultRenderType());
    }

    public static CraftCriteria getFromNMS(ObjectiveCriteria criteria) {
        CraftCriteria known = DEFAULTS.get(criteria.getName());
        return known != null ? known : new CraftCriteria(criteria);
    }

    public static CraftCriteria getFromNMS(Objective objective) {
        return getFromNMS(objective.getCriteria());
    }

    public static CraftCriteria getFromBukkit(String name) {
        java.util.Objects.requireNonNull(name, "name");
        CraftCriteria known = DEFAULTS.get(name);
        if (known != null) return known;
        return ObjectiveCriteria.byName(name).map(CraftCriteria::new).orElseGet(() -> new CraftCriteria(name));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CraftCriteria that && this.bukkitName.equals(that.bukkitName);
    }

    @Override
    public int hashCode() {
        return this.bukkitName.hashCode() ^ CraftCriteria.class.hashCode();
    }
}
