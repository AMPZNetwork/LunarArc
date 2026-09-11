package org.bukkit.craftbukkit;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.world.raid.RaidBridge;
import io.lunararcdevs.lunararc.common.server.LunarArcBossBar;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.raid.Raider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class CraftRaid implements Raid {
    private final net.minecraft.world.entity.raid.Raid handle;

    public CraftRaid(net.minecraft.world.entity.raid.Raid handle) {
        this.handle = java.util.Objects.requireNonNull(handle, "handle");
    }

    public net.minecraft.world.entity.raid.Raid getHandle() { return this.handle; }
    private RaidBridge bridge() { return (RaidBridge) (Object) this.handle; }

    @Override public boolean isStarted() { return this.handle.isStarted(); }
    @Override public long getActiveTicks() { return bridge().lunararc$activeTicks(); }
    @Override public int getBadOmenLevel() { return bridge().lunararc$raidOmenLevel(); }

    @Override
    public void setBadOmenLevel(int badOmenLevel) {
        int max = this.handle.getMaxRaidOmenLevel();
        if (badOmenLevel < 0 || badOmenLevel > max) {
            throw new IllegalArgumentException("Bad Omen level must be between 0 and " + max);
        }
        bridge().lunararc$raidOmenLevel(badOmenLevel);
    }

    @Override
    public @NotNull Location getLocation() {
        BlockPos pos = this.handle.getCenter();
        World bukkitWorld = null;
        for (World candidate : Bukkit.getWorlds()) {
            if (candidate instanceof CraftWorld craft && craft.getHandle() == bridge().lunararc$level()) {
                bukkitWorld = candidate;
                break;
            }
        }
        if (bukkitWorld == null) {
            throw new IllegalStateException("Raid level is not registered with CraftServer");
        }
        return new Location(bukkitWorld, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public @NotNull RaidStatus getStatus() {
        if (this.handle.isStopped()) return RaidStatus.STOPPED;
        if (this.handle.isVictory()) return RaidStatus.VICTORY;
        if (this.handle.isLoss()) return RaidStatus.LOSS;
        return RaidStatus.ONGOING;
    }

    @Override public int getSpawnedGroups() { return this.handle.getGroupsSpawned(); }
    @Override public int getTotalGroups() { return bridge().lunararc$numGroups() + (getBadOmenLevel() > 1 ? 1 : 0); }
    @Override public int getTotalWaves() { return bridge().lunararc$numGroups(); }
    @Override public float getTotalHealth() { return this.handle.getHealthOfLivingRaiders(); }
    @Override public @NotNull Set<UUID> getHeroes() { return Collections.unmodifiableSet(bridge().lunararc$heroes()); }

    @Override
    public @NotNull List<org.bukkit.entity.Raider> getRaiders() {
        List<org.bukkit.entity.Raider> result = new ArrayList<>();
        for (Raider raider : bridge().lunararc$raiders()) {
            Entity entity = ((EntityBridge) (Object) raider).lunararc$getBukkitEntity();
            if (entity instanceof org.bukkit.entity.Raider bukkitRaider) result.add(bukkitRaider);
        }
        return List.copyOf(result);
    }

    @Override public int getId() { return this.handle.getId(); }
    @Override public @NotNull BossBar getBossBar() { return LunarArcBossBar.wrap(bridge().lunararc$bossEvent()); }
    @Override public @NotNull org.bukkit.persistence.PersistentDataContainer getPersistentDataContainer() { return bridge().lunararc$persistentData(); }

    @Override public boolean equals(Object other) { return this == other || (other instanceof CraftRaid raid && this.handle.equals(raid.handle)); }
    @Override public int hashCode() { return this.handle.hashCode(); }
    @Override public String toString() { return "CraftRaid[" + getId() + "]"; }
}
