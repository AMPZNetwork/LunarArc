package org.bukkit.craftbukkit.generator.structure;

import io.lunararcdevs.lunararc.common.bridge.world.StructureStartBridge;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructurePiece;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Concrete view of an NMS StructureStart.
 *
 * Structure identity, bounds and pieces are read directly from the loader-owned
 * NMS object. Bukkit persistent data is stored directly on that NMS object
 * through a targeted mixin and serialized in the structure start NBT.
 */
public final class CraftGeneratedStructure implements GeneratedStructure {
    private final net.minecraft.world.level.levelgen.structure.StructureStart handle;
    private List<StructurePiece> pieces;

    public CraftGeneratedStructure(@NotNull net.minecraft.world.level.levelgen.structure.StructureStart handle) {
        this.handle = Objects.requireNonNull(handle, "handle");
    }

    public @NotNull net.minecraft.world.level.levelgen.structure.StructureStart getHandle() {
        return this.handle;
    }

    @Override
    public @NotNull BoundingBox getBoundingBox() {
        net.minecraft.world.level.levelgen.structure.BoundingBox box = this.handle.getBoundingBox();
        return new BoundingBox(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
    }

    @Override
    public @NotNull Structure getStructure() {
        return CraftStructure.minecraftToBukkit(this.handle.getStructure());
    }

    @Override
    public @NotNull Collection<StructurePiece> getPieces() {
        if (this.pieces == null) {
            this.pieces = this.handle.getPieces().stream()
                    .map(CraftStructurePiece::new)
                    .map(StructurePiece.class::cast)
                    .toList();
        }
        return this.pieces;
    }

    @Override
    public @NotNull PersistentDataContainer getPersistentDataContainer() {
        return ((StructureStartBridge) (Object) this.handle).lunararc$getPersistentDataContainer();
    }
}
