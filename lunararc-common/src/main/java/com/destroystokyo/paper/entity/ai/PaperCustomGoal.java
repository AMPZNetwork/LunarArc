package com.destroystokyo.paper.entity.ai;

import org.bukkit.entity.Mob;

/** Bridges a plugin-defined Paper goal directly into the loader-owned NMS GoalSelector. */
public final class PaperCustomGoal<T extends Mob> extends net.minecraft.world.entity.ai.goal.Goal {
    private final Goal<T> handle;

    public PaperCustomGoal(Goal<T> handle) {
        this.handle = java.util.Objects.requireNonNull(handle, "handle");
        this.setFlags(MobGoalHelper.paperToVanilla(handle.getTypes()));
    }

    @Override public boolean canUse() { return handle.shouldActivate(); }
    @Override public boolean canContinueToUse() { return handle.shouldStayActive(); }
    @Override public void start() { handle.start(); }
    @Override public void stop() { handle.stop(); }
    @Override public void tick() { handle.tick(); }

    public Goal<T> getHandle() { return handle; }
    public GoalKey<T> getKey() { return handle.getKey(); }
}
