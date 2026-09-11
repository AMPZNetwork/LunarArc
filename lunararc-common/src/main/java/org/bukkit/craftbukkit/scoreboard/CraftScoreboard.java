package org.bukkit.craftbukkit.scoreboard;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

public final class CraftScoreboard implements org.bukkit.scoreboard.Scoreboard {
    final net.minecraft.world.scores.Scoreboard board;
    boolean registeredGlobally;

    CraftScoreboard(net.minecraft.world.scores.Scoreboard board) {
        this.board = java.util.Objects.requireNonNull(board, "board");
    }

    @Override
    public CraftObjective registerNewObjective(String name, String criteria) {
        return registerNewObjective(name, criteria, name);
    }

    @Override
    public CraftObjective registerNewObjective(String name, String criteria, String displayName) {
        return registerNewObjective(name, CraftCriteria.getFromBukkit(criteria), displayName, RenderType.INTEGER);
    }

    @Override
    public CraftObjective registerNewObjective(String name, String criteria, String displayName, RenderType renderType) {
        return registerNewObjective(name, CraftCriteria.getFromBukkit(criteria), displayName, renderType);
    }

    @Override
    public CraftObjective registerNewObjective(String name, Criteria criteria, String displayName) {
        return registerNewObjective(name, criteria, displayName, RenderType.INTEGER);
    }

    @Override
    public CraftObjective registerNewObjective(String name, Criteria criteria, String displayName, RenderType renderType) {
        Preconditions.checkArgument(displayName != null, "Display name cannot be null");
        return registerNewObjective(name, criteria,
                io.lunararcdevs.lunararc.common.messaging.LunarArcComponentPipeline.legacyToAdventure(displayName), renderType);
    }

    // Paper 1.21.1 Adventure overloads.
    public CraftObjective registerNewObjective(String name, String criteria, net.kyori.adventure.text.Component displayName) {
        return registerNewObjective(name, CraftCriteria.getFromBukkit(criteria), displayName, RenderType.INTEGER);
    }

    public CraftObjective registerNewObjective(String name, String criteria, net.kyori.adventure.text.Component displayName, RenderType renderType) {
        return registerNewObjective(name, CraftCriteria.getFromBukkit(criteria), displayName, renderType);
    }

    public CraftObjective registerNewObjective(String name, Criteria criteria, net.kyori.adventure.text.Component displayName) {
        return registerNewObjective(name, criteria, displayName, RenderType.INTEGER);
    }

    public CraftObjective registerNewObjective(String name, Criteria criteria, net.kyori.adventure.text.Component displayName, RenderType renderType) {
        if (displayName == null) displayName = net.kyori.adventure.text.Component.empty();
        Preconditions.checkArgument(name != null, "Objective name cannot be null");
        Preconditions.checkArgument(criteria != null, "Criteria cannot be null");
        Preconditions.checkArgument(renderType != null, "RenderType cannot be null");
        Preconditions.checkArgument(name.length() <= Short.MAX_VALUE,
                "The name '%s' is longer than 32767 characters", name);
        Preconditions.checkArgument(this.board.getObjective(name) == null,
                "An objective of name '%s' already exists", name);
        Preconditions.checkArgument(criteria instanceof CraftCriteria,
                "Criteria must originate from the Bukkit/Paper registry");

        if (!this.registeredGlobally) {
            org.bukkit.scoreboard.ScoreboardManager manager = io.lunararcdevs.lunararc.common.LunarArcServerAccess
                    .getCraftServer().getScoreboardManager();
            if (manager instanceof CraftScoreboardManager craftManager) {
                craftManager.registerScoreboardForVanilla(this);
                this.registeredGlobally = true;
            }
        }

        CraftCriteria craftCriteria = (CraftCriteria) criteria;
        net.minecraft.world.scores.Objective nms = this.board.addObjective(
                name,
                craftCriteria.criteria,
                io.papermc.paper.adventure.PaperAdventure.asVanilla(displayName),
                CraftScoreboardTranslations.fromBukkitRender(renderType),
                true,
                null);
        return new CraftObjective(this, nms);
    }

    @Override
    public CraftObjective getObjective(String name) {
        Preconditions.checkArgument(name != null, "Objective name cannot be null");
        net.minecraft.world.scores.Objective nms = this.board.getObjective(name);
        return nms == null ? null : new CraftObjective(this, nms);
    }

    @Override
    public ImmutableSet<Objective> getObjectivesByCriteria(String criteria) {
        Preconditions.checkArgument(criteria != null, "Criteria name cannot be null");
        ImmutableSet.Builder<Objective> result = ImmutableSet.builder();
        for (net.minecraft.world.scores.Objective objective : this.board.getObjectives()) {
            CraftObjective craft = new CraftObjective(this, objective);
            if (craft.getCriteria().equals(criteria)) result.add(craft);
        }
        return result.build();
    }

    @Override
    public ImmutableSet<Objective> getObjectivesByCriteria(Criteria criteria) {
        Preconditions.checkArgument(criteria != null, "Criteria cannot be null");
        ImmutableSet.Builder<Objective> result = ImmutableSet.builder();
        for (net.minecraft.world.scores.Objective objective : this.board.getObjectives()) {
            CraftObjective craft = new CraftObjective(this, objective);
            if (craft.getTrackedCriteria().equals(criteria)) result.add(craft);
        }
        return result.build();
    }

    @Override
    public ImmutableSet<Objective> getObjectives() {
        ImmutableSet.Builder<Objective> result = ImmutableSet.builder();
        for (net.minecraft.world.scores.Objective objective : this.board.getObjectives()) {
            result.add(new CraftObjective(this, objective));
        }
        return result.build();
    }

