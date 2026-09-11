package org.bukkit.craftbukkit.scoreboard;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.scoreboard.ScoreboardManager;

public final class CraftScoreboardManager implements ScoreboardManager {
    private final CraftScoreboard mainScoreboard;
    private final MinecraftServer server;
    private final Set<CraftScoreboard> scoreboards = Collections.synchronizedSet(
            Collections.newSetFromMap(new WeakHashMap<>()));
    private final Map<CraftPlayer, CraftScoreboard> playerBoards = Collections.synchronizedMap(new WeakHashMap<>());

    public CraftScoreboardManager(MinecraftServer server, net.minecraft.world.scores.Scoreboard scoreboard) {
        this.server = java.util.Objects.requireNonNull(server, "server");
        this.mainScoreboard = new CraftScoreboard(java.util.Objects.requireNonNull(scoreboard, "scoreboard"));
        this.mainScoreboard.registeredGlobally = true;
        this.scoreboards.add(this.mainScoreboard);
    }

    @Override
    public CraftScoreboard getMainScoreboard() {
        return this.mainScoreboard;
    }

    @Override
    public CraftScoreboard getNewScoreboard() {
        io.lunararcdevs.lunararc.common.util.AsyncCatcher.catchOp("scoreboard creation");
        CraftScoreboard scoreboard = new CraftScoreboard(new ServerScoreboard(this.server));
        scoreboard.registeredGlobally = true;
        this.scoreboards.add(scoreboard);
        return scoreboard;
    }

    public void registerScoreboardForVanilla(CraftScoreboard scoreboard) {
        io.lunararcdevs.lunararc.common.util.AsyncCatcher.catchOp("scoreboard registration");
        this.scoreboards.add(java.util.Objects.requireNonNull(scoreboard, "scoreboard"));
    }

    public CraftScoreboard getPlayerBoard(CraftPlayer player) {
        CraftScoreboard board = this.playerBoards.get(player);
        return board == null ? this.mainScoreboard : board;
    }

    public void setPlayerBoard(CraftPlayer player, org.bukkit.scoreboard.Scoreboard bukkitScoreboard) {
        Preconditions.checkArgument(bukkitScoreboard instanceof CraftScoreboard,
                "Cannot set player scoreboard to a scoreboard not owned by LunarArc");
        CraftScoreboard scoreboard = (CraftScoreboard) bukkitScoreboard;
        net.minecraft.world.scores.Scoreboard oldBoard = getPlayerBoard(player).getHandle();
        net.minecraft.world.scores.Scoreboard newBoard = scoreboard.getHandle();
        if (oldBoard == newBoard) return;

        if (scoreboard == this.mainScoreboard) this.playerBoards.remove(player);
        else this.playerBoards.put(player, scoreboard);

        ServerPlayer serverPlayer = player.getHandle();
        if (serverPlayer.connection == null) return;

        HashSet<Objective> removed = new HashSet<>();
        for (net.minecraft.world.scores.DisplaySlot slot : net.minecraft.world.scores.DisplaySlot.values()) {
            Objective objective = oldBoard.getDisplayObjective(slot);
            if (objective != null && removed.add(objective)) {
                serverPlayer.connection.send(new ClientboundSetObjectivePacket(objective, ClientboundSetObjectivePacket.METHOD_REMOVE));
            }
        }
        for (PlayerTeam team : oldBoard.getPlayerTeams()) {
            serverPlayer.connection.send(ClientboundSetPlayerTeamPacket.createRemovePacket(team));
        }

        if (!(newBoard instanceof ServerScoreboard serverScoreboard)) {
            throw new IllegalStateException("LunarArc plugin scoreboards must be backed by ServerScoreboard");
        }
        this.server.getPlayerList().updateEntireScoreboard(serverScoreboard, serverPlayer);
    }

    public void removePlayer(CraftPlayer player) {
        this.playerBoards.remove(player);
    }

    public void forAllObjectives(ObjectiveCriteria criteria, ScoreHolder holder, Consumer<ScoreAccess> consumer) {
        CraftScoreboard[] snapshot;
        synchronized (this.scoreboards) {
            snapshot = this.scoreboards.toArray(CraftScoreboard[]::new);
        }
        for (CraftScoreboard scoreboard : snapshot) {
            scoreboard.getHandle().forAllObjectives(criteria, holder, consumer);
        }
    }
}
