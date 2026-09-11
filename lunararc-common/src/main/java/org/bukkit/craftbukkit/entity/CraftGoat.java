package org.bukkit.craftbukkit.entity;
import io.lunararcdevs.lunararc.common.bridge.entity.GoatBridge;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import org.bukkit.craftbukkit.CraftServer;
public final class CraftGoat extends CraftAnimals implements org.bukkit.entity.Goat {
    public CraftGoat(CraftServer server, net.minecraft.world.entity.animal.goat.Goat entity) { super(server, entity); }
    @Override public net.minecraft.world.entity.animal.goat.Goat getHandle() { return (net.minecraft.world.entity.animal.goat.Goat) entity; }
    private GoatBridge b() { return (GoatBridge)(Object)getHandle(); }
    @Override public boolean hasLeftHorn() { return getHandle().hasLeftHorn(); }
    @Override public void setLeftHorn(boolean hasHorn) { b().lunararc$setLeftHorn(hasHorn); }
    @Override public boolean hasRightHorn() { return getHandle().hasRightHorn(); }
    @Override public void setRightHorn(boolean hasHorn) { b().lunararc$setRightHorn(hasHorn); }
    @Override public boolean isScreaming() { return getHandle().isScreamingGoat(); }
    @Override public void setScreaming(boolean screaming) { getHandle().setScreamingGoat(screaming); }
    @Override public void ram(org.bukkit.entity.LivingEntity entity) {
        java.util.Objects.requireNonNull(entity, "entity");
        net.minecraft.world.entity.LivingEntity target = ((CraftLivingEntity) entity).getHandle();
        var brain = getHandle().getBrain();
        brain.setMemory(MemoryModuleType.RAM_TARGET, target.position());
        brain.eraseMemory(MemoryModuleType.RAM_COOLDOWN_TICKS);
        brain.eraseMemory(MemoryModuleType.BREED_TARGET);
        brain.eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
        brain.setActiveActivityIfPossible(Activity.RAM);
    }
    @Override public String toString() { return "CraftGoat"; }
}
