package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.entity.VillagerBridge;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Villager.class)
public abstract class VillagerMixin implements VillagerBridge {
    @Shadow private int villagerXp;
    @Shadow private int numberOfRestocksToday;
    @Override public int lunararc$getVillagerXp() { return this.villagerXp; }
    @Override public void lunararc$setVillagerXp(int xp) { this.villagerXp = xp; }
    @Override public int lunararc$getRestocksToday() { return this.numberOfRestocksToday; }
    @Override public void lunararc$setRestocksToday(int restocks) { this.numberOfRestocksToday = restocks; }
}