    @Override
    public CraftObjective getObjective(DisplaySlot slot) {
        Preconditions.checkArgument(slot != null, "Display slot cannot be null");
        net.minecraft.world.scores.Objective objective = this.board.getDisplayObjective(CraftScoreboardTranslations.fromBukkitSlot(slot));
        return objective == null ? null : new CraftObjective(this, objective);
    }

    @Override
    public ImmutableSet<Score> getScores(OfflinePlayer player) {
        return getScores(getScoreHolder(player));
    }

    @Override
    public ImmutableSet<Score> getScores(String entry) {
        return getScores(getScoreHolder(entry));
    }

    private ImmutableSet<Score> getScores(ScoreHolder holder) {
        Preconditions.checkArgument(holder != null, "Entry cannot be null");
        ImmutableSet.Builder<Score> result = ImmutableSet.builder();
        for (net.minecraft.world.scores.Objective objective : this.board.getObjectives()) {
            result.add(new CraftScore(new CraftObjective(this, objective), holder));
        }
        return result.build();
    }

    @Override
    public void resetScores(OfflinePlayer player) {
        resetScores(getScoreHolder(player));
    }

    @Override
    public void resetScores(String entry) {
        resetScores(getScoreHolder(entry));
    }

    private void resetScores(ScoreHolder holder) {
        Preconditions.checkArgument(holder != null, "Entry cannot be null");
        for (net.minecraft.world.scores.Objective objective : this.board.getObjectives()) {
            this.board.resetSinglePlayerScore(holder, objective);
        }
    }

    @Override
    public CraftTeam getPlayerTeam(OfflinePlayer player) {
        Preconditions.checkArgument(player != null, "OfflinePlayer cannot be null");
        PlayerTeam team = this.board.getPlayersTeam(player.getName());
        return team == null ? null : new CraftTeam(this, team);
    }

    @Override
    public CraftTeam getEntryTeam(String entry) {
        Preconditions.checkArgument(entry != null, "Entry cannot be null");
        PlayerTeam team = this.board.getPlayersTeam(entry);
        return team == null ? null : new CraftTeam(this, team);
    }

    @Override
    public CraftTeam getTeam(String teamName) {
        Preconditions.checkArgument(teamName != null, "Team name cannot be null");
        PlayerTeam team = this.board.getPlayerTeam(teamName);
        return team == null ? null : new CraftTeam(this, team);
    }

    @Override
    public ImmutableSet<Team> getTeams() {
        ImmutableSet.Builder<Team> result = ImmutableSet.builder();
        for (PlayerTeam team : this.board.getPlayerTeams()) result.add(new CraftTeam(this, team));
        return result.build();
    }

    @Override
    public CraftTeam registerNewTeam(String name) {
        Preconditions.checkArgument(name != null, "Team name cannot be null");
        Preconditions.checkArgument(name.length() <= Short.MAX_VALUE,
                "Team name '%s' is longer than 32767 characters", name);
        Preconditions.checkArgument(this.board.getPlayerTeam(name) == null,
                "Team name '%s' is already in use", name);
        return new CraftTeam(this, this.board.addPlayerTeam(name));
    }

    @Override
    public ImmutableSet<OfflinePlayer> getPlayers() {
        ImmutableSet.Builder<OfflinePlayer> result = ImmutableSet.builder();
        for (ScoreHolder holder : this.board.getTrackedPlayers()) {
            result.add(Bukkit.getOfflinePlayer(holder.getScoreboardName()));
        }
        return result.build();
    }

    @Override
    public ImmutableSet<String> getEntries() {
        ImmutableSet.Builder<String> result = ImmutableSet.builder();
        for (ScoreHolder holder : this.board.getTrackedPlayers()) result.add(holder.getScoreboardName());
        return result.build();
    }

    @Override
    public void clearSlot(DisplaySlot slot) {
        Preconditions.checkArgument(slot != null, "Display slot cannot be null");
        this.board.setDisplayObjective(CraftScoreboardTranslations.fromBukkitSlot(slot), null);
    }

    // Paper entity scoreboard API.
    public ImmutableSet<Score> getScoresFor(org.bukkit.entity.Entity entity) {
        Preconditions.checkArgument(entity instanceof org.bukkit.craftbukkit.entity.CraftEntity,
                "Entity is not owned by LunarArc");
        return getScores(((org.bukkit.craftbukkit.entity.CraftEntity) entity).getHandle());
    }

    public void resetScoresFor(org.bukkit.entity.Entity entity) {
        Preconditions.checkArgument(entity instanceof org.bukkit.craftbukkit.entity.CraftEntity,
                "Entity is not owned by LunarArc");
        resetScores(((org.bukkit.craftbukkit.entity.CraftEntity) entity).getHandle());
    }

    public Team getEntityTeam(org.bukkit.entity.Entity entity) {
        Preconditions.checkArgument(entity instanceof org.bukkit.craftbukkit.entity.CraftEntity,
                "Entity is not owned by LunarArc");
        return getEntryTeam(((org.bukkit.craftbukkit.entity.CraftEntity) entity).getHandle().getScoreboardName());
    }

    public net.minecraft.world.scores.Scoreboard getHandle() {
        return this.board;
    }

    static ScoreHolder getScoreHolder(String entry) {
        Preconditions.checkArgument(entry != null, "Entry cannot be null");
        return () -> entry;
    }

    static ScoreHolder getScoreHolder(OfflinePlayer player) {
        Preconditions.checkArgument(player != null, "OfflinePlayer cannot be null");
        if (player instanceof CraftPlayer craftPlayer) return craftPlayer.getHandle();
        return getScoreHolder(player.getName());
    }
}
