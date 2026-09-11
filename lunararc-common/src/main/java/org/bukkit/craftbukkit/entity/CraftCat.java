package org.bukkit.craftbukkit.entity;

import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.CatVariant;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Cat;

public final class CraftCat extends CraftTameableAnimal implements Cat {
    public CraftCat(CraftServer server, net.minecraft.world.entity.animal.Cat entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.Cat getHandle() {
        return (net.minecraft.world.entity.animal.Cat) this.entity;
    }

    @Override
    public Type getCatType() {
        Holder<CatVariant> holder = getHandle().getVariant();
        ResourceLocation location = holder.unwrapKey().orElseThrow(() -> new IllegalStateException("Cat variant is not registry-backed")).location();
        Type type = Registry.CAT_VARIANT.get(new NamespacedKey(location.getNamespace(), location.getPath()));
        if (type == null) throw new IllegalStateException("No Bukkit cat type for " + location);
        return type;
    }

    @Override
    public void setCatType(Type type) {
        Objects.requireNonNull(type, "type");
        NamespacedKey key = type.getKey();
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey());
        ResourceKey<CatVariant> resourceKey = ResourceKey.create(Registries.CAT_VARIANT, location);
        Holder.Reference<CatVariant> holder = server.getServer().registryAccess().registryOrThrow(Registries.CAT_VARIANT)
                .getHolder(resourceKey).orElseThrow(() -> new IllegalArgumentException("Unknown cat type " + key));
        getHandle().setVariant(holder);
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
    public void setLyingDown(boolean lyingDown) {
        getHandle().setLying(lyingDown);
    }

    @Override
    public boolean isLyingDown() {
        return getHandle().isLying();
    }

    @Override
    public void setHeadUp(boolean headUp) {
        getHandle().setRelaxStateOne(headUp);
    }

    @Override
    public boolean isHeadUp() {
        return getHandle().isRelaxStateOne();
    }

    @Override
    public String toString() {
        return "CraftCat";
    }
}
