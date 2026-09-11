package org.bukkit.craftbukkit.entity;

import java.util.UUID;
import net.minecraft.world.entity.animal.Animal;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Animals;
import org.bukkit.inventory.ItemStack;

public class CraftAnimals extends CraftAgeable implements Animals {
    public CraftAnimals(CraftServer server, Animal entity) { super(server, entity); }

    @Override public Animal getHandle() { return (Animal) this.entity; }
    @Override public UUID getBreedCause() { return getHandle().loveCause; }
    @Override public void setBreedCause(UUID uuid) { getHandle().loveCause = uuid; }
    @Override public boolean isLoveMode() { return getHandle().isInLove(); }

    @Override
    public void setLoveModeTicks(int ticks) {
        if (ticks < 0) throw new IllegalArgumentException("Love mode ticks must be positive or 0");
        getHandle().inLove = ticks;
    }

    @Override public int getLoveModeTicks() { return getHandle().inLove; }
    @Override public boolean isBreedItem(ItemStack itemStack) {
        if (itemStack == null) throw new IllegalArgumentException("itemStack");
        return getHandle().isFood(CraftItemStack.asNMSCopy(itemStack));
    }
    @Override public boolean isBreedItem(Material material) {
        if (material == null) throw new IllegalArgumentException("material");
        return isBreedItem(new ItemStack(material));
    }

    @Override public String toString() { return "CraftAnimals"; }
}
