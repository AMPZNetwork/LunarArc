package io.papermc.paper.entity;

import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftFish;
import io.ampznetwork.lunararc.common.bridge.EntityBridge;
import io.ampznetwork.lunararc.common.mixin.core.entity.SchoolingFishAccessor;

public class PaperSchoolableFish extends CraftFish implements SchoolableFish {
    public PaperSchoolableFish(CraftServer server, AbstractSchoolingFish entity) {
        super(server, entity);
    }

    @Override public AbstractSchoolingFish getHandle() { return (AbstractSchoolingFish) super.getHandle(); }
    @Override public void startFollowing(SchoolableFish leader) {
        if (getHandle().isFollower()) stopFollowing();
        getHandle().startFollowing(((PaperSchoolableFish) leader).getHandle());
    }
    @Override public void stopFollowing() { getHandle().stopFollowing(); }
    @Override public int getSchoolSize() { return ((SchoolingFishAccessor) getHandle()).lunararc$getSchoolSize(); }
    @Override public int getMaxSchoolSize() { return getHandle().getMaxSchoolSize(); }
    @Override public SchoolableFish getSchoolLeader() {
        AbstractSchoolingFish leader = ((SchoolingFishAccessor) getHandle()).lunararc$getLeader();
        return leader == null ? null : (SchoolableFish) ((EntityBridge) leader).lunararc$getBukkitEntity();
    }
}
