package io.lunararcdevs.lunararc.common.bridge;

import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface FireworkRocketBridge {
    int lunararc$getLife();
    void lunararc$setLife(int life);
    int lunararc$getLifetime();
    void lunararc$setLifetime(int lifetime);
    @Nullable LivingEntity lunararc$getAttachedEntity();
    void lunararc$setAttachedEntity(@Nullable LivingEntity entity);
    boolean lunararc$isShotAtAngle();
    void lunararc$setShotAtAngle(boolean shotAtAngle);
    @Nullable UUID lunararc$getSpawningEntity();
    ItemStack lunararc$getItem();
    void lunararc$setItem(ItemStack item);
}
