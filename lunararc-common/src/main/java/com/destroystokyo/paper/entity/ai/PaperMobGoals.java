package com.destroystokyo.paper.entity.ai;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.entity.Mob;

/** Concrete Paper MobGoals implementation over the real NMS goal/target selectors. */
public final class PaperMobGoals implements MobGoals {
    @Override
    public <T extends Mob> void addGoal(T mob, int priority, Goal<T> goal) {
        java.util.Objects.requireNonNull(goal, "goal");
        CraftMob craftMob = requireCraftMob(mob);
        net.minecraft.world.entity.ai.goal.Goal nms = goal instanceof PaperVanillaGoal<?> vanilla
                ? vanilla.getHandle()
                : new PaperCustomGoal<>(goal);
        selector(craftMob, goal.getTypes()).addGoal(priority, nms);
    }

    @Override
    public <T extends Mob> void removeGoal(T mob, Goal<T> goal) {
        java.util.Objects.requireNonNull(goal, "goal");
        CraftMob craftMob = requireCraftMob(mob);
        if (goal instanceof PaperVanillaGoal<?> vanilla) {
            removeFromBoth(craftMob, vanilla.getHandle());
            return;
        }
        for (GoalSelector selector : selectors(craftMob)) {
            Set<net.minecraft.world.entity.ai.goal.Goal> remove = Collections.newSetFromMap(new IdentityHashMap<>());
            for (WrappedGoal wrapped : selector.getAvailableGoals()) {
                if (wrapped.getGoal() instanceof PaperCustomGoal<?> custom && custom.getHandle() == goal) {
                    remove.add(wrapped.getGoal());
                }
            }
            remove.forEach(selector::removeGoal);
        }
    }

    @Override public <T extends Mob> void removeAllGoals(T mob) {
        CraftMob craftMob = requireCraftMob(mob);
        for (GoalSelector selector : selectors(craftMob)) {
            Set<net.minecraft.world.entity.ai.goal.Goal> remove = Collections.newSetFromMap(new IdentityHashMap<>());
            for (WrappedGoal wrapped : selector.getAvailableGoals()) remove.add(wrapped.getGoal());
            remove.forEach(selector::removeGoal);
        }
    }

    @Override public <T extends Mob> void removeAllGoals(T mob, GoalType type) {
        for (Goal<T> goal : getAllGoals(mob, type)) removeGoal(mob, goal);
    }

    @Override public <T extends Mob> void removeGoal(T mob, GoalKey<T> key) {
        for (Goal<T> goal : getGoals(mob, key)) removeGoal(mob, goal);
    }

    @Override public <T extends Mob> boolean hasGoal(T mob, GoalKey<T> key) { return getGoal(mob, key) != null; }

    @Override public <T extends Mob> Goal<T> getGoal(T mob, GoalKey<T> key) {
        java.util.Objects.requireNonNull(key, "key");
        for (Goal<T> goal : getAllGoals(mob)) if (goal.getKey().equals(key)) return goal;
        return null;
    }

    @Override public <T extends Mob> Collection<Goal<T>> getGoals(T mob, GoalKey<T> key) {
        java.util.Objects.requireNonNull(key, "key");
        Set<Goal<T>> result = new LinkedHashSet<>();
        for (Goal<T> goal : getAllGoals(mob)) if (goal.getKey().equals(key)) result.add(goal);
        return result;
    }

    @Override public <T extends Mob> Collection<Goal<T>> getAllGoals(T mob) {
        return collect(requireCraftMob(mob), null, false, false);
    }

    @Override public <T extends Mob> Collection<Goal<T>> getAllGoals(T mob, GoalType type) {
        java.util.Objects.requireNonNull(type, "type");
        return collect(requireCraftMob(mob), type, false, false);
    }

    @Override public <T extends Mob> Collection<Goal<T>> getAllGoalsWithout(T mob, GoalType type) {
        java.util.Objects.requireNonNull(type, "type");
        return collect(requireCraftMob(mob), type, false, true);
    }

    @Override public <T extends Mob> Collection<Goal<T>> getRunningGoals(T mob) {
        return collect(requireCraftMob(mob), null, true, false);
    }

    @Override public <T extends Mob> Collection<Goal<T>> getRunningGoals(T mob, GoalType type) {
        java.util.Objects.requireNonNull(type, "type");
        return collect(requireCraftMob(mob), type, true, false);
    }

    @Override public <T extends Mob> Collection<Goal<T>> getRunningGoalsWithout(T mob, GoalType type) {
        java.util.Objects.requireNonNull(type, "type");
        return collect(requireCraftMob(mob), type, true, true);
    }

    private <T extends Mob> Collection<Goal<T>> collect(CraftMob mob, GoalType type, boolean runningOnly, boolean invert) {
        Set<net.minecraft.world.entity.ai.goal.Goal> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<Goal<T>> result = new LinkedHashSet<>();
        for (GoalSelector selector : selectors(mob)) {
            for (WrappedGoal wrapped : selector.getAvailableGoals()) {
                if (runningOnly && !wrapped.isRunning()) continue;
                net.minecraft.world.entity.ai.goal.Goal nms = wrapped.getGoal();
                boolean matches = type == null || MobGoalHelper.hasType(nms, type);
                if (type != null && invert) matches = !matches;
                if (!matches || !seen.add(nms)) continue;
                result.add(toPaper(nms));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Mob> Goal<T> toPaper(net.minecraft.world.entity.ai.goal.Goal goal) {
        if (goal instanceof PaperCustomGoal<?> custom) return (Goal<T>) custom.getHandle();
        return new PaperVanillaGoal<>(goal);
    }

    private static CraftMob requireCraftMob(Mob mob) {
        if (!(mob instanceof CraftMob craftMob)) {
            throw new IllegalArgumentException("Mob must be a LunarArc CraftMob, got " + (mob == null ? "null" : mob.getClass().getName()));
        }
        return craftMob;
    }

    private static GoalSelector selector(CraftMob mob, EnumSet<GoalType> types) {
        return types.contains(GoalType.TARGET) ? mob.getHandle().targetSelector : mob.getHandle().goalSelector;
    }

    private static GoalSelector[] selectors(CraftMob mob) {
        return new GoalSelector[] { mob.getHandle().goalSelector, mob.getHandle().targetSelector };
    }

    private static void removeFromBoth(CraftMob mob, net.minecraft.world.entity.ai.goal.Goal goal) {
        mob.getHandle().goalSelector.removeGoal(goal);
        mob.getHandle().targetSelector.removeGoal(goal);
    }
}
