package io.lunararcdevs.lunararc.common.bridge.world.raid;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.raid.Raider;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/** Narrow CraftBukkit state access mixed directly into the loader-owned Raid. */
public interface RaidBridge {
    long lunararc$activeTicks();
    int lunararc$raidOmenLevel();
    void lunararc$raidOmenLevel(int level);
    int lunararc$numGroups();
    Set<UUID> lunararc$heroes();
    Collection<Raider> lunararc$raiders();
    ServerBossEvent lunararc$bossEvent();
    ServerLevel lunararc$level();
    CraftPersistentDataContainer lunararc$persistentData();
}
