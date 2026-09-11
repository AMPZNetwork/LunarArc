package io.lunararcdevs.lunararc.common.mixin.core.server;

import io.lunararcdevs.lunararc.common.bridge.access.ChunkMapAccessBridge;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkMap.class)
public interface ChunkMapAccessor extends ChunkMapAccessBridge {
    @Override
    @Accessor("visibleChunkMap")
    Long2ObjectLinkedOpenHashMap<ChunkHolder> lunararc$getVisibleChunkMap();

    @Override
    @Accessor("serverViewDistance")
    int lunararc$getServerViewDistance();

    @Override
    @Accessor("entityMap")
    Int2ObjectMap<ChunkMap.TrackedEntity> lunararc$getEntityMap();
}
