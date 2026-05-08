package org.bukkit.craftbukkit.v1_21_R1.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.craftbukkit.v1_21_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.jetbrains.annotations.Nullable;

public class CraftEventFactory {
    public static EntityDamageEvent callEntityDamageEvent(LivingEntity entity, DamageSource source, float damage) {
        DamageCause cause = DamageCause.CUSTOM;

        // Map Vanilla damage sources to Bukkit causes
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALL))
            cause = DamageCause.FALL;
        else if (source.is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE))
            cause = DamageCause.FIRE_TICK;
        else if (source.is(net.minecraft.world.damagesource.DamageTypes.LAVA))
            cause = DamageCause.LAVA;
        else if (source.is(net.minecraft.world.damagesource.DamageTypes.DROWN))
            cause = DamageCause.DROWNING;
        else if (source.is(net.minecraft.world.damagesource.DamageTypes.STARVE))
            cause = DamageCause.STARVATION;
        else if (source.is(net.minecraft.world.damagesource.DamageTypes.MAGIC))
            cause = DamageCause.MAGIC;
        else if (source.is(net.minecraft.world.damagesource.DamageTypes.WITHER))
            cause = DamageCause.WITHER;

        org.bukkit.entity.Entity bukkitEntity = ((io.ampznetwork.lunararc.common.bridge.EntityBridge) entity)
                .lunararc$getBukkitEntity();
        EntityDamageEvent event = new EntityDamageEvent(bukkitEntity, cause, (org.bukkit.damage.DamageSource) null,
                (double) damage);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerInteractEvent callPlayerInteractEvent(net.minecraft.server.level.ServerPlayer player,
            Action action, @Nullable net.minecraft.core.BlockPos pos, @Nullable net.minecraft.core.Direction direction,
            @Nullable net.minecraft.world.item.ItemStack itemstack) {
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) ((io.ampznetwork.lunararc.common.bridge.EntityBridge) player)
                .lunararc$getBukkitEntity();
        if (bukkitPlayer == null)
            return null;

        ItemStack item = CraftItemStack.asBukkitCopy(itemstack);
        Block block = (pos != null) ? CraftBlock.create(player.serverLevel(), pos) : null;
        BlockFace face = (direction != null) ? BlockFace.valueOf(direction.name()) : BlockFace.SELF;

        PlayerInteractEvent event = new PlayerInteractEvent(bukkitPlayer, action, item, block, face);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static org.bukkit.event.block.BlockBreakEvent callBlockBreakEvent(
            net.minecraft.server.level.ServerLevel world, net.minecraft.core.BlockPos pos,
            net.minecraft.server.level.ServerPlayer player) {
        org.bukkit.block.Block block = org.bukkit.craftbukkit.v1_21_R1.block.CraftBlock.create(world, pos);
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) ((io.ampznetwork.lunararc.common.bridge.EntityBridge) player)
                .lunararc$getBukkitEntity();

        org.bukkit.event.block.BlockBreakEvent event = new org.bukkit.event.block.BlockBreakEvent(block, bukkitPlayer);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static org.bukkit.event.block.BlockPlaceEvent callBlockPlaceEvent(
            net.minecraft.server.level.ServerLevel world, net.minecraft.core.BlockPos pos,
            net.minecraft.server.level.ServerPlayer player, net.minecraft.world.InteractionHand hand,
            net.minecraft.world.level.block.state.BlockState newState) {
        org.bukkit.block.Block block = org.bukkit.craftbukkit.v1_21_R1.block.CraftBlock.create(world, pos);

        org.bukkit.entity.Player bukkitPlayer = player != null
                ? (org.bukkit.entity.Player) ((io.ampznetwork.lunararc.common.bridge.EntityBridge) player)
                        .lunararc$getBukkitEntity()
                : null;
        ItemStack item = player != null ? CraftItemStack.asBukkitCopy(player.getItemInHand(hand)) : null;

        org.bukkit.event.block.BlockPlaceEvent event = new org.bukkit.event.block.BlockPlaceEvent(block,
                block.getState(), block, item, bukkitPlayer, true, org.bukkit.inventory.EquipmentSlot.HAND);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
}
