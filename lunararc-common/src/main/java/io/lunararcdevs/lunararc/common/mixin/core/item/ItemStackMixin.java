package io.lunararcdevs.lunararc.common.mixin.core.item;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.ItemStackBridge;
import io.lunararcdevs.lunararc.common.LunarArcServerAccess;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackBridge {

    @WrapMethod(
            method = "save(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;",
            require = 0)
    private Tag lunararc$saveWithRegistry(
            HolderLookup.Provider provider, Tag tag, Operation<Tag> original) {
        if (provider == null) {
            net.minecraft.server.MinecraftServer server = LunarArcServerAccess.getMinecraftServer();
            if (server != null) provider = server.registryAccess();
        }
        return original.call(provider, tag);
    }

    @WrapMethod(method = "save(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/Tag;")
    private Tag lunararc$saveWithRegistry(HolderLookup.Provider provider, Operation<Tag> original) {
        if (provider == null) provider = LunarArcServerAccess.getMinecraftServer().registryAccess();
        return original.call(provider);
    }

    @WrapMethod(method = "saveOptional(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/Tag;")
    private Tag lunararc$saveOptionalWithRegistry(HolderLookup.Provider provider, Operation<Tag> original) {
        if (provider == null) provider = LunarArcServerAccess.getMinecraftServer().registryAccess();
        return original.call(provider);
    }

    @Override
    public void lunararc$hurtAndBreak(int amount, LivingEntity owner, EquipmentSlot slot, boolean force) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!(owner.level() instanceof ServerLevel level) || !stack.isDamageableItem()) {
            return;
        }
        if (!force && owner instanceof ServerPlayer player && player.hasInfiniteMaterials()) {
            return;
        }

        int damage = amount > 0 ? EnchantmentHelper.processDurabilityChange(level, stack, amount) : amount;
        if (damage > 0 && owner instanceof ServerPlayer player) {
            PlayerItemDamageEvent event = new PlayerItemDamageEvent(
                    (org.bukkit.entity.Player) ((EntityBridge) player).lunararc$getBukkitEntity(),
                    CraftItemStack.asCraftMirror(stack), damage, amount);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            damage = event.getDamage();
        } else if (damage > 0) {
            io.papermc.paper.event.entity.EntityDamageItemEvent event =
                    new io.papermc.paper.event.entity.EntityDamageItemEvent(
                            ((EntityBridge) owner).lunararc$getBukkitEntity(),
                            CraftItemStack.asCraftMirror(stack), damage);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            damage = event.getDamage();
        }

        if (damage == 0) {
            return;
        }

        int newDamage = stack.getDamageValue() + damage;
        if (owner instanceof ServerPlayer player) {
            CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(player, stack, newDamage);
        }
        stack.setDamageValue(newDamage);
        if (stack.getDamageValue() < stack.getMaxDamage()) {
            return;
        }

        Item brokenItem = stack.getItem();
        if (stack.getCount() == 1 && owner instanceof ServerPlayer player) {
            PlayerItemBreakEvent event = new PlayerItemBreakEvent(
                    (org.bukkit.entity.Player) ((EntityBridge) player).lunararc$getBukkitEntity(),
                    CraftItemStack.asCraftMirror(stack));
            Bukkit.getPluginManager().callEvent(event);
        }
        stack.shrink(1);
        if (slot != null) {
            owner.onEquippedItemBroken(brokenItem, slot);
        }
    }
}
