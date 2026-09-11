package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.RandomSourceWrapper;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

public final class CraftLootTable implements org.bukkit.loot.LootTable {
    private final NamespacedKey key;
    private final net.minecraft.world.level.storage.loot.LootTable handle;

    public CraftLootTable(NamespacedKey key, net.minecraft.world.level.storage.loot.LootTable handle) {
        this.key = Objects.requireNonNull(key, "key");
        this.handle = Objects.requireNonNull(handle, "handle");
    }

    public net.minecraft.world.level.storage.loot.LootTable getHandle() {
        return this.handle;
    }

    public static ResourceKey<net.minecraft.world.level.storage.loot.LootTable> bukkitKeyToMinecraft(NamespacedKey key) {
        Objects.requireNonNull(key, "key");
        return ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey()));
    }

    // The three methods below aren't in any Paper patch — real Paper's CraftLootTable is
    // CraftBukkit-base source, which (unlike Paper's own patches) isn't legitimately obtainable
    // here (see the parity tracker's note on VanillaCommandWrapper/ApiVersion for the same
    // sourcing wall). minecraftToBukkit's exact real logic is confirmed from
    // patches/server/0920-Fixup-NamespacedKey-handling.patch, which shows a later fix to it —
    // that fragment is what's ported verbatim below. bukkitToMinecraft and minecraftToBukkitKey
    // are the mechanical inverse of bukkitKeyToMinecraft directly above (same real, existing
    // conversion this class already does elsewhere), not independently-sourced logic.

    public static NamespacedKey minecraftToBukkitKey(ResourceKey<net.minecraft.world.level.storage.loot.LootTable> minecraft) {
        Objects.requireNonNull(minecraft, "minecraft");
        return new NamespacedKey(minecraft.location().getNamespace(), minecraft.location().getPath());
    }

    @org.jetbrains.annotations.Nullable
    public static org.bukkit.loot.LootTable minecraftToBukkit(
            @org.jetbrains.annotations.Nullable ResourceKey<net.minecraft.world.level.storage.loot.LootTable> minecraft) {
        return (minecraft == null || minecraft.location().getPath().isEmpty())
                ? null
                : org.bukkit.Bukkit.getLootTable(minecraftToBukkitKey(minecraft));
    }

    @org.jetbrains.annotations.Nullable
    public static ResourceKey<net.minecraft.world.level.storage.loot.LootTable> bukkitToMinecraft(
            @org.jetbrains.annotations.Nullable org.bukkit.loot.LootTable bukkit) {
        return bukkit == null ? null : bukkitKeyToMinecraft(bukkit.getKey());
    }

    @Override
    public Collection<ItemStack> populateLoot(Random random, LootContext context) {
        Objects.requireNonNull(random, "random");
        Objects.requireNonNull(context, "context");
        LootParams params = convertContext(context);
        List<net.minecraft.world.item.ItemStack> generated =
                this.handle.getRandomItems(params, new RandomSourceWrapper(random));
        List<ItemStack> result = new ArrayList<>(generated.size());
        for (net.minecraft.world.item.ItemStack stack : generated) {
            if (!stack.isEmpty()) result.add(CraftItemStack.asBukkitCopy(stack));
        }
        return result;
    }

    @Override
    public void fillInventory(Inventory inventory, Random random, LootContext context) {
        Objects.requireNonNull(inventory, "inventory");
        Objects.requireNonNull(random, "random");
        Objects.requireNonNull(context, "context");

        List<ItemStack> loot = new ArrayList<>(populateLoot(random, context));
        Location location = Objects.requireNonNull(context.getLocation(), "LootContext location");
        org.bukkit.World bukkitWorld = Objects.requireNonNull(location.getWorld(), "LootContext world");
        org.bukkit.event.world.LootGenerateEvent event = new org.bukkit.event.world.LootGenerateEvent(
                bukkitWorld,
                context.getLootedEntity(),
                inventory.getHolder(),
                this,
                context,
                loot,
                true);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        // Vanilla fills random available slots. Keep that behavior at the
        // Bukkit boundary so this works for both LunarArc's generic inventories
        // and NMS-backed Inventory implementations. Existing occupied slots are
        // never overwritten.
        List<Integer> availableSlots = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack existing = inventory.getItem(slot);
            if (existing == null || existing.isEmpty()) availableSlots.add(slot);
        }
        java.util.Collections.shuffle(availableSlots, random);

        List<ItemStack> generated = new ArrayList<>();
        for (ItemStack stack : event.getLoot()) {
            if (stack == null || stack.isEmpty()) continue;
            generated.add(stack.clone());
        }
        splitToFit(generated, availableSlots.size(), random);
        java.util.Collections.shuffle(generated, random);

        int count = Math.min(availableSlots.size(), generated.size());
        for (int i = 0; i < count; i++) {
            inventory.setItem(availableSlots.get(i), generated.get(i));
        }
    }

    private static void splitToFit(List<ItemStack> stacks, int slotCount, Random random) {
        if (slotCount <= 0) {
            stacks.clear();
            return;
        }
        while (stacks.size() < slotCount) {
            List<ItemStack> splittable = new ArrayList<>();
            for (ItemStack stack : stacks) {
                if (stack.getAmount() > 1) splittable.add(stack);
            }
            if (splittable.isEmpty()) break;
            ItemStack chosen = splittable.get(random.nextInt(splittable.size()));
            int amount = chosen.getAmount();
            int split = 1 + random.nextInt(amount / 2);
            chosen.setAmount(amount - split);
            ItemStack extra = chosen.clone();
            extra.setAmount(split);
            stacks.add(extra);
        }
    }

    @Override
    public NamespacedKey getKey() {
        return this.key;
    }

    private LootParams convertContext(LootContext context) {
        Location location = Objects.requireNonNull(context.getLocation(), "LootContext location");
        Preconditions.checkArgument(location.getWorld() instanceof CraftWorld,
                "LootContext world is not backed by LunarArc CraftWorld");
        ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();
        LootParams.Builder builder = new LootParams.Builder(level);

        setMaybe(builder, LootContextParams.ORIGIN,
                new Vec3(location.getX(), location.getY(), location.getZ()));

        if (this.handle != net.minecraft.world.level.storage.loot.LootTable.EMPTY) {
            if (context.getLootedEntity() instanceof CraftEntity craftEntity) {
                Entity looted = craftEntity.getHandle();
                setMaybe(builder, LootContextParams.THIS_ENTITY, looted);
                setMaybe(builder, LootContextParams.DAMAGE_SOURCE, level.damageSources().generic());
                setMaybe(builder, LootContextParams.ORIGIN, looted.position());
            }

            if (context.getKiller() instanceof CraftHumanEntity human) {
                Player killer = human.getHandle();
                setMaybe(builder, LootContextParams.ATTACKING_ENTITY, killer);
                setMaybe(builder, LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(killer));
                setMaybe(builder, LootContextParams.LAST_DAMAGE_PLAYER, killer);
                setMaybe(builder, LootContextParams.TOOL, killer.getUseItem());
            }
        }
        return builder.create(this.handle.getParamSet());
    }

    private <T> void setMaybe(LootParams.Builder builder, LootContextParam<T> parameter, T value) {
        if (this.handle.getParamSet().getRequired().contains(parameter)
                || this.handle.getParamSet().getAllowed().contains(parameter)) {
            builder.withParameter(parameter, value);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof org.bukkit.loot.LootTable table && this.key.equals(table.getKey());
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public String toString() {
        return this.key.toString();
    }

    /** CraftBukkit's ResourceLocation overload; null in, null out, as CraftBukkit has it. */
    public static org.bukkit.loot.LootTable minecraftToBukkit(net.minecraft.resources.ResourceLocation minecraft) {
        return minecraft == null ? null : org.bukkit.Bukkit.getLootTable(
                org.bukkit.craftbukkit.util.CraftNamespacedKey.fromMinecraft(minecraft));
    }
}
