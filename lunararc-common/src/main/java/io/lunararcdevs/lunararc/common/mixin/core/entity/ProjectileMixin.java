package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.ProjectileBridge;
import io.lunararcdevs.lunararc.common.bridge.access.ProjectileAccessBridge;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Projectile.class)
public abstract class ProjectileMixin implements ProjectileBridge {
    @Unique private boolean lunararc$hitCancelled;

    // ProjectileBridge was declared "mixed directly into the loader-owned NMS Projectile" but
    // nothing actually implemented it anywhere in the codebase - every CraftProjectile method
    // that called projectileBridge() (getShooter, setShooter, doesBounce, canHitEntity,
    // hitEntity, ...) threw ClassCastException for every projectile in the game (confirmed live:
    // WorldGuard's damage-cause lookup crashed calling getShooter() on a plain arrow). leftOwner/
    // hasBeenShot/ownerUUID already exist as real vanilla Projectile fields - shadow those instead
    // of duplicating them; only bounce/projectileSource are genuinely new Bukkit-side state.
    @Shadow private java.util.UUID ownerUUID;
    @Shadow private boolean leftOwner;
    @Shadow private boolean hasBeenShot;

    @Unique private boolean lunararc$bounce;
    @Unique private @Nullable org.bukkit.projectiles.ProjectileSource lunararc$projectileSource;

    @Override public boolean lunararc$hasLeftShooter() { return this.leftOwner; }
    @Override public void lunararc$setHasLeftShooter(boolean value) { this.leftOwner = value; }
    @Override public boolean lunararc$hasBeenShot() { return this.hasBeenShot; }
    @Override public void lunararc$setHasBeenShot(boolean value) { this.hasBeenShot = value; }
    @Override public boolean lunararc$doesBounce() { return this.lunararc$bounce; }
    @Override public void lunararc$setBounce(boolean value) { this.lunararc$bounce = value; }
    @Override public @Nullable java.util.UUID lunararc$getOwnerUUID() { return this.ownerUUID; }
    @Override public @Nullable org.bukkit.projectiles.ProjectileSource lunararc$getProjectileSource() { return this.lunararc$projectileSource; }
    @Override public void lunararc$setProjectileSource(@Nullable org.bukkit.projectiles.ProjectileSource source) { this.lunararc$projectileSource = source; }

    @Override
    public boolean lunararc$canHitEntity(net.minecraft.world.entity.Entity entity) {
        return ((ProjectileAccessBridge) this).lunararc$invokeCanHitEntity(entity);
    }

    @Override
    public void lunararc$hitEntity(net.minecraft.world.entity.Entity entity, @Nullable Vec3 hitPosition) {
        Vec3 position = hitPosition != null ? hitPosition : entity.position();
        ((ProjectileAccessBridge) this).lunararc$invokeOnHitEntity(new EntityHitResult(entity, position));
    }

    @Inject(method = "hitTargetOrDeflectSelf", at = @At("HEAD"), cancellable = true, require = 0)
    private void lunararc$projectileHit(HitResult hitResult, CallbackInfoReturnable<ProjectileDeflection> cir) {
        Projectile projectile = (Projectile) (Object) this;
        Object bukkit = ((EntityBridge) projectile).lunararc$getBukkitEntity();
        if (!(bukkit instanceof org.bukkit.entity.Projectile bukkitProjectile) || projectile.level().isClientSide) {
            return;
        }

        org.bukkit.entity.Entity hitEntity = null;
        org.bukkit.block.Block hitBlock = null;
        BlockFace hitFace = null;
        if (hitResult instanceof EntityHitResult entityHit) {
            hitEntity = ((EntityBridge) entityHit.getEntity()).lunararc$getBukkitEntity();
        } else if (hitResult instanceof BlockHitResult blockHit) {
            hitBlock = CraftBlock.at((net.minecraft.server.level.ServerLevel) projectile.level(), blockHit.getBlockPos());
            hitFace = org.bukkit.block.BlockFace.valueOf(blockHit.getDirection().name());
        }

        org.bukkit.event.entity.ProjectileHitEvent event =
                new org.bukkit.event.entity.ProjectileHitEvent(bukkitProjectile, hitEntity, hitBlock, hitFace);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        this.lunararc$hitCancelled = event.isCancelled();

        // Paper semantics: cancelling an entity hit prevents the collision itself.
        // Block collisions still occur, but the block-side action is suppressed below.
        if (event.isCancelled() && hitResult.getType() != HitResult.Type.BLOCK) {
            cir.setReturnValue(ProjectileDeflection.NONE);
        }
    }

    @Inject(method = "onHitBlock", at = @At("HEAD"), cancellable = true, require = 0)
    private void lunararc$cancelBlockHitAction(BlockHitResult result, CallbackInfo ci) {
        if (this.lunararc$hitCancelled) {
            this.lunararc$hitCancelled = false;
            ci.cancel();
        }
    }
}
