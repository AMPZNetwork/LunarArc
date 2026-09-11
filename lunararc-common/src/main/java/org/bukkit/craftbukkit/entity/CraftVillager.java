package org.bukkit.craftbukkit.entity;

import com.destroystokyo.paper.entity.villager.Reputation;
import com.destroystokyo.paper.entity.villager.ReputationType;
import io.lunararcdevs.lunararc.common.bridge.entity.VillagerBridge;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;

public final class CraftVillager extends CraftAbstractVillager implements Villager {
    public CraftVillager(CraftServer server, net.minecraft.world.entity.npc.Villager entity) { super(server, entity); }
    @Override public net.minecraft.world.entity.npc.Villager getHandle() { return (net.minecraft.world.entity.npc.Villager) this.entity; }
    private VillagerBridge concreteVillagerBridge() { return (VillagerBridge) (Object) getHandle(); }

    @Override
    public Profession getProfession() {
        net.minecraft.world.entity.npc.VillagerProfession nms = getHandle().getVillagerData().getProfession();
        ResourceLocation key = server.getServer().registryAccess().registryOrThrow(Registries.VILLAGER_PROFESSION).getKey(nms);
        if (key == null) throw new IllegalStateException("Villager profession is not registry-backed");
        Profession profession = Registry.VILLAGER_PROFESSION.get(new NamespacedKey(key.getNamespace(), key.getPath()));
        if (profession == null) throw new IllegalStateException("No Bukkit profession for " + key);
        return profession;
    }

