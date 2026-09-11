package io.lunararcdevs.lunararc.common.mixin.core.world;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.bossbar.BossBar;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ported from the real PaperMC/Paper-archive ver/1.21.1 patch to {@code net.minecraft.world.BossEvent}
 * (patches/server/0010-Adventure.patch). Real Paper adds a public {@code adventure} field directly to the
 * vanilla class via source patch; LunarArc can't source-patch vanilla, so this is the same field + the same
 * delegation logic expressed as a Mixin instead. Every getter/setter below mirrors the real patch's
 * "if (this.adventure != null) delegate to it" behavior line for line.
 */
@Mixin(BossEvent.class)
public abstract class BossEventMixin implements io.lunararcdevs.lunararc.common.bridge.BossEventBridge {

    @Unique
    public BossBar lunararc$adventure;

    @Override
    public BossBar lunararc$getAdventureBossBar() {
        return this.lunararc$adventure;
    }

    @Override
    public void lunararc$setAdventureBossBar(BossBar bar) {
        this.lunararc$adventure = bar;
    }

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void lunararc$getName(CallbackInfoReturnable<Component> cir) {
        if (this.lunararc$adventure != null) {
            cir.setReturnValue(PaperAdventure.asVanilla(this.lunararc$adventure.name()));
        }
    }

    @Inject(method = "setName", at = @At("HEAD"))
    private void lunararc$setName(Component name, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (this.lunararc$adventure != null) {
            this.lunararc$adventure.name(PaperAdventure.asAdventure(name));
        }
    }

    @Inject(method = "getProgress", at = @At("HEAD"), cancellable = true)
    private void lunararc$getProgress(CallbackInfoReturnable<Float> cir) {
        if (this.lunararc$adventure != null) {
            cir.setReturnValue(this.lunararc$adventure.progress());
        }
    }

    @Inject(method = "setProgress", at = @At("HEAD"))
    private void lunararc$setProgress(float percent, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (this.lunararc$adventure != null) {
            this.lunararc$adventure.progress(percent);
        }
    }

    @Inject(method = "getColor", at = @At("HEAD"), cancellable = true)
    private void lunararc$getColor(CallbackInfoReturnable<BossEvent.BossBarColor> cir) {
        if (this.lunararc$adventure != null) {
            cir.setReturnValue(PaperAdventure.asVanilla(this.lunararc$adventure.color()));
        }
    }

    @Inject(method = "setColor", at = @At("HEAD"))
    private void lunararc$setColor(BossEvent.BossBarColor color, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (this.lunararc$adventure != null) {
            this.lunararc$adventure.color(PaperAdventure.asAdventure(color));
        }
    }

    @Inject(method = "getOverlay", at = @At("HEAD"), cancellable = true)
    private void lunararc$getOverlay(CallbackInfoReturnable<BossEvent.BossBarOverlay> cir) {
        if (this.lunararc$adventure != null) {
            cir.setReturnValue(PaperAdventure.asVanilla(this.lunararc$adventure.overlay()));
        }
    }

    @Inject(method = "setOverlay", at = @At("HEAD"))
    private void lunararc$setOverlay(BossEvent.BossBarOverlay style, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (this.lunararc$adventure != null) {
            this.lunararc$adventure.overlay(PaperAdventure.asAdventure(style));
        }
    }

    @Inject(method = "shouldDarkenScreen", at = @At("HEAD"), cancellable = true)
    private void lunararc$shouldDarkenScreen(CallbackInfoReturnable<Boolean> cir) {
        if (this.lunararc$adventure != null) {
            cir.setReturnValue(this.lunararc$adventure.hasFlag(BossBar.Flag.DARKEN_SCREEN));
        }
    }

    // The three flag setters return BossEvent for chaining while the four setters above return
    // void, so Mixin requires CallbackInfoReturnable here and rejects CallbackInfo outright -
    // "Invalid descriptor ... CallbackInfoReturnable is required!" at apply time, which killed the
    // server as BossEvent loaded during EndDragonFight construction. These only observe, so the
    // callback is never used to set a return value; the field write still falls through to vanilla.
    @Inject(method = "setDarkenScreen", at = @At("HEAD"))
    private void lunararc$setDarkenScreen(boolean darkenSky, CallbackInfoReturnable<BossEvent> cir) {
        if (this.lunararc$adventure != null) {
            PaperAdventure.setFlag(this.lunararc$adventure, BossBar.Flag.DARKEN_SCREEN, darkenSky);
        }
    }

    @Inject(method = "shouldPlayBossMusic", at = @At("HEAD"), cancellable = true)
    private void lunararc$shouldPlayBossMusic(CallbackInfoReturnable<Boolean> cir) {
        if (this.lunararc$adventure != null) {
            cir.setReturnValue(this.lunararc$adventure.hasFlag(BossBar.Flag.PLAY_BOSS_MUSIC));
        }
    }

    @Inject(method = "setPlayBossMusic", at = @At("HEAD"))
    private void lunararc$setPlayBossMusic(boolean dragonMusic, CallbackInfoReturnable<BossEvent> cir) {
        if (this.lunararc$adventure != null) {
            PaperAdventure.setFlag(this.lunararc$adventure, BossBar.Flag.PLAY_BOSS_MUSIC, dragonMusic);
        }
    }

    @Inject(method = "shouldCreateWorldFog", at = @At("HEAD"), cancellable = true)
    private void lunararc$shouldCreateWorldFog(CallbackInfoReturnable<Boolean> cir) {
        if (this.lunararc$adventure != null) {
            cir.setReturnValue(this.lunararc$adventure.hasFlag(BossBar.Flag.CREATE_WORLD_FOG));
        }
    }

    @Inject(method = "setCreateWorldFog", at = @At("HEAD"))
    private void lunararc$setCreateWorldFog(boolean thickenFog, CallbackInfoReturnable<BossEvent> cir) {
        if (this.lunararc$adventure != null) {
            PaperAdventure.setFlag(this.lunararc$adventure, BossBar.Flag.CREATE_WORLD_FOG, thickenFog);
        }
    }
}
