package io.lunararcdevs.lunararc.common.mixin.core.world;

import com.destroystokyo.paper.loottable.PaperLootableInventoryData;
import io.lunararcdevs.lunararc.common.server.LunarArcLootableDataStorage;
import net.minecraft.world.RandomizableContainer;
import com.destroystokyo.paper.loottable.LootableInventory;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RandomizableContainer.class)
public interface RandomizableContainerMixin extends io.lunararcdevs.lunararc.common.bridge.RandomizableContainerBridge {

    default PaperLootableInventoryData lootableData() {
        return LunarArcLootableDataStorage.get(this, PaperLootableInventoryData::new);
    }

    default LootableInventory getLootableInventory() {
        throw new UnsupportedOperationException(
                "Paper lootable event-firing integration is not implemented for RandomizableContainer yet");
    }
}
