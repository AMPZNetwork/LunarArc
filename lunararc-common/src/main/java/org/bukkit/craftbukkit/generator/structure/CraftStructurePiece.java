package org.bukkit.craftbukkit.generator.structure;

import org.bukkit.generator.structure.StructurePiece;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class CraftStructurePiece implements StructurePiece {
    private final net.minecraft.world.level.levelgen.structure.StructurePiece handle;

    public CraftStructurePiece(@NotNull net.minecraft.world.level.levelgen.structure.StructurePiece handle) {
        this.handle = Objects.requireNonNull(handle, "handle");
    }

    public @NotNull net.minecraft.world.level.levelgen.structure.StructurePiece getHandle() {
        return this.handle;
    }

    @Override
    public @NotNull BoundingBox getBoundingBox() {
        net.minecraft.world.level.levelgen.structure.BoundingBox box = this.handle.getBoundingBox();
        return new BoundingBox(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
    }
}
