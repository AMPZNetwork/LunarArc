package org.bukkit.craftbukkit.v1_21_R1.inventory;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CraftItemStack extends ItemStack {
    private final net.minecraft.world.item.ItemStack handle;

    public CraftItemStack(net.minecraft.world.item.ItemStack handle) {
        this.handle = handle;
    }

    public static ItemStack asBukkitCopy(net.minecraft.world.item.ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        
        Material material = Material.AIR;
        try {
            net.minecraft.resources.ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
            material = Material.valueOf(key.getPath().toUpperCase(java.util.Locale.ROOT));
        } catch (Exception ignored) {}
        
        return new ItemStack(material, stack.getCount());
    }

    public static net.minecraft.world.item.ItemStack asNMSCopy(@Nullable ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }
        
        net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
            net.minecraft.resources.ResourceLocation.parse(stack.getType().getKey().toString())
        );
        
        if (item == null) return net.minecraft.world.item.ItemStack.EMPTY;
        
        return new net.minecraft.world.item.ItemStack(item, stack.getAmount());
    }

    public static CraftItemStack asCraftMirror(net.minecraft.world.item.ItemStack stack) {
        return new CraftItemStack(stack);
    }

    public static CraftItemStack asCraftCopy(ItemStack stack) {
        if (stack instanceof CraftItemStack) {
            return new CraftItemStack(((CraftItemStack) stack).handle.copy());
        }
        return new CraftItemStack(asNMSCopy(stack));
    }

    public net.minecraft.world.item.ItemStack getHandle() {
        return handle;
    }
}
