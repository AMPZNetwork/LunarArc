package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.EntityFactory;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;

public final class CraftEntityFactory implements EntityFactory {
    private static final CraftEntityFactory INSTANCE = new CraftEntityFactory();

    private CraftEntityFactory() {}

    public static CraftEntityFactory instance() {
        return INSTANCE;
    }

    @Override
    public EntitySnapshot createEntitySnapshot(String input) {
        Preconditions.checkArgument(input != null, "Input string cannot be null");
        final CompoundTag tag;
        try {
            tag = TagParser.parseTag(input);
        } catch (CommandSyntaxException exception) {
            throw new IllegalArgumentException("Could not parse Entity: " + input, exception);
        }

        net.minecraft.world.entity.EntityType<?> minecraftType =
                net.minecraft.world.entity.EntityType.by(tag).orElse(null);
        if (minecraftType == null) {
            throw new IllegalArgumentException("Could not parse Entity: " + input);
        }

        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(minecraftType);
        if (id == null) {
            throw new IllegalArgumentException("Entity type from snapshot is not registered: " + minecraftType);
        }
        EntityType bukkitType = Registry.ENTITY_TYPE.get(new NamespacedKey(id.getNamespace(), id.getPath()));
        if (bukkitType == null) {
            throw new IllegalArgumentException("No Bukkit EntityType wrapper exists for " + id);
        }

        EntitySnapshot snapshot = CraftEntitySnapshot.create(tag, bukkitType);
        if (snapshot == null) {
            throw new IllegalArgumentException("Could not create EntitySnapshot: " + input);
        }
        return snapshot;
    }
}
