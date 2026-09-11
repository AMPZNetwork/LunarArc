package org.bukkit.craftbukkit.damage;

import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;


public final class CraftDamageSourceBuilder implements DamageSource.Builder {
    private final DamageType damageType;
    private Entity causingEntity;
    private Entity directEntity;
    private Location damageLocation;

    public CraftDamageSourceBuilder(@NotNull DamageType damageType) {
        this.damageType = Objects.requireNonNull(damageType, "DamageType cannot be null");
    }

    @Override
    public DamageSource.@NotNull Builder withCausingEntity(@NotNull Entity entity) {
        this.causingEntity = Objects.requireNonNull(entity, "Entity cannot be null");
        return this;
    }

    @Override
    public DamageSource.@NotNull Builder withDirectEntity(@NotNull Entity entity) {
        this.directEntity = Objects.requireNonNull(entity, "Entity cannot be null");
        return this;
    }

    @Override
    public DamageSource.@NotNull Builder withDamageLocation(@NotNull Location location) {
        this.damageLocation = Objects.requireNonNull(location, "Location cannot be null").clone();
        return this;
    }


    @Override
    public @NotNull DamageSource build() {
        if (this.causingEntity != null && this.directEntity == null) {
            throw new IllegalArgumentException("Direct entity must be set if causing entity is set");
        }
        return CraftDamageSource.fromBukkit(
                this.damageType,
                this.causingEntity,
                this.directEntity,
                this.damageLocation
        );
    }
}
