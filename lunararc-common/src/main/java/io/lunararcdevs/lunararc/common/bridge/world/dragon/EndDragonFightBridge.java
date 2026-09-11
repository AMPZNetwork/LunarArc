package io.lunararcdevs.lunararc.common.bridge.world.dragon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.dimension.end.DragonRespawnAnimation;

import java.util.List;
import java.util.UUID;

/** Narrow CraftBukkit access mixed directly into loader-owned EndDragonFight. */
public interface EndDragonFightBridge {
    ServerLevel lunararc$level();
    UUID lunararc$dragonUUID();
    ServerBossEvent lunararc$bossEvent();
    BlockPos lunararc$portalLocation();
    boolean lunararc$previouslyKilled();
    void lunararc$previouslyKilled(boolean value);
    DragonRespawnAnimation lunararc$respawnStage();
    List<Integer> lunararc$gateways();
    List<EndCrystal> lunararc$respawnCrystals();
    BlockPattern.BlockPatternMatch lunararc$findExitPortal();
    void lunararc$spawnExitPortal(boolean active);
    void lunararc$spawnNewGateway();
    void lunararc$spawnNewGateway(BlockPos pos);
    void lunararc$respawnDragon(List<EndCrystal> crystals);
}
