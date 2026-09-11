package io.lunararcdevs.lunararc.common.mod.util;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.ServerLevelBridge;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;

public final class LunarArcEntityTransforms {
    private LunarArcEntityTransforms() {}

    public static <T extends Mob> @Nullable T convert(
            Mob source,
            EntityType<T> targetType,
            boolean keepEquipment,
            EntityTransformEvent.TransformReason transformReason,
            CreatureSpawnEvent.SpawnReason spawnReason,
            @Nullable Consumer<T> prepareTarget) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(targetType, "targetType");
        Objects.requireNonNull(transformReason, "transformReason");
        Objects.requireNonNull(spawnReason, "spawnReason");
        if (source.isRemoved() || !(source.level() instanceof ServerLevel level)) return null;

        T target = targetType.create(level);
        if (target == null) return null;

        target.copyPosition(source);
        target.setBaby(source.isBaby());
        target.setNoAi(source.isNoAi());
        if (source.hasCustomName()) {
            target.setCustomName(source.getCustomName());
            target.setCustomNameVisible(source.isCustomNameVisible());
        }
        if (source.isPersistenceRequired()) target.setPersistenceRequired();
        target.setInvulnerable(source.isInvulnerable());

        if (keepEquipment) {
            target.setCanPickUpLoot(source.canPickUpLoot());
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack item = source.getItemBySlot(slot);
                if (!item.isEmpty()) {
                    target.setItemSlot(slot, item.copy());
                    target.setDropChance(slot, source.getEquipmentDropChance(slot));
                }
            }
        }

        if (prepareTarget != null) prepareTarget.accept(target);

        org.bukkit.entity.Entity sourceBukkit = ((EntityBridge) source).lunararc$getBukkitEntity();
        org.bukkit.entity.Entity targetBukkit = ((EntityBridge) target).lunararc$getBukkitEntity();
        EntityTransformEvent transformEvent = new EntityTransformEvent(sourceBukkit, List.of(targetBukkit), transformReason);
        Bukkit.getPluginManager().callEvent(transformEvent);
        if (transformEvent.isCancelled()) {
            target.discard();
            return null;
        }

        if (!((ServerLevelBridge) level).lunararc$addFreshEntity(target, spawnReason)) {
            target.discard();
            return null;
        }

        if (keepEquipment) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (!source.getItemBySlot(slot).isEmpty()) source.setItemSlot(slot, ItemStack.EMPTY);
            }
        }

        if (source.isPassenger()) {
            Entity vehicle = source.getVehicle();
            source.stopRiding();
            if (vehicle != null) target.startRiding(vehicle, true);
        }

        source.discard();
        return target;
    }
}
