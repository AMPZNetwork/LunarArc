package org.bukkit.craftbukkit.event;

import com.mojang.datafixers.util.Pair;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;

public class CraftEventFactory {
    @SuppressWarnings("deprecation")
    public static EntityDamageEvent callEntityDamageEvent(LivingEntity entity, DamageSource source, float damage) {
        DamageCause cause = damageCause(source);

        org.bukkit.entity.Entity bukkitEntity = ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) entity)
                .lunararc$getBukkitEntity();
        org.bukkit.craftbukkit.damage.CraftDamageSource bukkitSource =
                new org.bukkit.craftbukkit.damage.CraftDamageSource(source);

        org.bukkit.entity.Entity damager = bukkitSource.getDirectEntity();
        if (damager == null) damager = bukkitSource.getCausingEntity();

        EntityDamageEvent event = damager == null
                ? new EntityDamageEvent(bukkitEntity, cause, bukkitSource, (double) damage)
                : new org.bukkit.event.entity.EntityDamageByEntityEvent(
                        damager, bukkitEntity, cause, bukkitSource, (double) damage);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    private static DamageCause damageCause(DamageSource source) {
        if (source.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)) return DamageCause.KILL;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.OUTSIDE_BORDER)) return DamageCause.WORLD_BORDER;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.CACTUS)
                || source.is(net.minecraft.world.damagesource.DamageTypes.SWEET_BERRY_BUSH)
                || source.is(net.minecraft.world.damagesource.DamageTypes.STALAGMITE)
                || source.is(net.minecraft.world.damagesource.DamageTypes.STING)) return DamageCause.CONTACT;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK)
                || source.is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK_NO_AGGRO)
                || source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)) return DamageCause.ENTITY_ATTACK;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.ARROW)
                || source.is(net.minecraft.world.damagesource.DamageTypes.TRIDENT)
                || source.is(net.minecraft.world.damagesource.DamageTypes.MOB_PROJECTILE)
                || source.is(net.minecraft.world.damagesource.DamageTypes.SPIT)
                || source.is(net.minecraft.world.damagesource.DamageTypes.WIND_CHARGE)
                || source.is(net.minecraft.world.damagesource.DamageTypes.FIREWORKS)
                || source.is(net.minecraft.world.damagesource.DamageTypes.FIREBALL)
                || source.is(net.minecraft.world.damagesource.DamageTypes.UNATTRIBUTED_FIREBALL)
                || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER_SKULL)
                || source.is(net.minecraft.world.damagesource.DamageTypes.THROWN)) return DamageCause.PROJECTILE;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)) return DamageCause.SUFFOCATION;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALL)) return DamageCause.FALL;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE)) return DamageCause.FIRE;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE)) return DamageCause.FIRE_TICK;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.CAMPFIRE)) return DamageCause.CAMPFIRE;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR)) return DamageCause.HOT_FLOOR;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.LAVA)) return DamageCause.LAVA;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.DROWN)) return DamageCause.DROWNING;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION)) return source.getDirectEntity() == null ? DamageCause.BLOCK_EXPLOSION : DamageCause.ENTITY_EXPLOSION;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_EXPLOSION)
                || source.is(net.minecraft.world.damagesource.DamageTypes.BAD_RESPAWN_POINT)) return DamageCause.ENTITY_EXPLOSION;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD)) return DamageCause.VOID;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT)) return DamageCause.LIGHTNING;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.STARVE)) return DamageCause.STARVATION;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.MAGIC)
                || source.is(net.minecraft.world.damagesource.DamageTypes.INDIRECT_MAGIC)) return DamageCause.MAGIC;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) return DamageCause.WITHER;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALLING_BLOCK)
                || source.is(net.minecraft.world.damagesource.DamageTypes.FALLING_ANVIL)
                || source.is(net.minecraft.world.damagesource.DamageTypes.FALLING_STALACTITE)) return DamageCause.FALLING_BLOCK;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.THORNS)) return DamageCause.THORNS;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.DRAGON_BREATH)) return DamageCause.DRAGON_BREATH;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FLY_INTO_WALL)) return DamageCause.FLY_INTO_WALL;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.CRAMMING)) return DamageCause.CRAMMING;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.DRY_OUT)) return DamageCause.DRYOUT;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FREEZE)) return DamageCause.FREEZE;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.SONIC_BOOM)) return DamageCause.SONIC_BOOM;
        return DamageCause.CUSTOM;
    }

    public static PlayerInteractEvent callPlayerInteractEvent(net.minecraft.server.level.ServerPlayer player,
            Action action, @Nullable net.minecraft.core.BlockPos pos, @Nullable net.minecraft.core.Direction direction,
            @Nullable net.minecraft.world.item.ItemStack itemstack) {
        return callPlayerInteractEvent(player, action, pos, direction, itemstack, org.bukkit.inventory.EquipmentSlot.HAND);
    }

    public static PlayerInteractEvent callPlayerInteractEvent(net.minecraft.server.level.ServerPlayer player,
            Action action, @Nullable net.minecraft.core.BlockPos pos, @Nullable net.minecraft.core.Direction direction,
            @Nullable net.minecraft.world.item.ItemStack itemstack, @Nullable org.bukkit.inventory.EquipmentSlot hand) {
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) player)
                .lunararc$getBukkitEntity();
        if (bukkitPlayer == null)
            return null;

        ItemStack item = CraftItemStack.asBukkitCopy(itemstack);
        Block block = (pos != null) ? CraftBlock.create(player.serverLevel(), pos) : null;
        BlockFace face = (direction != null) ? BlockFace.valueOf(direction.name()) : BlockFace.SELF;

        PlayerInteractEvent event = new PlayerInteractEvent(bukkitPlayer, action, item, block, face, hand);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }


    public static com.mojang.datafixers.util.Either<net.minecraft.world.entity.player.Player.BedSleepingProblem, net.minecraft.util.Unit> callPlayerBedEnterEvent(
            net.minecraft.server.level.ServerPlayer player, net.minecraft.core.BlockPos bed,
            com.mojang.datafixers.util.Either<net.minecraft.world.entity.player.Player.BedSleepingProblem, net.minecraft.util.Unit> nmsResult) {
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) player)
                .lunararc$getBukkitEntity();
        if (bukkitPlayer == null) return nmsResult;

        org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult result = nmsResult.map(problem -> switch (problem) {
            case NOT_POSSIBLE_HERE -> org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult.NOT_POSSIBLE_HERE;
            case NOT_POSSIBLE_NOW -> org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult.NOT_POSSIBLE_NOW;
            case TOO_FAR_AWAY -> org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult.TOO_FAR_AWAY;
            case NOT_SAFE -> org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult.NOT_SAFE;
            case OBSTRUCTED, OTHER_PROBLEM -> org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult.OTHER_PROBLEM;
        }, unit -> org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult.OK);

        var event = new org.bukkit.event.player.PlayerBedEnterEvent(
                bukkitPlayer, org.bukkit.craftbukkit.block.CraftBlock.at(player.serverLevel(), bed), result);
        Bukkit.getPluginManager().callEvent(event);
        return switch (event.useBed()) {
            case ALLOW -> com.mojang.datafixers.util.Either.right(net.minecraft.util.Unit.INSTANCE);
            case DENY -> com.mojang.datafixers.util.Either.left(net.minecraft.world.entity.player.Player.BedSleepingProblem.OTHER_PROBLEM);
            case DEFAULT -> nmsResult;
        };
    }

    public static org.bukkit.event.player.PlayerBedLeaveEvent callPlayerBedLeaveEvent(
            net.minecraft.server.level.ServerPlayer player, net.minecraft.core.BlockPos bedPos, boolean setSpawnLocation) {
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) player)
                .lunararc$getBukkitEntity();
        if (bukkitPlayer == null) return null;
        org.bukkit.block.Block bed = bedPos != null
                ? org.bukkit.craftbukkit.block.CraftBlock.at(player.serverLevel(), bedPos)
                : bukkitPlayer.getLocation().getBlock();
        var event = new org.bukkit.event.player.PlayerBedLeaveEvent(bukkitPlayer, bed, setSpawnLocation);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static org.bukkit.event.block.BlockBreakEvent callBlockBreakEvent(
            net.minecraft.server.level.ServerLevel world, net.minecraft.core.BlockPos pos,
            net.minecraft.server.level.ServerPlayer player) {
        org.bukkit.block.Block block = org.bukkit.craftbukkit.block.CraftBlock.create(world, pos);
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) player)
                .lunararc$getBukkitEntity();

        org.bukkit.event.block.BlockBreakEvent event = new org.bukkit.event.block.BlockBreakEvent(block, bukkitPlayer);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static org.bukkit.event.block.BlockPlaceEvent callBlockPlaceEvent(
            net.minecraft.server.level.ServerLevel world, net.minecraft.core.BlockPos pos,
            net.minecraft.server.level.ServerPlayer player, net.minecraft.world.InteractionHand hand,
            net.minecraft.world.level.block.state.BlockState newState) {
        org.bukkit.block.Block block = org.bukkit.craftbukkit.block.CraftBlock.create(world, pos);

        org.bukkit.entity.Player bukkitPlayer = player != null
                ? (org.bukkit.entity.Player) ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) player)
                        .lunararc$getBukkitEntity()
                : null;
        ItemStack item = player != null ? CraftItemStack.asBukkitCopy(player.getItemInHand(hand)) : null;

        org.bukkit.event.block.BlockPlaceEvent event = new org.bukkit.event.block.BlockPlaceEvent(block,
                block.getState(), block, item, bukkitPlayer, true, org.bukkit.inventory.EquipmentSlot.HAND);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Loader-native block placement bridge. Forge/NeoForge provide a snapshot of the
     * state that existed before placement; use it directly instead of calling
     * block.getState() after a modded block entity has been created. This mirrors the
     * information Paper's placement transaction supplies while keeping loader-owned NMS.
     */
    public static org.bukkit.event.block.BlockPlaceEvent callBlockPlaceEvent(
            net.minecraft.server.level.ServerLevel world, net.minecraft.core.BlockPos pos,
            net.minecraft.server.level.ServerPlayer player, net.minecraft.world.InteractionHand hand,
            net.minecraft.world.level.block.state.BlockState newState,
            net.minecraft.world.level.block.state.BlockState replacedState,
            @javax.annotation.Nullable net.minecraft.nbt.CompoundTag replacedBlockEntityTag) {
        org.bukkit.block.Block block = org.bukkit.craftbukkit.block.CraftBlock.create(world, pos);

        org.bukkit.entity.Player bukkitPlayer = player != null
                ? (org.bukkit.entity.Player) ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) player)
                        .lunararc$getBukkitEntity()
                : null;
        ItemStack item = player != null ? CraftItemStack.asBukkitCopy(player.getItemInHand(hand)) : null;

        net.minecraft.world.level.block.entity.BlockEntity replacedTileEntity = replacedBlockEntityTag == null
                ? null
                : net.minecraft.world.level.block.entity.BlockEntity.loadStatic(
                        pos, replacedState, replacedBlockEntityTag, world.registryAccess());
        org.bukkit.block.BlockState bukkitReplacedState =
                org.bukkit.craftbukkit.block.CraftBlockStates.getBlockState(
                        block.getWorld(), pos, replacedState, replacedTileEntity);

        org.bukkit.event.block.BlockPlaceEvent event = new org.bukkit.event.block.BlockPlaceEvent(
                block, bukkitReplacedState, block, item, bukkitPlayer, true,
                hand == net.minecraft.world.InteractionHand.OFF_HAND
                        ? org.bukkit.inventory.EquipmentSlot.OFF_HAND
                        : org.bukkit.inventory.EquipmentSlot.HAND);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }


    public static org.bukkit.event.entity.EntitySpawnEvent callEntitySpawnEvent(net.minecraft.world.entity.Entity entity) {
        org.bukkit.entity.Entity bukkitEntity = ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) entity).lunararc$getBukkitEntity();
        if (bukkitEntity == null) return null;
        var event = new org.bukkit.event.entity.EntitySpawnEvent(bukkitEntity);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static org.bukkit.event.entity.CreatureSpawnEvent callCreatureSpawnEvent(net.minecraft.world.entity.LivingEntity entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        org.bukkit.entity.Entity bukkitEntity = ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) entity).lunararc$getBukkitEntity();
        if (!(bukkitEntity instanceof org.bukkit.entity.LivingEntity living)) return null;
        var event = new org.bukkit.event.entity.CreatureSpawnEvent(living, reason);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }


    public static org.bukkit.event.player.PlayerJoinEvent callPlayerJoinEvent(net.minecraft.server.level.ServerPlayer player) {
        org.bukkit.entity.Player bukkitPlayer = org.bukkit.Bukkit.getPlayer(player.getUUID());
        if (bukkitPlayer == null) return null;
        var event = new org.bukkit.event.player.PlayerJoinEvent(bukkitPlayer, net.kyori.adventure.text.Component.text(player.getScoreboardName() + " joined the game"));
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }


    public static org.bukkit.event.player.PlayerQuitEvent callPlayerQuitEvent(net.minecraft.server.level.ServerPlayer player) {
        org.bukkit.entity.Player bukkitPlayer = org.bukkit.Bukkit.getPlayer(player.getUUID());
        if (bukkitPlayer == null) return null;
        var event = new org.bukkit.event.player.PlayerQuitEvent(bukkitPlayer, net.kyori.adventure.text.Component.text(player.getScoreboardName() + " left the game"));
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }


    public static org.bukkit.event.player.PlayerMoveEvent callPlayerMoveEvent(net.minecraft.server.level.ServerPlayer player, Location from, Location to) {
        org.bukkit.entity.Player bukkitPlayer = org.bukkit.Bukkit.getPlayer(player.getUUID());
        if (bukkitPlayer == null) return null;
        var event = new org.bukkit.event.player.PlayerMoveEvent(bukkitPlayer, from, to);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }


    public static org.bukkit.event.player.AsyncPlayerChatEvent callAsyncChatEvent(
            net.minecraft.server.level.ServerPlayer player, String message) {
        org.bukkit.entity.Player bukkitPlayer = org.bukkit.Bukkit.getPlayer(player.getUUID());
        if (bukkitPlayer == null) return null;
        java.util.Set<org.bukkit.entity.Player> recipients = new java.util.HashSet<>(
                (java.util.Collection<? extends org.bukkit.entity.Player>)
                        org.bukkit.Bukkit.getOnlinePlayers());
        @SuppressWarnings("deprecation")
        var event = new org.bukkit.event.player.AsyncPlayerChatEvent(false, bukkitPlayer, message, recipients);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }


    public static void callWorldLoadEvent(org.bukkit.World world) {
        var event = new org.bukkit.event.world.WorldLoadEvent(world);
        Bukkit.getPluginManager().callEvent(event);
    }


    public static void callWorldSaveEvent(org.bukkit.World world) {
        var event = new org.bukkit.event.world.WorldSaveEvent(world);
        Bukkit.getPluginManager().callEvent(event);
    }






    public static org.bukkit.event.entity.PlayerLeashEntityEvent callPlayerLeashEntityEvent(
            net.minecraft.world.entity.Entity entity,
            net.minecraft.world.entity.Entity leashHolder,
            net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand) {
        var event = new org.bukkit.event.entity.PlayerLeashEntityEvent(
                ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) entity).lunararc$getBukkitEntity(),
                ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) leashHolder).lunararc$getBukkitEntity(),
                (org.bukkit.entity.Player) ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) player).lunararc$getBukkitEntity(),
                org.bukkit.craftbukkit.CraftEquipmentSlot.getHand(hand));
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static org.bukkit.event.entity.EntityBreedEvent callEntityBreedEvent(
            net.minecraft.world.entity.LivingEntity child,
            net.minecraft.world.entity.LivingEntity mother,
            net.minecraft.world.entity.LivingEntity father,
            @Nullable net.minecraft.world.entity.LivingEntity breeder,
            @Nullable net.minecraft.world.item.ItemStack bredWith,
            int experience) {
        org.bukkit.entity.LivingEntity breederEntity = breeder == null
                ? null
                : (org.bukkit.entity.LivingEntity) ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) breeder).lunararc$getBukkitEntity();
        org.bukkit.inventory.ItemStack bredWithStack = bredWith == null || bredWith.isEmpty()
                ? null
                : org.bukkit.craftbukkit.inventory.CraftItemStack.asBukkitCopy(bredWith);
        var event = new org.bukkit.event.entity.EntityBreedEvent(
                (org.bukkit.entity.LivingEntity) ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) child).lunararc$getBukkitEntity(),
                (org.bukkit.entity.LivingEntity) ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) mother).lunararc$getBukkitEntity(),
                (org.bukkit.entity.LivingEntity) ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) father).lunararc$getBukkitEntity(),
                breederEntity, bredWithStack, experience);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static org.bukkit.event.entity.EntityPotionEffectEvent callEntityPotionEffectChangeEvent(
            net.minecraft.world.entity.LivingEntity entity,
            @Nullable net.minecraft.world.effect.MobEffectInstance oldEffect,
            @Nullable net.minecraft.world.effect.MobEffectInstance newEffect,
            org.bukkit.event.entity.EntityPotionEffectEvent.Cause cause,
            boolean willOverride) {
        org.bukkit.event.entity.EntityPotionEffectEvent.Action action =
                oldEffect == null
                        ? org.bukkit.event.entity.EntityPotionEffectEvent.Action.ADDED
                        : newEffect == null
                                ? org.bukkit.event.entity.EntityPotionEffectEvent.Action.REMOVED
                                : org.bukkit.event.entity.EntityPotionEffectEvent.Action.CHANGED;
        return callEntityPotionEffectChangeEvent(entity, oldEffect, newEffect, cause, action, willOverride);
    }

    public static org.bukkit.event.entity.EntityPotionEffectEvent callEntityPotionEffectChangeEvent(
            net.minecraft.world.entity.LivingEntity entity,
            @Nullable net.minecraft.world.effect.MobEffectInstance oldEffect,
            @Nullable net.minecraft.world.effect.MobEffectInstance newEffect,
            org.bukkit.event.entity.EntityPotionEffectEvent.Cause cause,
            org.bukkit.event.entity.EntityPotionEffectEvent.Action action,
            boolean willOverride) {
        org.bukkit.potion.PotionEffect bukkitOld = lunararc$toBukkitEffect(oldEffect);
        org.bukkit.potion.PotionEffect bukkitNew = lunararc$toBukkitEffect(newEffect);
        if (bukkitOld == null && bukkitNew == null) {
            throw new IllegalArgumentException("Old and new potion effect are both null");
        }
        Object bukkit = ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) entity).lunararc$getBukkitEntity();
        if (!(bukkit instanceof org.bukkit.entity.LivingEntity living)) {
            throw new IllegalStateException("NMS LivingEntity is not backed by a Bukkit LivingEntity: " + entity);
        }
        var event = new org.bukkit.event.entity.EntityPotionEffectEvent(
                living, bukkitOld, bukkitNew, cause, action, willOverride);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    private static @Nullable org.bukkit.potion.PotionEffect lunararc$toBukkitEffect(
            @Nullable net.minecraft.world.effect.MobEffectInstance effect) {
        if (effect == null) return null;
        net.minecraft.resources.ResourceLocation id =
                net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
        if (id == null) return null;
        org.bukkit.potion.PotionEffectType type = org.bukkit.potion.PotionEffectType.getByKey(
                new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath()));
        if (type == null) return null;
        return new org.bukkit.potion.PotionEffect(
                type, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
    }

    public static org.bukkit.event.entity.EntityDeathEvent callEntityDeathEvent(
            net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.damagesource.DamageSource source) {
        org.bukkit.entity.Entity bukkitEntity = ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) entity).lunararc$getBukkitEntity();
        if (!(bukkitEntity instanceof org.bukkit.entity.LivingEntity living)) return null;

        var event = new org.bukkit.event.entity.EntityDeathEvent(
                living, new org.bukkit.craftbukkit.damage.CraftDamageSource(source),
                new ArrayList<org.bukkit.inventory.ItemStack>());
        double maxHealth = living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) == null
                ? living.getMaxHealth()
                : living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        if (maxHealth > 0.0D) event.setReviveHealth(maxHealth);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }


    public static AbstractContainerMenu callInventoryOpenEvent(ServerPlayer player,
            AbstractContainerMenu container, InventoryView view) {
        return callInventoryOpenEventWithTitle(player, container, view, false).getSecond();
    }

    public static AbstractContainerMenu callInventoryOpenEvent(ServerPlayer player,
            AbstractContainerMenu container, InventoryView view, boolean cancelled) {
        return callInventoryOpenEventWithTitle(player, container, view, cancelled).getSecond();
    }

    public static Pair<Component, AbstractContainerMenu> callInventoryOpenEventWithTitle(
            ServerPlayer player, AbstractContainerMenu container, InventoryView view, boolean cancelled) {
        InventoryOpenEvent event = new InventoryOpenEvent(view);
        event.setCancelled(cancelled);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return Pair.of(null, null);
        }
        return Pair.of(event.titleOverride(), container);
    }


    public static void handleInventoryCloseEvent(ServerPlayer player) {
        handleInventoryCloseEvent(player, InventoryCloseEvent.Reason.UNKNOWN);
    }

    public static void handleInventoryCloseEvent(ServerPlayer player, InventoryCloseEvent.Reason reason) {
        if (player.containerMenu == player.inventoryMenu) return;
        Object bukkit = ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) player).lunararc$getBukkitEntity();
        if (!(bukkit instanceof HumanEntity human)) return;
        InventoryCloseEvent event = new InventoryCloseEvent(human.getOpenInventory(), reason);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static net.minecraft.world.item.ItemStack handleEditBookEvent(
            net.minecraft.server.level.ServerPlayer player, int slot,
            net.minecraft.world.item.ItemStack oldBook,
            net.minecraft.world.item.ItemStack proposedBook) {
        Object bukkit = ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) player).lunararc$getBukkitEntity();
        if (!(bukkit instanceof org.bukkit.entity.Player bp)) return proposedBook;
        org.bukkit.inventory.ItemStack oldBukkit = org.bukkit.craftbukkit.inventory.CraftItemStack.asBukkitCopy(oldBook);
        org.bukkit.inventory.ItemStack proposedBukkit = org.bukkit.craftbukkit.inventory.CraftItemStack.asBukkitCopy(proposedBook);
        if (!(oldBukkit.getItemMeta() instanceof org.bukkit.inventory.meta.BookMeta oldMeta)
                || !(proposedBukkit.getItemMeta() instanceof org.bukkit.inventory.meta.BookMeta newMeta)) return proposedBook;
        boolean signing = proposedBook.is(net.minecraft.world.item.Items.WRITTEN_BOOK);
        var event = new org.bukkit.event.player.PlayerEditBookEvent(
                bp, slot >= 0 && slot <= 8 ? slot : -1, oldMeta, newMeta, signing);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return oldBook;
        org.bukkit.inventory.ItemStack result = proposedBukkit.clone();
        result.setType(event.isSigning() ? org.bukkit.Material.WRITTEN_BOOK : org.bukkit.Material.WRITABLE_BOOK);
        result.setItemMeta(event.getNewBookMeta());
        return org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(result);
    }

    public static org.bukkit.event.player.PlayerBucketFillEvent callPlayerBucketFillEvent(
            net.minecraft.server.level.ServerLevel world,
            net.minecraft.server.level.ServerPlayer player,
            net.minecraft.core.BlockPos changed,
            net.minecraft.core.BlockPos clicked,
            net.minecraft.core.Direction face,
            net.minecraft.world.item.ItemStack bucketInHand,
            net.minecraft.world.item.ItemStack result,
            net.minecraft.world.InteractionHand hand) {
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) player)
                .lunararc$getBukkitEntity();
        if (bukkitPlayer == null) return null;
        org.bukkit.block.Block block = org.bukkit.craftbukkit.block.CraftBlock.at(world, changed);
        org.bukkit.block.Block clickedBlock = org.bukkit.craftbukkit.block.CraftBlock.at(world, clicked);
        org.bukkit.block.BlockFace blockFace = org.bukkit.block.BlockFace.valueOf(face.name());
        org.bukkit.inventory.EquipmentSlot slot = hand == net.minecraft.world.InteractionHand.OFF_HAND
                ? org.bukkit.inventory.EquipmentSlot.OFF_HAND : org.bukkit.inventory.EquipmentSlot.HAND;
        org.bukkit.event.player.PlayerBucketFillEvent event = new org.bukkit.event.player.PlayerBucketFillEvent(
                bukkitPlayer, block, clickedBlock, blockFace,
                org.bukkit.craftbukkit.inventory.CraftItemStack.asBukkitCopy(bucketInHand).getType(),
                org.bukkit.craftbukkit.inventory.CraftItemStack.asBukkitCopy(result), slot);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static org.bukkit.event.player.PlayerBucketEmptyEvent callPlayerBucketEmptyEvent(
            net.minecraft.server.level.ServerLevel world,
            net.minecraft.server.level.ServerPlayer player,
            net.minecraft.core.BlockPos changed,
            net.minecraft.core.BlockPos clicked,
            net.minecraft.core.Direction face,
            net.minecraft.world.item.ItemStack bucketInHand,
            net.minecraft.world.item.ItemStack result,
            net.minecraft.world.InteractionHand hand) {
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) player)
                .lunararc$getBukkitEntity();
        if (bukkitPlayer == null) return null;
        org.bukkit.block.Block block = org.bukkit.craftbukkit.block.CraftBlock.at(world, changed);
        org.bukkit.block.Block clickedBlock = org.bukkit.craftbukkit.block.CraftBlock.at(world, clicked);
        org.bukkit.block.BlockFace blockFace = org.bukkit.block.BlockFace.valueOf(face.name());
        org.bukkit.inventory.EquipmentSlot slot = hand == net.minecraft.world.InteractionHand.OFF_HAND
                ? org.bukkit.inventory.EquipmentSlot.OFF_HAND : org.bukkit.inventory.EquipmentSlot.HAND;
        org.bukkit.event.player.PlayerBucketEmptyEvent event = new org.bukkit.event.player.PlayerBucketEmptyEvent(
                bukkitPlayer, block, clickedBlock, blockFace,
                org.bukkit.craftbukkit.inventory.CraftItemStack.asBukkitCopy(bucketInHand).getType(),
                org.bukkit.craftbukkit.inventory.CraftItemStack.asBukkitCopy(result), slot);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static org.bukkit.craftbukkit.inventory.CraftInventoryView createInventoryView(
            ServerPlayer player, AbstractContainerMenu menu, net.kyori.adventure.text.Component title) {
        Object bukkit = ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) player).lunararc$getBukkitEntity();
        if (!(bukkit instanceof org.bukkit.craftbukkit.entity.CraftPlayer craftPlayer)) return null;
        org.bukkit.event.inventory.InventoryType type;
        org.bukkit.craftbukkit.inventory.CraftInventory top = null;
        if (menu instanceof net.minecraft.world.inventory.MerchantMenu merchant) {
            return new org.bukkit.craftbukkit.inventory.CraftMerchantView(craftPlayer, merchant, title);
        }
        if (menu instanceof net.minecraft.world.inventory.AnvilMenu anvil) {
            return new org.bukkit.craftbukkit.inventory.CraftAnvilView(craftPlayer, anvil, title);
        }
        if (menu instanceof net.minecraft.world.inventory.CraftingMenu crafting) {
            type = org.bukkit.event.inventory.InventoryType.WORKBENCH;
            var recipe = player.serverLevel().getRecipeManager().getRecipeFor(
                    net.minecraft.world.item.crafting.RecipeType.CRAFTING,
                    crafting.craftSlots.asCraftInput(), player.serverLevel()).orElse(null);
            org.bukkit.inventory.Inventory craftingTop = new org.bukkit.craftbukkit.inventory.CraftInventoryCrafting(
                    crafting.craftSlots, crafting.resultSlots, craftPlayer, recipe);
            return new org.bukkit.craftbukkit.inventory.CraftInventoryView(
                    craftPlayer, menu, craftingTop, craftPlayer.getInventory(), type,
                    title == null ? net.kyori.adventure.text.Component.text(type.name()) : title);
        }
        else if (menu instanceof net.minecraft.world.inventory.FurnaceMenu) type = org.bukkit.event.inventory.InventoryType.FURNACE;
        else if (menu instanceof net.minecraft.world.inventory.BlastFurnaceMenu) type = org.bukkit.event.inventory.InventoryType.BLAST_FURNACE;
        else if (menu instanceof net.minecraft.world.inventory.SmokerMenu) type = org.bukkit.event.inventory.InventoryType.SMOKER;
        else if (menu instanceof net.minecraft.world.inventory.SmithingMenu smithing) {
            type = org.bukkit.event.inventory.InventoryType.SMITHING;
            top = new org.bukkit.craftbukkit.inventory.CraftSmithingInventory(smithing, craftPlayer);
        } else if (menu instanceof net.minecraft.world.inventory.EnchantmentMenu) type = org.bukkit.event.inventory.InventoryType.ENCHANTING;
        else if (menu instanceof net.minecraft.world.inventory.BrewingStandMenu) type = org.bukkit.event.inventory.InventoryType.BREWING;
        else if (menu instanceof net.minecraft.world.inventory.BeaconMenu) type = org.bukkit.event.inventory.InventoryType.BEACON;
        else if (menu instanceof net.minecraft.world.inventory.HopperMenu) type = org.bukkit.event.inventory.InventoryType.HOPPER;
        else if (menu instanceof net.minecraft.world.inventory.ShulkerBoxMenu) type = org.bukkit.event.inventory.InventoryType.SHULKER_BOX;
        else if (menu instanceof net.minecraft.world.inventory.LecternMenu) type = org.bukkit.event.inventory.InventoryType.LECTERN;
        else if (menu instanceof net.minecraft.world.inventory.LoomMenu) type = org.bukkit.event.inventory.InventoryType.LOOM;
        else if (menu instanceof net.minecraft.world.inventory.GrindstoneMenu grindstone) {
            type = org.bukkit.event.inventory.InventoryType.GRINDSTONE;
            top = new org.bukkit.craftbukkit.inventory.CraftGrindstoneInventory(grindstone, craftPlayer);
        }
        else if (menu instanceof net.minecraft.world.inventory.CartographyTableMenu) type = org.bukkit.event.inventory.InventoryType.CARTOGRAPHY;
        else if (menu instanceof net.minecraft.world.inventory.StonecutterMenu) type = org.bukkit.event.inventory.InventoryType.STONECUTTER;
        else if (menu instanceof net.minecraft.world.inventory.ChestMenu) type = org.bukkit.event.inventory.InventoryType.CHEST;
        else type = org.bukkit.event.inventory.InventoryType.CHEST;
        return new org.bukkit.craftbukkit.inventory.CraftInventoryView(
                craftPlayer, menu, top, craftPlayer.getInventory(), type,
                title == null ? net.kyori.adventure.text.Component.text(type.name()) : title);
    }


    public static CraftPortalEvent callPortalEvent(net.minecraft.world.entity.Entity entity,
                                                    org.bukkit.Location exit,
                                                    org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause,
                                                    int searchRadius,
                                                    int creationRadius) {
        org.bukkit.entity.Entity bukkitEntity = ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) entity).lunararc$getBukkitEntity();
        org.bukkit.Location enter = bukkitEntity.getLocation();
        if (bukkitEntity instanceof org.bukkit.entity.Player player) {
            org.bukkit.event.player.PlayerPortalEvent event = new org.bukkit.event.player.PlayerPortalEvent(
                    player, enter, exit, cause, searchRadius, true, creationRadius);
            org.bukkit.Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled() || event.getTo() == null || event.getTo().getWorld() == null) return null;
            return new CraftPortalEvent(event);
        }
        org.bukkit.event.entity.EntityPortalEvent event = new org.bukkit.event.entity.EntityPortalEvent(
                bukkitEntity, enter, exit, searchRadius, true, creationRadius);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || event.getTo() == null || event.getTo().getWorld() == null || !entity.isAlive()) return null;
        return new CraftPortalEvent(event);
    }

    public static org.bukkit.event.entity.EntityExplodeEvent callEntityExplodeEvent(
            net.minecraft.world.entity.Entity entity,
            java.util.List<Block> blocks,
            float yield,
            net.minecraft.world.level.Explosion.BlockInteraction effect) {
        org.bukkit.entity.Entity bukkitEntity =
                ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) entity).lunararc$getBukkitEntity();
        org.bukkit.event.entity.EntityExplodeEvent event = new org.bukkit.event.entity.EntityExplodeEvent(
                bukkitEntity, bukkitEntity.getLocation(), blocks, yield,
                org.bukkit.craftbukkit.CraftExplosionResult.toBukkit(effect));
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static org.bukkit.event.block.BlockExplodeEvent callBlockExplodeEvent(
            Block block,
            org.bukkit.block.BlockState state,
            java.util.List<Block> blocks,
            float yield,
            net.minecraft.world.level.Explosion.BlockInteraction effect) {
        org.bukkit.event.block.BlockExplodeEvent event = new org.bukkit.event.block.BlockExplodeEvent(
                block, state, blocks, yield,
                org.bukkit.craftbukkit.CraftExplosionResult.toBukkit(effect));
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static org.bukkit.event.block.BlockIgniteEvent callBlockIgniteEvent(
            net.minecraft.world.level.Level level,
            net.minecraft.core.BlockPos ignited,
            net.minecraft.core.BlockPos source) {
        org.bukkit.World world =
                ((io.lunararcdevs.lunararc.common.bridge.LevelBridge) level).lunararc$getWorld();
        Block igniter = world.getBlockAt(source.getX(), source.getY(), source.getZ());

        org.bukkit.event.block.BlockIgniteEvent.IgniteCause cause = switch (igniter.getType()) {
            case LAVA -> org.bukkit.event.block.BlockIgniteEvent.IgniteCause.LAVA;
            case DISPENSER -> org.bukkit.event.block.BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL;
            // Fire, or anything else that is not specifically recognised, counts as spread.
            default -> org.bukkit.event.block.BlockIgniteEvent.IgniteCause.SPREAD;
        };

        org.bukkit.event.block.BlockIgniteEvent event = new org.bukkit.event.block.BlockIgniteEvent(
                world.getBlockAt(ignited.getX(), ignited.getY(), ignited.getZ()), cause, igniter);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean callEntityChangeBlockEvent(
            net.minecraft.world.entity.Entity entity,
            net.minecraft.core.BlockPos position,
            net.minecraft.world.level.block.state.BlockState newState) {
        return callEntityChangeBlockEvent(entity, position, newState, false);
    }

    /** As above, with the event pre-cancelled, which a few CraftBukkit call sites rely on. */
    public static boolean callEntityChangeBlockEvent(
            net.minecraft.world.entity.Entity entity,
            net.minecraft.core.BlockPos position,
            net.minecraft.world.level.block.state.BlockState newState,
            boolean cancelled) {
        Block block = ((io.lunararcdevs.lunararc.common.bridge.LevelBridge) entity.level())
                .lunararc$getWorld()
                .getBlockAt(position.getX(), position.getY(), position.getZ());

        org.bukkit.event.entity.EntityChangeBlockEvent event =
                new org.bukkit.event.entity.EntityChangeBlockEvent(
                        ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) entity).lunararc$getBukkitEntity(),
                        block,
                        org.bukkit.craftbukkit.block.data.CraftBlockData.fromData(newState));
        event.setCancelled(cancelled);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }


}
