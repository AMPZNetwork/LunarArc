package io.lunararcdevs.lunararc.common.mixin.core.network;

import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Substitutes an obfuscated, throwaway copy of a chunk's below-height-limit sections into the
 * outgoing chunk packet - see {@link io.lunararcdevs.lunararc.common.server.LunarArcAntiXrayEngine}
 * for why this is the interception point and what it does and does not replicate from real Paper's
 * anti-xray. Vanilla's {@code extractChunkData} reads {@code chunk.getSections()} once, right here,
 * to serialise every section into the packet buffer - redirecting only that one call means every
 * other caller of {@code LevelChunk#getSections()} (physics, block placement, everything else) keeps
 * seeing the chunk's real, unobfuscated data. The temporary array is never stored anywhere and the
 * persisted chunk itself is never touched.
 */
@Mixin(ClientboundLevelChunkPacketData.class)
public abstract class ClientboundLevelChunkPacketDataMixin {

    @Redirect(
            method = "extractChunkData",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/LevelChunk;getSections()[Lnet/minecraft/world/level/chunk/LevelChunkSection;"),
            require = 0)
    private static LevelChunkSection[] lunararc$obfuscateForSend(LevelChunk chunk) {
        if (!(chunk.getLevel() instanceof ServerLevel serverLevel)) return chunk.getSections();
        return io.lunararcdevs.lunararc.common.server.LunarArcAntiXrayEngine.forLevel(serverLevel).obfuscateForSend(chunk);
    }
}
