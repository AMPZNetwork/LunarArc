package io.lunararcdevs.lunararc.common.mixin.core.world.raid;

import io.lunararcdevs.lunararc.common.bridge.world.raid.RaidsBridge;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(Raids.class)
public abstract class RaidsMixin implements RaidsBridge {
    @Shadow private Map<Integer, Raid> raidMap;

    @Override
    public Map<Integer, Raid> lunararc$raids() {
        return this.raidMap;
    }
}
