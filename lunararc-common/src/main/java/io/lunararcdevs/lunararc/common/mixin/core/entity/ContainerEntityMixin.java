package io.lunararcdevs.lunararc.common.mixin.core.entity;

import com.destroystokyo.paper.loottable.PaperLootableInventoryData;
import io.lunararcdevs.lunararc.common.server.LunarArcLootableDataStorage;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import com.destroystokyo.paper.loottable.LootableInventory;
import org.spongepowered.asm.mixin.Mixin;

/** See {@code RandomizableContainerMixin} for the real rationale — same technique, targeting
 * ContainerEntity (minecart chests/hoppers etc.) instead of block containers. */
@Mixin(ContainerEntity.class)
public interface ContainerEntityMixin extends io.lunararcdevs.lunararc.common.bridge.ContainerEntityBridge {

    default PaperLootableInventoryData lootableData() {
        return LunarArcLootableDataStorage.get(this, PaperLootableInventoryData::new);
    }

    default LootableInventory getLootableInventory() {
        throw new UnsupportedOperationException(
                "Paper lootable event-firing integration is not implemented for ContainerEntity yet");
    }
}
