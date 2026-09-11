package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Fallable;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public final class CraftBlockType<B extends BlockData> implements BlockType.Typed<B> {
    private final NamespacedKey key;
    private final Block handle;

    public CraftBlockType(NamespacedKey key, Block handle) {
        this.key = Objects.requireNonNull(key, "key");
        this.handle = Objects.requireNonNull(handle, "handle");
    }

    public Block getHandle() { return this.handle; }

    public static BlockType minecraftToBukkitNew(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        return id == null ? null : Registry.BLOCK.get(new NamespacedKey(id.getNamespace(), id.getPath()));
    }

    @Override public BlockType.@NotNull Typed<BlockData> typed() { return this.typed(BlockData.class); }

    @Override
    @SuppressWarnings("unchecked")
    public <Other extends BlockData> BlockType.@NotNull Typed<Other> typed(@NotNull Class<Other> blockDataType) {
        Objects.requireNonNull(blockDataType, "blockDataType");
        if (blockDataType.isAssignableFrom(CraftBlockData.class) || blockDataType == BlockData.class) return (BlockType.Typed<Other>) this;
        throw new IllegalArgumentException("Cannot type block type " + this.key + " to blockdata type " + blockDataType.getSimpleName());
    }

    @Override public boolean hasItemType() { return this.handle.asItem() != net.minecraft.world.item.Items.AIR || this.key.equals(NamespacedKey.minecraft("air")); }

    @Override
    public @NotNull ItemType getItemType() {
        if (this.key.equals(NamespacedKey.minecraft("air"))) return ItemType.AIR;
        net.minecraft.world.item.Item item = this.handle.asItem();
        Preconditions.checkArgument(item != net.minecraft.world.item.Items.AIR, "The block type %s has no corresponding item type", this.key);
        ItemType value = org.bukkit.craftbukkit.inventory.CraftItemType.minecraftToBukkitNew(item);
        if (value == null) throw new IllegalStateException("No Bukkit ItemType for block " + this.key);
        return value;
    }

    @Override @SuppressWarnings("unchecked") public @NotNull Class<B> getBlockDataClass() { return (Class<B>) BlockData.class; }
    @Override @SuppressWarnings("unchecked") public @NotNull B createBlockData() { return (B) CraftBlockData.fromData(this.handle.defaultBlockState()); }
    @Override public @NotNull B createBlockData(@Nullable Consumer<? super B> consumer) { B data = createBlockData(); if (consumer != null) consumer.accept(data); return data; }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull B createBlockData(@Nullable String data) {
        if (data == null || data.isBlank()) return createBlockData();
        String text = data.trim();
        if (text.startsWith("[")) text = this.key + text;
        else if (!text.contains(":" ) && !text.contains("[")) text = this.key + "[" + text + "]";
        CraftBlockData parsed = CraftBlockData.parse(text);
        if (parsed.getState().getBlock() != this.handle) throw new IllegalArgumentException("Block data does not match " + this.key + ": " + data);
        return (B) parsed;
    }

    @Override public boolean isSolid() { return this.handle.defaultBlockState().blocksMotion(); }
    @Override public boolean isAir() { return this.handle.defaultBlockState().isAir(); }

    @Override
    public boolean isEnabledByFeature(@NotNull World world) {
        Objects.requireNonNull(world, "world");
        if (!(world instanceof CraftWorld craftWorld)) throw new IllegalArgumentException("World is not backed by LunarArc CraftWorld");
        return this.handle.isEnabled(craftWorld.getHandle().enabledFeatures());
    }

    @Override public boolean isFlammable() { return this.handle.defaultBlockState().ignitedByLava(); }
    @Override public boolean isBurnable() { Material material = asMaterial(); return material != null && material.isBurnable(); }
    @Override public boolean isOccluding() { return this.handle.defaultBlockState().isRedstoneConductor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO); }
    @Override public boolean hasGravity() { return this.handle instanceof Fallable; }
    @Override public boolean isInteractable() { Material material = asMaterial(); return material != null && material.isInteractable(); }
    @Override public float getHardness() { return this.handle.defaultBlockState().getDestroySpeed(EmptyBlockGetter.INSTANCE, BlockPos.ZERO); }
    @Override public float getBlastResistance() { return this.handle.getExplosionResistance(); }
    @Override public float getSlipperiness() { return this.handle.getFriction(); }
    @Override public @NotNull String getTranslationKey() { return this.handle.getDescriptionId(); }
    @Override public @NotNull String translationKey() { return this.handle.getDescriptionId(); }
    @Override public boolean hasCollision() { return !this.handle.defaultBlockState().getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty(); }
    @Override public @Nullable Material asMaterial() { return Registry.MATERIAL.get(this.key); }
    @Override public @NotNull NamespacedKey getKey() { return this.key; }
    @Override public String toString() { return "CraftBlockType[" + this.key + "]"; }

    // CraftBukkit's Material/BlockType conversion set. The "New" pair is the typed BlockType API
    // added in 1.20.5; the plain pair is the long-standing Material one plugins already use.
    public static org.bukkit.Material minecraftToBukkit(net.minecraft.world.level.block.Block block) {
        return org.bukkit.craftbukkit.util.CraftMagicNumbers.getMaterial(block);
    }

    public static net.minecraft.world.level.block.Block bukkitToMinecraft(org.bukkit.Material material) {
        return org.bukkit.craftbukkit.util.CraftMagicNumbers.getBlock(material);
    }

    public static net.minecraft.world.level.block.Block bukkitToMinecraftNew(org.bukkit.block.BlockType bukkit) {
        return org.bukkit.craftbukkit.CraftRegistry.bukkitToMinecraft(bukkit);
    }
}
