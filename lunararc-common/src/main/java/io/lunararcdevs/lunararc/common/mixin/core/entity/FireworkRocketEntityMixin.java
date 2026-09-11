package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.FireworkRocketBridge;
import java.util.OptionalInt;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Direct Paper/CraftBukkit state access on the real loader-owned firework entity.
 * Vanilla 1.21.1 fields are shadowed directly; only Paper's spawning-entity UUID
 * is added as targeted mixin state.
 */
@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin implements FireworkRocketBridge {
    @Shadow private int life;
    @Shadow public int lifetime;
    @Shadow @Nullable public LivingEntity attachedToEntity;
    @Shadow @Final private static EntityDataAccessor<ItemStack> DATA_ID_FIREWORKS_ITEM;
    @Shadow @Final private static EntityDataAccessor<OptionalInt> DATA_ATTACHED_TO_TARGET;
    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_SHOT_AT_ANGLE;

    @Unique private @Nullable UUID lunararc$spawningEntity;

    @Override public int lunararc$getLife() { return this.life; }
    @Override public void lunararc$setLife(int life) { this.life = life; }
    @Override public int lunararc$getLifetime() { return this.lifetime; }
    @Override public void lunararc$setLifetime(int lifetime) { this.lifetime = lifetime; }

    @Override
    public @Nullable LivingEntity lunararc$getAttachedEntity() {
        return this.attachedToEntity;
    }

    @Override
    public void lunararc$setAttachedEntity(@Nullable LivingEntity entity) {
        this.attachedToEntity = entity;
        ((FireworkRocketEntity) (Object) this).getEntityData().set(
                DATA_ATTACHED_TO_TARGET,
                entity == null ? OptionalInt.empty() : OptionalInt.of(entity.getId())
        );
    }

    @Override
    public boolean lunararc$isShotAtAngle() {
        return ((FireworkRocketEntity) (Object) this).getEntityData().get(DATA_SHOT_AT_ANGLE);
    }

    @Override
    public void lunararc$setShotAtAngle(boolean shotAtAngle) {
        ((FireworkRocketEntity) (Object) this).getEntityData().set(DATA_SHOT_AT_ANGLE, shotAtAngle);
    }

    @Override public @Nullable UUID lunararc$getSpawningEntity() { return this.lunararc$spawningEntity; }

    @Override
    public ItemStack lunararc$getItem() {
        return ((FireworkRocketEntity) (Object) this).getItem();
    }

    @Override
    public void lunararc$setItem(ItemStack item) {
        ((FireworkRocketEntity) (Object) this).getEntityData().set(DATA_ID_FIREWORKS_ITEM, item);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"), require = 0)
    private void lunararc$savePaperSpawningEntity(CompoundTag tag, CallbackInfo ci) {
        if (this.lunararc$spawningEntity != null) {
            tag.putUUID("SpawningEntity", this.lunararc$spawningEntity);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"), require = 0)
    private void lunararc$loadPaperSpawningEntity(CompoundTag tag, CallbackInfo ci) {
        this.lunararc$spawningEntity = tag.hasUUID("SpawningEntity") ? tag.getUUID("SpawningEntity") : null;
    }
}
