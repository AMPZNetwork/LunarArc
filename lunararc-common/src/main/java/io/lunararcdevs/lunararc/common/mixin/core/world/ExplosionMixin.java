package io.lunararcdevs.lunararc.common.mixin.core.world;

import io.lunararcdevs.lunararc.common.bridge.ExplosionBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Explosion.class)
public abstract class ExplosionMixin implements ExplosionBridge {

    @Shadow @Final private Level level;
    @Shadow @Final private double x;
    @Shadow @Final private double y;
    @Shadow @Final private double z;
    @Shadow @Final private Entity source;

    // Accessors rather than field shadows: toBlow is declared as a fastutil ObjectArrayList and
    // radius as a private field, and a shadow has to match the declared type exactly. Both public
    // methods are part of the vanilla surface the loaders' own explosion events already use.
    @Shadow public abstract List<BlockPos> getToBlow();

    @Shadow public abstract float radius();

    @Shadow public abstract Explosion.BlockInteraction getBlockInteraction();

    // Computed on first read rather than in <init>: Explosion has several constructors that
    // delegate to one another, so an <init> injection would run more than once per explosion.
    @Unique private Float lunararc$yield;

    @Override
    public float lunararc$getYield() {
        if (this.lunararc$yield == null) {
            // CraftBukkit's own initializer, kept verbatim: a decaying explosion reports the same
            // fraction vanilla would have dropped, so an untouched explosion looks unchanged.
            this.lunararc$yield = this.getBlockInteraction() == Explosion.BlockInteraction.DESTROY_WITH_DECAY
                    ? 1.0F / this.radius()
                    : 1.0F;
        }
        return this.lunararc$yield;
    }

    @Override
    public void lunararc$setYield(float yield) {
        this.lunararc$yield = yield;
    }

    @Inject(method = "finalizeExplosion", at = @At("HEAD"))
    private void lunararc$callExplosionEvent(boolean spawnParticles, CallbackInfo ci) {
        // Vanilla only touches blocks when the interaction is not KEEP, and CraftBukkit fires the
        // event inside that same branch - a KEEP explosion breaks nothing, so there is nothing for
        // a protection plugin to veto.
        if (!(this.level instanceof ServerLevel serverLevel)) return;
        if (this.getBlockInteraction() == Explosion.BlockInteraction.KEEP) return;

        List<BlockPos> toBlow = this.getToBlow();
        List<org.bukkit.block.Block> blockList = new ArrayList<>(toBlow.size());
        for (int index = toBlow.size() - 1; index >= 0; index--) {
            org.bukkit.block.Block block = CraftBlock.at(serverLevel, toBlow.get(index));
            if (!block.getType().isAir()) blockList.add(block);
        }

        boolean cancelled;
        List<org.bukkit.block.Block> remaining;
        if (this.source != null) {
            org.bukkit.event.entity.EntityExplodeEvent event = CraftEventFactory.callEntityExplodeEvent(
                    this.source, blockList, this.lunararc$getYield(), this.getBlockInteraction());
            cancelled = event.isCancelled();
            remaining = event.blockList();
            this.lunararc$setYield(event.getYield());
        } else {
            // CraftBukkit reads the pre-explosion state off the damage source here so a bed or
            // respawn anchor reports the block that actually blew up. That field is a CraftBukkit
            // addition to DamageSource which LunarArc has no bridge for, so the block's current
            // state is used instead - the same fallback CraftBukkit takes when the source carries
            // no state of its own.
            org.bukkit.block.Block block = CraftBlock.at(serverLevel, BlockPos.containing(this.x, this.y, this.z));
            org.bukkit.event.block.BlockExplodeEvent event = CraftEventFactory.callBlockExplodeEvent(
                    block, block.getState(), blockList, this.lunararc$getYield(), this.getBlockInteraction());
            cancelled = event.isCancelled();
            remaining = event.blockList();
            this.lunararc$setYield(event.getYield());
        }

        toBlow.clear();
        if (cancelled) return;
        for (org.bukkit.block.Block block : remaining) {
            toBlow.add(new BlockPos(block.getX(), block.getY(), block.getZ()));
        }
    }
}
