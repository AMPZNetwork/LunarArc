package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.LightningBoltBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LightningBolt;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings("deprecation")
public final class CraftLightningStrike extends CraftEntity implements org.bukkit.entity.LightningStrike {
    public CraftLightningStrike(CraftServer server, LightningBolt entity) {
        super(server, entity);
    }

    @Override
    public LightningBolt getHandle() {
        return (LightningBolt) super.getHandle();
    }

    @Override
    public boolean isEffect() {
        return ((LightningBoltBridge) getHandle()).lunararc$isEffect();
    }

    @Override
    public int getFlashes() {
        return getFlashCount();
    }

    @Override
    public void setFlashes(int flashes) {
        setFlashCount(flashes);
    }

    @Override
    public int getLifeTicks() {
        return getHandle().life;
    }

    @Override
    public void setLifeTicks(int ticks) {
        getHandle().life = ticks;
    }

    @Override
    public int getFlashCount() {
        return getHandle().flashes;
    }

    @Override
    public void setFlashCount(int flashes) {
        if (flashes < 0) throw new IllegalArgumentException("flashes must be non-negative");
        getHandle().flashes = flashes;
    }

    @Override
    public @Nullable Player getCausingPlayer() {
        ServerPlayer cause = getHandle().getCause();
        return cause == null ? null : (Player) CraftEntity.getEntity(server, cause);
    }

    @Override
    public void setCausingPlayer(@Nullable Player player) {
        if (player == null) {
            getHandle().setCause(null);
            return;
        }
        if (!(player instanceof CraftPlayer craft)) {
            throw new IllegalArgumentException("player must be backed by LunarArc CraftPlayer");
        }
        getHandle().setCause(craft.getHandle());
    }

    @Override
    public @Nullable org.bukkit.entity.Entity getCausingEntity() {
        return getCausingPlayer();
    }

    @Override
    public org.bukkit.entity.LightningStrike.@NotNull Spigot spigot() {
        return new org.bukkit.entity.LightningStrike.Spigot();
    }
}
