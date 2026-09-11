package io.lunararcdevs.lunararc.common.mixin.core.world;

import io.lunararcdevs.lunararc.common.bridge.world.StructureStartBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stores Bukkit StructureStart PDC on the loader-owned NMS StructureStart.
 * The compound is serialized with the same 1.21.1 key used by CraftBukkit/Paper.
 */
@Mixin(StructureStart.class)
public abstract class StructureStartMixin implements StructureStartBridge {
    @Unique
    private final CraftPersistentDataContainer lunararc$persistentDataContainer = new CraftPersistentDataContainer();

    @Override
    public CraftPersistentDataContainer lunararc$getPersistentDataContainer() {
        return this.lunararc$persistentDataContainer;
    }

    @Inject(method = "createTag", at = @At("RETURN"))
    private void lunararc$savePersistentData(StructurePieceSerializationContext context, ChunkPos chunkPos,
            CallbackInfoReturnable<CompoundTag> cir) {
        if (!this.lunararc$persistentDataContainer.isEmpty()) {
            cir.getReturnValue().put("StructureBukkitValues", this.lunararc$persistentDataContainer.toTagCompound());
        }
    }

    @Inject(method = "loadStaticStart", at = @At("RETURN"))
    private static void lunararc$loadPersistentData(StructurePieceSerializationContext context, CompoundTag tag, long seed,
            CallbackInfoReturnable<StructureStart> cir) {
        StructureStart start = cir.getReturnValue();
        if (start == null || start == StructureStart.INVALID_START || !tag.contains("StructureBukkitValues", Tag.TAG_COMPOUND)) {
            return;
        }
        ((StructureStartBridge) (Object) start).lunararc$getPersistentDataContainer()
                .putAll(tag.getCompound("StructureBukkitValues"));
    }
}
