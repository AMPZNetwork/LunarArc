package org.bukkit.craftbukkit.inventory;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.Nullable;

/** Concrete armor trim metadata over Minecraft 1.21.1 TRIM. */
public class CraftMetaArmor extends CraftItemMeta implements ArmorMeta {
    private ArmorTrim trim;
    public CraftMetaArmor() { super(); }
    public CraftMetaArmor(ItemStack nms) {
        super(nms);
        if (nms == null || nms.isEmpty()) return;
        net.minecraft.world.item.armortrim.ArmorTrim nmsTrim = nms.get(DataComponents.TRIM);
        if (nmsTrim != null) {
            this.trim = new ArmorTrim(toBukkitMaterial(nmsTrim.material()), toBukkitPattern(nmsTrim.pattern()));
        }
    }
    @Override public void applyToNms(ItemStack nms) {
        super.applyToNms(nms);
        if (this.trim == null) { nms.remove(DataComponents.TRIM); return; }
        nms.set(DataComponents.TRIM, new net.minecraft.world.item.armortrim.ArmorTrim(toNmsMaterial(this.trim.getMaterial()), toNmsPattern(this.trim.getPattern())));
    }
    private static TrimMaterial toBukkitMaterial(Holder<net.minecraft.world.item.armortrim.TrimMaterial> holder) {
        ResourceLocation id = holder.unwrapKey().orElseThrow().location();
        org.bukkit.Registry<TrimMaterial> registry = org.bukkit.Registry.TRIM_MATERIAL;
        TrimMaterial result = registry.get(new NamespacedKey(id.getNamespace(), id.getPath()));
        if (result == null) throw new IllegalArgumentException("Unknown trim material " + id);
        return result;
    }
    private static TrimPattern toBukkitPattern(Holder<net.minecraft.world.item.armortrim.TrimPattern> holder) {
        ResourceLocation id = holder.unwrapKey().orElseThrow().location();
        TrimPattern result = org.bukkit.Registry.TRIM_PATTERN.get(new NamespacedKey(id.getNamespace(), id.getPath()));
        if (result == null) throw new IllegalArgumentException("Unknown trim pattern " + id);
        return result;
    }
    private static Holder<net.minecraft.world.item.armortrim.TrimMaterial> toNmsMaterial(TrimMaterial material) {
        Registry<net.minecraft.world.item.armortrim.TrimMaterial> registry = ((org.bukkit.craftbukkit.CraftServer) org.bukkit.Bukkit.getServer()).getServer().registryAccess().registryOrThrow(Registries.TRIM_MATERIAL);
        NamespacedKey key = material.getKey();
        ResourceKey<net.minecraft.world.item.armortrim.TrimMaterial> resourceKey = ResourceKey.create(Registries.TRIM_MATERIAL, ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey()));
        return registry.getHolder(resourceKey).orElseThrow(() -> new IllegalArgumentException("Unknown trim material " + key));
    }
    private static Holder<net.minecraft.world.item.armortrim.TrimPattern> toNmsPattern(TrimPattern pattern) {
        Registry<net.minecraft.world.item.armortrim.TrimPattern> registry = ((org.bukkit.craftbukkit.CraftServer) org.bukkit.Bukkit.getServer()).getServer().registryAccess().registryOrThrow(Registries.TRIM_PATTERN);
        NamespacedKey key = pattern.getKey();
        ResourceKey<net.minecraft.world.item.armortrim.TrimPattern> resourceKey = ResourceKey.create(Registries.TRIM_PATTERN, ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey()));
        return registry.getHolder(resourceKey).orElseThrow(() -> new IllegalArgumentException("Unknown trim pattern " + key));
    }
    @Override public boolean hasTrim() { return this.trim != null; }
    @Override public @Nullable ArmorTrim getTrim() { return this.trim; }
    @Override public void setTrim(@Nullable ArmorTrim trim) { this.trim = trim; }
    @Override public CraftMetaArmor clone() { return (CraftMetaArmor) super.clone(); }
}