    @Override
    public void setProfession(Profession profession) {
        Objects.requireNonNull(profession, "profession");
        NamespacedKey key = profession.getKey();
        net.minecraft.world.entity.npc.VillagerProfession nms = server.getServer().registryAccess().registryOrThrow(Registries.VILLAGER_PROFESSION)
                .get(ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey()));
        if (nms == null) throw new IllegalArgumentException("Unknown villager profession " + key);
        getHandle().setVillagerData(getHandle().getVillagerData().setProfession(nms));
    }

    @Override
    public Type getVillagerType() {
        net.minecraft.world.entity.npc.VillagerType nms = getHandle().getVillagerData().getType();
        ResourceLocation key = server.getServer().registryAccess().registryOrThrow(Registries.VILLAGER_TYPE).getKey(nms);
        if (key == null) throw new IllegalStateException("Villager type is not registry-backed");
        Type type = Registry.VILLAGER_TYPE.get(new NamespacedKey(key.getNamespace(), key.getPath()));
        if (type == null) throw new IllegalStateException("No Bukkit villager type for " + key);
        return type;
    }

    @Override
    public void setVillagerType(Type type) {
        Objects.requireNonNull(type, "type");
        NamespacedKey key = type.getKey();
        net.minecraft.world.entity.npc.VillagerType nms = server.getServer().registryAccess().registryOrThrow(Registries.VILLAGER_TYPE)
                .get(ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey()));
        if (nms == null) throw new IllegalArgumentException("Unknown villager type " + key);
        getHandle().setVillagerData(getHandle().getVillagerData().setType(nms));
    }

    @Override public int getVillagerLevel() { return getHandle().getVillagerData().getLevel(); }
    @Override public void setVillagerLevel(int level) {
        if (level < 1 || level > 5) throw new IllegalArgumentException("level must be between 1 and 5");
        getHandle().setVillagerData(getHandle().getVillagerData().setLevel(level));
    }
    @Override public int getVillagerExperience() { return concreteVillagerBridge().lunararc$getVillagerXp(); }
    @Override public void setVillagerExperience(int experience) {
        if (experience < 0) throw new IllegalArgumentException("experience must be >= 0");
        concreteVillagerBridge().lunararc$setVillagerXp(experience);
    }

    @Override
    public boolean increaseLevel(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        int target = getVillagerLevel() + amount;
        if (target > 5) throw new IllegalArgumentException("final villager level must be <= 5");
        boolean added = false;
        while (getVillagerLevel() < target) {
            int next = getVillagerLevel() + 1;
            setVillagerLevel(next);
            var byLevel = VillagerTrades.TRADES.get(getHandle().getVillagerData().getProfession());
            if (byLevel != null) {
                VillagerTrades.ItemListing[] listings = byLevel.get(next);
                if (listings != null) {
                    int before = getHandle().getOffers().size();
                    super.villagerBridge().lunararc$addOffers(getHandle().getOffers(), listings, 2);
                    added |= getHandle().getOffers().size() > before;
                }
            }
        }
        return added;
    }

    @Override
    public boolean addTrades(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        var byLevel = VillagerTrades.TRADES.get(getHandle().getVillagerData().getProfession());
        if (byLevel == null) return false;
        VillagerTrades.ItemListing[] listings = byLevel.get(getVillagerLevel());
        if (listings == null || listings.length == 0) return false;
        int before = getHandle().getOffers().size();
        super.villagerBridge().lunararc$addOffers(getHandle().getOffers(), listings, amount);
        return getHandle().getOffers().size() > before;
    }

    @Override public int getRestocksToday() { return concreteVillagerBridge().lunararc$getRestocksToday(); }
    @Override public void setRestocksToday(int restocksToday) { concreteVillagerBridge().lunararc$setRestocksToday(restocksToday); }

    @Override
    public boolean sleep(Location location) {
        Objects.requireNonNull(location, "location");
        if (location.getWorld() == null || !location.getWorld().equals(getWorld())) throw new IllegalArgumentException("location must be in the villager's world");
        BlockPos pos = BlockPos.containing(location.getX(), location.getY(), location.getZ());
        if (!(getHandle().level().getBlockState(pos).getBlock() instanceof net.minecraft.world.level.block.BedBlock)) return false;
        getHandle().startSleeping(pos);
        return true;
    }
    @Override public void wakeup() {
        if (!isSleeping()) throw new IllegalStateException("villager is not sleeping");
        getHandle().stopSleeping();
    }
    @Override public void shakeHead() { getHandle().setUnhappy(); }

    @Override
    public ZombieVillager zombify() {
        if (!(getHandle().level() instanceof net.minecraft.server.level.ServerLevel level)) return null;
        net.minecraft.world.entity.monster.ZombieVillager converted =
                io.lunararcdevs.lunararc.common.mod.util.LunarArcEntityTransforms.convert(
                        getHandle(),
                        net.minecraft.world.entity.EntityType.ZOMBIE_VILLAGER,
                        false,
                        org.bukkit.event.entity.EntityTransformEvent.TransformReason.INFECTION,
                        org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM,
                        zombie -> {
                            zombie.setVillagerData(getHandle().getVillagerData());
                            zombie.setGossips(getHandle().getGossips().store(net.minecraft.nbt.NbtOps.INSTANCE));
                            zombie.setTradeOffers(getHandle().getOffers().copy());
                            zombie.setVillagerXp(getHandle().getVillagerXp());
                        });
        if (converted == null) return null;
        converted.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(converted.blockPosition()),
                net.minecraft.world.entity.MobSpawnType.CONVERSION,
                new net.minecraft.world.entity.monster.Zombie.ZombieGroupData(false, true));
        return (ZombieVillager) ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) converted).lunararc$getBukkitEntity();
    }

    @Override
    public Reputation getReputation(UUID uniqueId) {
        Objects.requireNonNull(uniqueId, "uniqueId");
        EnumMap<ReputationType, Integer> values = new EnumMap<>(ReputationType.class);
        var entries = getHandle().getGossips().getGossipEntries().get(uniqueId);
        if (entries != null) {
            for (ReputationType type : ReputationType.values()) {
                int value = entries.getOrDefault(GossipType.valueOf(type.name()), 0);
                if (value != 0) values.put(type, value);
            }
        }
        return new Reputation(values);
    }

    @Override
    public Map<UUID, Reputation> getReputations() {
        Map<UUID, Reputation> result = new HashMap<>();
        for (UUID id : getHandle().getGossips().getGossipEntries().keySet()) result.put(id, getReputation(id));
        return result;
    }

    @Override
    public void setReputation(UUID uniqueId, Reputation reputation) {
        Objects.requireNonNull(uniqueId, "uniqueId");
        Objects.requireNonNull(reputation, "reputation");
        GossipContainer gossips = getHandle().getGossips();
        for (ReputationType type : ReputationType.values()) {
            GossipType nms = GossipType.valueOf(type.name());
            gossips.remove(uniqueId, nms);
            if (reputation.hasReputationSet(type)) {
                int value = reputation.getReputation(type);
                if (value != 0) gossips.add(uniqueId, nms, value);
            }
        }
    }

    @Override public void setReputations(Map<UUID, Reputation> reputations) {
        Objects.requireNonNull(reputations, "reputations");
        for (Map.Entry<UUID, Reputation> entry : reputations.entrySet()) setReputation(entry.getKey(), entry.getValue());
    }

    @Override
    public void clearReputations() {
        GossipContainer gossips = getHandle().getGossips();
        for (UUID id : java.util.List.copyOf(gossips.getGossipEntries().keySet())) {
            for (GossipType type : GossipType.values()) gossips.remove(id, type);
        }
    }

    @Override public String toString() { return "CraftVillager"; }
}
