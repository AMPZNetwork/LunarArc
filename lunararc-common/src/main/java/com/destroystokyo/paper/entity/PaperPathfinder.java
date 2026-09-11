package com.destroystokyo.paper.entity;

import io.lunararcdevs.lunararc.common.bridge.access.PathFinderAccessBridge;
import io.lunararcdevs.lunararc.common.bridge.access.PathNavigationAccessBridge;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Concrete Paper pathfinder adapter over the loader-owned Mob navigation. */
public final class PaperPathfinder implements Pathfinder {
    private net.minecraft.world.entity.Mob entity;

    public PaperPathfinder(net.minecraft.world.entity.Mob entity) {
        this.entity = Objects.requireNonNull(entity, "entity");
    }

    public void setHandle(net.minecraft.world.entity.Mob entity) {
        this.entity = Objects.requireNonNull(entity, "entity");
    }

    @Override public @NotNull Mob getEntity() {
        org.bukkit.entity.Entity bukkit = ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) (Object) entity).lunararc$getBukkitEntity();
        if (!(bukkit instanceof Mob mob)) throw new IllegalStateException("NMS Mob is not backed by Bukkit Mob");
        return mob;
    }

    @Override public void stopPathfinding() { entity.getNavigation().stop(); }
    @Override public boolean hasPath() {
        Path path = entity.getNavigation().getPath();
        return path != null && !path.isDone();
    }
    @Override public @Nullable PathResult getCurrentPath() {
        Path path = entity.getNavigation().getPath();
        return path != null && !path.isDone() ? new Result(path) : null;
    }
    @Override public @Nullable PathResult findPath(@NotNull Location loc) {
        Objects.requireNonNull(loc, "loc");
        if (loc.getWorld() != getEntity().getWorld()) throw new IllegalArgumentException("Location must be in the same world");
        Path path = entity.getNavigation().createPath(loc.getX(), loc.getY(), loc.getZ(), 0);
        return path == null ? null : new Result(path);
    }
    @Override public @Nullable PathResult findPath(@NotNull LivingEntity target) {
        Objects.requireNonNull(target, "target");
        if (!(target instanceof CraftLivingEntity craft)) throw new IllegalArgumentException("Target must be backed by LunarArc");
        if (target.getWorld() != getEntity().getWorld()) throw new IllegalArgumentException("Target must be in the same world");
        Path path = entity.getNavigation().createPath(craft.getHandle(), 0);
        return path == null ? null : new Result(path);
    }
    @Override public boolean moveTo(@NotNull PathResult result, double speed) {
        Objects.requireNonNull(result, "result");
        if (!(result instanceof Result ours)) throw new IllegalArgumentException("PathResult was not created by this LunarArc pathfinder");
        return entity.getNavigation().moveTo(ours.path, speed);
    }

    private NodeEvaluator evaluator() {
        return ((PathFinderAccessBridge) (Object) ((PathNavigationAccessBridge) (Object) entity.getNavigation()).lunararc$getPathFinder())
                .lunararc$getNodeEvaluator();
    }
    @Override public boolean canOpenDoors() { return evaluator().canOpenDoors(); }
    @Override public void setCanOpenDoors(boolean value) { evaluator().setCanOpenDoors(value); }
    @Override public boolean canPassDoors() { return evaluator().canPassDoors(); }
    @Override public void setCanPassDoors(boolean value) { evaluator().setCanPassDoors(value); }
    @Override public boolean canFloat() { return evaluator().canFloat(); }
    @Override public void setCanFloat(boolean value) { evaluator().setCanFloat(value); }

    private final class Result implements PathResult {
        private final Path path;
        private Result(Path path) { this.path = path; }
        @Override public @NotNull List<Location> getPoints() {
            List<Location> points = new ArrayList<>(path.getNodeCount());
            for (int i = 0; i < path.getNodeCount(); i++) points.add(toLocation(path.getNode(i)));
            return points;
        }
        @Override public int getNextPointIndex() { return path.getNextNodeIndex(); }
        @Override public @Nullable Location getNextPoint() {
            int index = path.getNextNodeIndex();
            return index < path.getNodeCount() ? toLocation(path.getNode(index)) : null;
        }
        @Override public @Nullable Location getFinalPoint() {
            Node end = path.getEndNode();
            return end == null ? null : toLocation(end);
        }
        @Override public boolean canReachFinalPoint() { return path.canReach(); }
    }

    private Location toLocation(Node node) {
        return new Location(getEntity().getWorld(), node.x, node.y, node.z);
    }
}
