package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockType;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public final class CraftItemType<M extends ItemMeta> implements ItemType.Typed<M> {
    private final NamespacedKey key;
    private final Item handle;
    private final Class<M> metaClass;

    @SuppressWarnings("unchecked")
    public CraftItemType(NamespacedKey key, Item handle) {
        this.key = Objects.requireNonNull(key, "key");
        this.handle = Objects.requireNonNull(handle, "handle");
        Material material = Registry.MATERIAL.get(key);
        this.metaClass = (Class<M>) ((material == Material.POTION || material == Material.SPLASH_POTION
                || material == Material.LINGERING_POTION || material == Material.TIPPED_ARROW)
                ? PotionMeta.class : ItemMeta.class);
    }

    public Item getHandle() { return this.handle; }

    public static ItemType minecraftToBukkitNew(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return id == null ? null : Registry.ITEM.get(new NamespacedKey(id.getNamespace(), id.getPath()));
    }

    @Override public ItemType.@NotNull Typed<ItemMeta> typed() { return this.typed(ItemMeta.class); }

    @Override
    @SuppressWarnings("unchecked")
    public <Other extends ItemMeta> ItemType.@NotNull Typed<Other> typed(@NotNull Class<Other> itemMetaType) {
        Objects.requireNonNull(itemMetaType, "itemMetaType");
        if (itemMetaType.isAssignableFrom(this.metaClass)) return (ItemType.Typed<Other>) this;
        throw new IllegalArgumentException("Cannot type item type " + this.key + " to meta type " + itemMetaType.getSimpleName());
    }

    @Override public @NotNull ItemStack createItemStack() { return createItemStack(1, null); }
    @Override public @NotNull ItemStack createItemStack(int amount) { return createItemStack(amount, null); }
    @Override public @NotNull ItemStack createItemStack(@Nullable Consumer<? super M> metaConfigurator) { return createItemStack(1, metaConfigurator); }

    @Override
    public @NotNull ItemStack createItemStack(int amount, @Nullable Consumer<? super M> metaConfigurator) {
        Preconditions.checkArgument(amount >= 1, "amount must be at least 1");
        CraftItemStack stack = CraftItemStack.asCraftMirror(new net.minecraft.world.item.ItemStack(this.handle, amount));
        if (metaConfigurator != null) stack.editMeta(this.metaClass, metaConfigurator);
        return stack;
    }

    @Override public boolean hasBlockType() { return this.handle instanceof BlockItem; }

    @Override
    public @NotNull BlockType getBlockType() {
        if (!(this.handle instanceof BlockItem blockItem)) throw new IllegalStateException("The item type " + this.key + " has no corresponding block type");
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
        BlockType value = id == null ? null : Registry.BLOCK.get(new NamespacedKey(id.getNamespace(), id.getPath()));
        if (value == null) throw new IllegalStateException("No Bukkit BlockType for " + id);
        return value;
    }

    @Override public @NotNull Class<M> getItemMetaClass() { return this.metaClass; }
    @Override public int getMaxStackSize() { return this.key.equals(NamespacedKey.minecraft("air")) ? 0 : this.handle.components().getOrDefault(DataComponents.MAX_STACK_SIZE, 64); }
    @Override public short getMaxDurability() { return this.handle.components().getOrDefault(DataComponents.MAX_DAMAGE, 0).shortValue(); }
    @Override public boolean isEdible() { return this.handle.components().has(DataComponents.FOOD); }
    @Override public boolean isRecord() { return this.handle.components().has(DataComponents.JUKEBOX_PLAYABLE); }
    @Override public boolean isFuel() { return AbstractFurnaceBlockEntity.isFuel(new net.minecraft.world.item.ItemStack(this.handle)); }
    @Override public boolean isCompostable() { return ComposterBlock.COMPOSTABLES.containsKey(this.handle); }

    @Override
    public float getCompostChance() {
        Preconditions.checkArgument(this.isCompostable(), "The item type %s is not compostable", this.key);
        return ComposterBlock.COMPOSTABLES.getFloat(this.handle);
    }

    @Override
    public @Nullable ItemType getCraftingRemainingItem() {
        Item remaining = this.handle.getCraftingRemainingItem();
        return remaining == null ? null : minecraftToBukkitNew(remaining);
    }

    @Override public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers() { return this.defaultAttributes(group -> true); }

    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot slot) {
        net.minecraft.world.entity.EquipmentSlot nms = CraftEquipmentSlot.getNMS(slot);
        return this.defaultAttributes(group -> group.test(nms));
    }

    private Multimap<Attribute, AttributeModifier> defaultAttributes(java.util.function.Predicate<net.minecraft.world.entity.EquipmentSlotGroup> slots) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> result = ImmutableMultimap.builder();
        ItemAttributeModifiers modifiers = this.handle.components().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (modifiers.modifiers().isEmpty()) modifiers = this.handle.getDefaultAttributeModifiers();
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (!slots.test(entry.slot())) continue;
            var nmsKey = entry.attribute().unwrapKey().orElse(null);
            if (nmsKey == null) continue;
            ResourceLocation id = nmsKey.location();
            Attribute attribute = Registry.ATTRIBUTE.get(new NamespacedKey(id.getNamespace(), id.getPath()));
            if (attribute == null) continue;
            net.minecraft.world.entity.ai.attributes.AttributeModifier modifier = entry.modifier();
            AttributeModifier.Operation operation = switch (modifier.operation()) {
                case ADD_VALUE -> AttributeModifier.Operation.ADD_NUMBER;
                case ADD_MULTIPLIED_BASE -> AttributeModifier.Operation.ADD_SCALAR;
                case ADD_MULTIPLIED_TOTAL -> AttributeModifier.Operation.MULTIPLY_SCALAR_1;
            };
            result.put(attribute, new AttributeModifier(
                    new NamespacedKey(modifier.id().getNamespace(), modifier.id().getPath()), modifier.amount(), operation,
                    CraftEquipmentSlot.getSlot(entry.slot())));
        }
        return result.build();
    }

    @Override public @Nullable CreativeCategory getCreativeCategory() { return CreativeCategory.BUILDING_BLOCKS; }

    @Override
    public boolean isEnabledByFeature(@NotNull World world) {
        Objects.requireNonNull(world, "world");
        if (!(world instanceof CraftWorld craftWorld)) throw new IllegalArgumentException("World is not backed by LunarArc CraftWorld");
        return this.handle.isEnabled(craftWorld.getHandle().enabledFeatures());
    }

    @Override public @Nullable Material asMaterial() { return Registry.MATERIAL.get(this.key); }
    @Override public @NotNull String getTranslationKey() { return this.handle.getDescriptionId(); }
    @Override public @NotNull String translationKey() { return this.handle.getDescriptionId(); }
    @Override public @Nullable org.bukkit.inventory.ItemRarity getItemRarity() {
        net.minecraft.world.item.Rarity rarity = this.handle.components().get(DataComponents.RARITY);
        return rarity == null ? null : org.bukkit.inventory.ItemRarity.valueOf(rarity.name());
    }
    @Override public @NotNull NamespacedKey getKey() { return this.key; }

    @Override public String toString() { return "CraftItemType[" + this.key + "]"; }

    // CraftBukkit's Material/ItemType conversion set, mirroring CraftBlockType's.
    public static org.bukkit.Material minecraftToBukkit(net.minecraft.world.item.Item item) {
        return org.bukkit.craftbukkit.util.CraftMagicNumbers.getMaterial(item);
    }

    public static net.minecraft.world.item.Item bukkitToMinecraft(org.bukkit.Material material) {
        return org.bukkit.craftbukkit.util.CraftMagicNumbers.getItem(material);
    }

    public static net.minecraft.world.item.Item bukkitToMinecraftNew(org.bukkit.inventory.ItemType bukkit) {
        return org.bukkit.craftbukkit.CraftRegistry.bukkitToMinecraft(bukkit);
    }
}
