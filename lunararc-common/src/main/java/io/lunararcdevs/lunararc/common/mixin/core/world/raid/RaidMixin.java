package io.lunararcdevs.lunararc.common.mixin.core.world.raid;

import io.lunararcdevs.lunararc.common.bridge.world.raid.RaidBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(Raid.class)
public abstract class RaidMixin implements RaidBridge {
    @Shadow private long ticksActive;
    @Shadow public int raidOmenLevel;
    @Shadow private int numGroups;
    @Shadow @Final private Set<UUID> heroesOfTheVillage;
    @Shadow @Final private Map<Integer, Set<Raider>> groupRaiderMap;
    @Shadow @Final private ServerBossEvent raidEvent;
    @Shadow @Final private ServerLevel level;

    @Unique private static final String LUNARARC_PDC_KEY = "BukkitValues";
    @Unique private CraftPersistentDataContainer lunararc$pdc = new CraftPersistentDataContainer();

    @Inject(method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"), require = 0)
    private void lunararc$readPersistentData(ServerLevel level, CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(LUNARARC_PDC_KEY, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            this.lunararc$pdc.putAll(tag.getCompound(LUNARARC_PDC_KEY));
        }
    }

    @Inject(method = "save", at = @At("RETURN"), require = 0)
    private void lunararc$writePersistentData(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (!this.lunararc$pdc.isEmpty()) {
            tag.put(LUNARARC_PDC_KEY, this.lunararc$pdc.toTagCompound());
        }
    }

    @Override public long lunararc$activeTicks() { return this.ticksActive; }
    @Override public int lunararc$raidOmenLevel() { return this.raidOmenLevel; }
    @Override public void lunararc$raidOmenLevel(int level) { this.raidOmenLevel = level; }
    @Override public int lunararc$numGroups() { return this.numGroups; }
    @Override public Set<UUID> lunararc$heroes() { return this.heroesOfTheVillage; }
    @Override public ServerBossEvent lunararc$bossEvent() { return this.raidEvent; }
    @Override public ServerLevel lunararc$level() { return this.level; }
    @Override public CraftPersistentDataContainer lunararc$persistentData() { return this.lunararc$pdc; }

    @Override
    public Collection<Raider> lunararc$raiders() {
        Set<Raider> result = new LinkedHashSet<>();
        for (Set<Raider> group : this.groupRaiderMap.values()) result.addAll(group);
        return result;
    }
}
