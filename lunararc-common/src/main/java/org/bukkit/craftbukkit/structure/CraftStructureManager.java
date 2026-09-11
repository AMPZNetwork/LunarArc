package org.bukkit.craftbukkit.structure;

import io.lunararcdevs.lunararc.common.bridge.access.StructureTemplateManagerAccessBridge;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.bukkit.NamespacedKey;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class CraftStructureManager implements StructureManager {
    private final MinecraftServer server;
    private final StructureTemplateManager handle;

    public CraftStructureManager(MinecraftServer server) {
        this.server = Objects.requireNonNull(server, "server");
        this.handle = server.getStructureManager();
    }

    @SuppressWarnings("unchecked")
    private Map<ResourceLocation, Optional<StructureTemplate>> repository() {
        return ((StructureTemplateManagerAccessBridge) (Object) handle).lunararc$getStructureRepository();
    }

    private static ResourceLocation id(NamespacedKey key) {
        Objects.requireNonNull(key, "key");
        return ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey());
    }

    private static NamespacedKey key(ResourceLocation id) {
        return new NamespacedKey(id.getNamespace(), id.getPath());
    }

    private CraftStructure wrap(StructureTemplate template) {
        return new CraftStructure(server, template);
    }

    private StructureTemplate unwrap(Structure structure) {
        if (!(structure instanceof CraftStructure craft)) {
            throw new IllegalArgumentException("Structure was not created by this server");
        }
        return craft.getHandle();
    }

    @Override
    public @NotNull Map<NamespacedKey, Structure> getStructures() {
        Map<NamespacedKey, Structure> out = new LinkedHashMap<>();
        repository().forEach((id, value) -> value.ifPresent(template -> out.put(key(id), wrap(template))));
        return java.util.Collections.unmodifiableMap(out);
    }

    @Override
    public @Nullable Structure getStructure(@NotNull NamespacedKey structureKey) {
        Optional<StructureTemplate> value = repository().get(id(structureKey));
        return value != null && value.isPresent() ? wrap(value.get()) : null;
    }

    @Override
    public @Nullable Structure registerStructure(@NotNull NamespacedKey structureKey, @NotNull Structure structure) {
        ResourceLocation id = id(structureKey);
        Optional<StructureTemplate> previous = repository().put(id, Optional.of(unwrap(structure)));
        return previous != null && previous.isPresent() ? wrap(previous.get()) : null;
    }

    @Override
    public @Nullable Structure unregisterStructure(@NotNull NamespacedKey structureKey) {
        Optional<StructureTemplate> previous = repository().remove(id(structureKey));
        return previous != null && previous.isPresent() ? wrap(previous.get()) : null;
    }

    @Override
    public @Nullable Structure loadStructure(@NotNull NamespacedKey structureKey, boolean register) {
        ResourceLocation id = id(structureKey);
        Optional<StructureTemplate> existing = repository().get(id);
        if (existing != null && existing.isPresent()) return wrap(existing.get());
        Optional<StructureTemplate> loaded = handle.get(id);
        if (loaded.isEmpty()) return null;
        if (!register) repository().remove(id);
        return wrap(loaded.get());
    }

    @Override
    public @Nullable Structure loadStructure(@NotNull NamespacedKey structureKey) {
        return loadStructure(structureKey, true);
    }

    @Override
    public void saveStructure(@NotNull NamespacedKey structureKey) {
        Structure structure = getStructure(structureKey);
        if (structure == null) throw new IllegalArgumentException("No registered structure " + structureKey);
        try {
            saveStructure(getStructureFile(structureKey), structure);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to save structure " + structureKey, ex);
        }
    }

    @Override
    public void saveStructure(@NotNull NamespacedKey structureKey, @NotNull Structure structure) throws IOException {
        registerStructure(structureKey, structure);
        saveStructure(getStructureFile(structureKey), structure);
    }

    @Override
    public void deleteStructure(@NotNull NamespacedKey structureKey) throws IOException {
        deleteStructure(structureKey, true);
    }

    @Override
    public void deleteStructure(@NotNull NamespacedKey structureKey, boolean unregister) throws IOException {
        Files.deleteIfExists(getStructureFile(structureKey).toPath());
        if (unregister) unregisterStructure(structureKey);
    }

    @Override
    public @NotNull File getStructureFile(@NotNull NamespacedKey structureKey) {
        return handle.createAndValidatePathToGeneratedStructure(id(structureKey), ".nbt").toFile();
    }

    @Override
    public @NotNull Structure loadStructure(@NotNull File file) throws IOException {
        Objects.requireNonNull(file, "file");
        try (InputStream input = new FileInputStream(file)) {
            return loadStructure(input);
        }
    }

    @Override
    public @NotNull Structure loadStructure(@NotNull InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");
        CompoundTag tag = NbtIo.readCompressed(inputStream, NbtAccounter.unlimitedHeap());
        return CraftStructure.fromTag(server, tag);
    }

    @Override
    public void saveStructure(@NotNull File file, @NotNull Structure structure) throws IOException {
        Objects.requireNonNull(file, "file");
        File parent = file.getParentFile();
        if (parent != null) Files.createDirectories(parent.toPath());
        try (OutputStream output = new FileOutputStream(file)) {
            saveStructure(output, structure);
        }
    }

    @Override
    public void saveStructure(@NotNull OutputStream outputStream, @NotNull Structure structure) throws IOException {
        Objects.requireNonNull(outputStream, "outputStream");
        if (!(structure instanceof CraftStructure craft)) throw new IllegalArgumentException("Structure was not created by this server");
        NbtIo.writeCompressed(craft.saveTag(), outputStream);
    }

    @Override
    public @NotNull Structure createStructure() {
        return new CraftStructure(server, new StructureTemplate());
    }

    @Override
    public @NotNull Structure copy(@NotNull Structure structure) {
        if (!(structure instanceof CraftStructure craft)) throw new IllegalArgumentException("Structure was not created by this server");
        return CraftStructure.fromTag(server, craft.saveTag().copy());
    }
}
