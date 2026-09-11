package org.bukkit.craftbukkit.entity;

import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.WolfVariant;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Wolf;

public final class CraftWolf extends CraftTameableAnimal implements Wolf {
    public CraftWolf(CraftServer server, net.minecraft.world.entity.animal.Wolf entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.Wolf getHandle() {
        return (net.minecraft.world.entity.animal.Wolf) this.entity;
    }

    @Override
    public boolean isAngry() {
        return getHandle().isAngry();
    }

    @Override
    public void setAngry(boolean angry) {
        if (angry) getHandle().startPersistentAngerTimer();
        else getHandle().stopBeingAngry();
    }

    @Override
    public DyeColor getCollarColor() {
        return DyeColor.getByWoolData((byte) getHandle().getCollarColor().getId());
    }

    @Override
    public void setCollarColor(DyeColor color) {
        Objects.requireNonNull(color, "color");
        getHandle().setCollarColor(net.minecraft.world.item.DyeColor.byId(color.getWoolData()));
    }

    @Override
    public boolean isWet() {
        return getHandle().isWet();
    }

    @Override
    public float getTailAngle() {
        return getHandle().getTailAngle();
    }

    @Override
    public boolean isInterested() {
        return getHandle().isInterested();
    }

    @Override
    public void setInterested(boolean interested) {
        getHandle().setIsInterested(interested);
    }

    @Override
    public Variant getVariant() {
        Holder<WolfVariant> holder = getHandle().getVariant();
        ResourceLocation location = holder.unwrapKey().orElseThrow(() -> new IllegalStateException("Wolf variant is not registry-backed")).location();
        Variant variant = Registry.WOLF_VARIANT.get(new NamespacedKey(location.getNamespace(), location.getPath()));
        if (variant == null) throw new IllegalStateException("No Bukkit wolf variant for " + location);
        return variant;
    }

    @Override
    public void setVariant(Variant variant) {
        Objects.requireNonNull(variant, "variant");
        NamespacedKey key = variant.getKey();
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey());
        ResourceKey<WolfVariant> resourceKey = ResourceKey.create(Registries.WOLF_VARIANT, location);
        Holder.Reference<WolfVariant> holder = server.getServer().registryAccess().registryOrThrow(Registries.WOLF_VARIANT)
                .getHolder(resourceKey).orElseThrow(() -> new IllegalArgumentException("Unknown wolf variant " + key));
        getHandle().setVariant(holder);
    }

    @Override
    public String toString() {
        return "CraftWolf";
    }
}
