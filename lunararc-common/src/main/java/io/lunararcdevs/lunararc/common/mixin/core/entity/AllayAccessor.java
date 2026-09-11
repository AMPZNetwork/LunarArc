package io.lunararcdevs.lunararc.common.mixin.core.entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.allay.Allay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(Allay.class)
public interface AllayAccessor extends io.lunararcdevs.lunararc.common.bridge.access.AllayAccessBridge {
    @Accessor("inventory") SimpleContainer lunararc$getInventory();
    @Accessor("jukeboxPos") BlockPos lunararc$getJukeboxPos();
    @Accessor("jukeboxPos") void lunararc$setJukeboxPos(BlockPos value);
    @Accessor("duplicationCooldown") long lunararc$getDuplicationCooldown();
    @Accessor("duplicationCooldown") void lunararc$setDuplicationCooldown(long value);
}
