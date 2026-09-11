package io.lunararcdevs.lunararc.common.bridge;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface ItemStackBridge {
    void lunararc$hurtAndBreak(int amount, LivingEntity owner, @Nullable EquipmentSlot slot, boolean force);
}
