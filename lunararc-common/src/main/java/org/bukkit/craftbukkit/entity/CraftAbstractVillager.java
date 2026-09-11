package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.entity.AbstractVillagerBridge;
import java.util.List;
import net.minecraft.world.item.trading.MerchantOffers;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftNMSInventory;
import org.bukkit.craftbukkit.inventory.CraftMerchantRecipe;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.MerchantRecipe;

public class CraftAbstractVillager extends CraftAgeable implements org.bukkit.entity.AbstractVillager {
    public CraftAbstractVillager(CraftServer server, net.minecraft.world.entity.npc.AbstractVillager entity) { super(server, entity); }
    @Override public net.minecraft.world.entity.npc.AbstractVillager getHandle() { return (net.minecraft.world.entity.npc.AbstractVillager) this.entity; }
    protected AbstractVillagerBridge villagerBridge() { return (AbstractVillagerBridge) (Object) getHandle(); }

    @Override public Inventory getInventory() { return new CraftNMSInventory(getHandle().getInventory(), this); }
    @Override public void resetOffers() { villagerBridge().lunararc$resetOffers(); }

    @Override public List<MerchantRecipe> getRecipes() {
        return getHandle().getOffers().stream().map(CraftMerchantRecipe::new).map(MerchantRecipe.class::cast).toList();
    }
    @Override public void setRecipes(List<MerchantRecipe> recipes) {
        java.util.Objects.requireNonNull(recipes, "recipes");
        MerchantOffers offers = new MerchantOffers();
        for (MerchantRecipe recipe : recipes) offers.add(CraftMerchantRecipe.fromBukkit(recipe).toMinecraft());
        villagerBridge().lunararc$replaceOffers(offers);
    }
    @Override public MerchantRecipe getRecipe(int index) { return new CraftMerchantRecipe(getHandle().getOffers().get(index)); }
    @Override public void setRecipe(int index, MerchantRecipe recipe) { getHandle().getOffers().set(index, CraftMerchantRecipe.fromBukkit(recipe).toMinecraft()); }
    @Override public int getRecipeCount() { return getHandle().getOffers().size(); }
    @Override public boolean isTrading() { return getHandle().getTradingPlayer() != null; }
    @Override public HumanEntity getTrader() {
        net.minecraft.world.entity.player.Player player = getHandle().getTradingPlayer();
        return player == null ? null : (HumanEntity) CraftEntity.getEntity(server, player);
    }

    @Override public String toString() { return "CraftAbstractVillager"; }

    /** The NMS Merchant behind this villager, under CraftBukkit's name. */
    public net.minecraft.world.item.trading.Merchant getMerchant() {
        return getHandle();
    }
}
