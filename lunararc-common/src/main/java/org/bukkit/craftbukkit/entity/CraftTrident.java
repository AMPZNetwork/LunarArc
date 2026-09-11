package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.ThrownTridentBridge;
import java.util.Objects;
import net.minecraft.world.entity.projectile.ThrownTrident;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class CraftTrident extends CraftAbstractArrow implements org.bukkit.entity.Trident {
    public CraftTrident(CraftServer server, ThrownTrident entity) {
        super(server, entity);
    }

    private ThrownTridentBridge tridentBridge() {
        return (ThrownTridentBridge) (Object) this.getHandle();
    }

    @Override
    public ThrownTrident getHandle() {
        return (ThrownTrident) this.entity;
    }

    @Override
    public ItemStack getItem() {
        return CraftItemStack.asBukkitCopy(this.arrowAccessForTrident().lunararc$getPickupItemStack());
    }

    @Override
    public void setItem(ItemStack itemStack) {
        Objects.requireNonNull(itemStack, "itemStack");
        this.arrowAccessForTrident().lunararc$setPickupItemStack(CraftItemStack.asNMSCopy(itemStack));
    }

    private io.lunararcdevs.lunararc.common.bridge.access.AbstractArrowAccessBridge arrowAccessForTrident() {
        return (io.lunararcdevs.lunararc.common.bridge.access.AbstractArrowAccessBridge) (Object) this.getHandle();
    }

    @Override
    public boolean hasGlint() {
        return this.tridentBridge().lunararc$hasFoil();
    }

    @Override
    public void setGlint(boolean glint) {
        this.tridentBridge().lunararc$setFoil(glint);
    }

    @Override
    public int getLoyaltyLevel() {
        return this.tridentBridge().lunararc$getLoyalty();
    }

    @Override
    public void setLoyaltyLevel(int loyaltyLevel) {
        if (loyaltyLevel < 0 || loyaltyLevel > 127) {
            throw new IllegalArgumentException("The loyalty level has to be between 0 and 127");
        }
        this.tridentBridge().lunararc$setLoyalty((byte) loyaltyLevel);
    }

    @Override
    public boolean hasDealtDamage() {
        return this.tridentBridge().lunararc$hasDealtDamage();
    }

    @Override
    public void setHasDealtDamage(boolean hasDealtDamage) {
        this.tridentBridge().lunararc$setHasDealtDamage(hasDealtDamage);
    }

    @Override
    public String toString() {
        return "CraftTrident";
    }
}
