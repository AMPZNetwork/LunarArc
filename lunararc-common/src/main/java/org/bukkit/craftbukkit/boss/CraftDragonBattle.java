package org.bukkit.craftbukkit.boss;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.server.LunarArcBossBar;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.dimension.end.DragonRespawnAnimation;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.DragonBattle;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class CraftDragonBattle implements DragonBattle {
    private final EndDragonFight handle;

    public CraftDragonBattle(EndDragonFight handle) {
        this.handle = java.util.Objects.requireNonNull(handle, "handle");
    }

    public EndDragonFight getHandle() { return this.handle; }
    private CraftWorld world() {
        for (World world : org.bukkit.Bukkit.getWorlds()) {
            if (world instanceof CraftWorld craft && craft.getHandle() == this.handle.level) return craft;
        }
        throw new IllegalStateException("EndDragonFight level is not registered with CraftServer");
    }

    private static EnderCrystal bukkitCrystal(EndCrystal crystal) {
        org.bukkit.entity.Entity bukkit = ((EntityBridge) (Object) crystal).lunararc$getBukkitEntity();
        return bukkit instanceof EnderCrystal result ? result : null;
    }

    @Override
    public @Nullable EnderDragon getEnderDragon() {
        java.util.UUID id = this.handle.dragonUUID;
        if (id == null) return null;
        Entity entity = this.handle.level.getEntity(id);
        if (entity == null) return null;
        org.bukkit.entity.Entity bukkit = ((EntityBridge) (Object) entity).lunararc$getBukkitEntity();
        return bukkit instanceof EnderDragon dragon ? dragon : null;
    }

    @Override public @NotNull BossBar getBossBar() { return LunarArcBossBar.wrap(this.handle.dragonEvent); }

    @Override
    public @Nullable Location getEndPortalLocation() {
        BlockPos pos = this.handle.portalLocation;
        return pos == null ? null : new Location(world(), pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean generateEndPortal(boolean withPortals) {
        if (this.handle.portalLocation != null || this.handle.findExitPortal() != null) return false;
        this.handle.spawnExitPortal(withPortals);
        return true;
    }

    @Override public boolean hasBeenPreviouslyKilled() { return this.handle.previouslyKilled; }
    @Override public void setPreviouslyKilled(boolean previouslyKilled) { this.handle.previouslyKilled = previouslyKilled; }
    @Override public void initiateRespawn() { this.handle.tryRespawn(); }

    @Override
    public boolean initiateRespawn(@Nullable Collection<EnderCrystal> crystals) {
        if (!hasBeenPreviouslyKilled() || getRespawnPhase() != RespawnPhase.NONE) return false;
        if (this.handle.portalLocation == null && this.handle.findExitPortal() == null) {
            this.handle.spawnExitPortal(true);
        }

        List<EndCrystal> nms = new ArrayList<>();
        if (crystals != null) {
            for (EnderCrystal crystal : crystals) {
                if (crystal == null || crystal.getWorld() != world()) continue;
                if (crystal instanceof CraftEntity craft && craft.getHandle() instanceof EndCrystal endCrystal) {
                    nms.add(endCrystal);
                }
            }
        }
        this.handle.respawnDragon(nms);
        return this.handle.respawnStage != null;
    }

    @Override
    public @NotNull RespawnPhase getRespawnPhase() {
        DragonRespawnAnimation stage = this.handle.respawnStage;
        return stage == null ? RespawnPhase.NONE : RespawnPhase.values()[stage.ordinal()];
    }

    @Override
    public boolean setRespawnPhase(@NotNull RespawnPhase phase) {
        java.util.Objects.requireNonNull(phase, "phase");
        if (phase == RespawnPhase.NONE) throw new IllegalArgumentException("Respawn phase NONE cannot be set while respawning");
        if (this.handle.respawnStage == null) return false;
        this.handle.setRespawnStage(DragonRespawnAnimation.values()[phase.ordinal()]);
        return true;
    }

    @Override public void resetCrystals() { this.handle.resetSpikeCrystals(); }
    @Override public int getGatewayCount() { return 20 - this.handle.gateways.size(); }

    @Override
    public boolean spawnNewGateway() {
        if (this.handle.gateways.isEmpty()) return false;
        // Mirror vanilla EndDragonFight#spawnNewGateway() without calling its
        // private no-arg helper: consume the next gateway index and delegate to
        // the BlockPos overload used by vanilla.
        int index = this.handle.gateways.remove(this.handle.gateways.size() - 1);
        int x = net.minecraft.util.Mth.floor(96.0D * Math.cos(2.0D * (-Math.PI + (Math.PI / 20.0D) * index)));
        int z = net.minecraft.util.Mth.floor(96.0D * Math.sin(2.0D * (-Math.PI + (Math.PI / 20.0D) * index)));
        this.handle.spawnNewGateway(new BlockPos(x, 75, z));
        return true;
    }

    @Override
    public void spawnNewGateway(@NotNull io.papermc.paper.math.Position position) {
        java.util.Objects.requireNonNull(position, "position");
        this.handle.spawnNewGateway(new BlockPos(position.blockX(), position.blockY(), position.blockZ()));
    }

    @Override
    public @NotNull List<EnderCrystal> getRespawnCrystals() {
        List<EndCrystal> crystals = this.handle.respawnCrystals;
        if (crystals == null || crystals.isEmpty()) return Collections.emptyList();
        List<EnderCrystal> result = new ArrayList<>();
        for (EndCrystal crystal : crystals) {
            if (crystal == null || crystal.isRemoved() || !crystal.isAlive()) continue;
            EnderCrystal bukkit = bukkitCrystal(crystal);
            if (bukkit != null) result.add(bukkit);
        }
        return List.copyOf(result);
    }

    @Override
    public @NotNull List<EnderCrystal> getHealingCrystals() {
        List<EnderCrystal> result = new ArrayList<>();
        for (SpikeFeature.EndSpike spike : SpikeFeature.getSpikesForLevel(this.handle.level)) {
            for (EndCrystal crystal : this.handle.level.getEntitiesOfClass(EndCrystal.class, spike.getTopBoundingBox())) {
                if (crystal.isRemoved() || !crystal.isAlive()) continue;
                EnderCrystal bukkit = bukkitCrystal(crystal);
                if (bukkit != null) result.add(bukkit);
            }
        }
        return List.copyOf(result);
    }

    @Override public boolean equals(Object other) { return this == other || (other instanceof CraftDragonBattle battle && battle.handle == this.handle); }
    @Override public int hashCode() { return this.handle.hashCode(); }
    @Override public String toString() { return "CraftDragonBattle[" + world().getName() + "]"; }
}
