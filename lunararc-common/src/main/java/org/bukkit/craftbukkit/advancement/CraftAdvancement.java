package org.bukkit.craftbukkit.advancement;

import io.lunararcdevs.lunararc.common.LunarArcServerAccess;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CraftAdvancement implements Advancement {
    private final AdvancementHolder handle;

    public CraftAdvancement(@NotNull AdvancementHolder handle) {
        this.handle = Objects.requireNonNull(handle, "handle");
    }

    public @NotNull AdvancementHolder getHandle() { return this.handle; }

    @Override
    public @NotNull NamespacedKey getKey() {
        var id = this.handle.id();
        return new NamespacedKey(id.getNamespace(), id.getPath());
    }

    @Override
    public @NotNull Collection<String> getCriteria() {
        return Collections.unmodifiableSet(this.handle.value().criteria().keySet());
    }

    @Override
    public @Nullable io.papermc.paper.advancement.AdvancementDisplay getDisplay() {
        return this.handle.value().display()
                .<io.papermc.paper.advancement.AdvancementDisplay>map(io.papermc.paper.advancement.PaperAdvancementDisplay::new)
                .orElse(null);
    }

    @Override
    public @NotNull net.kyori.adventure.text.Component displayName() {
        return io.papermc.paper.adventure.PaperAdventure.asAdventure(
                net.minecraft.advancements.Advancement.name(this.handle));
    }

    @Override
    public @Nullable Advancement getParent() {
        var manager = LunarArcServerAccess.getMinecraftServer().getAdvancements();
        return this.handle.value().parent()
                .map(manager::get)
                .map(CraftAdvancement::new)
                .orElse(null);
    }

    @Override
    public @NotNull Collection<Advancement> getChildren() {
        var manager = LunarArcServerAccess.getMinecraftServer().getAdvancements();
        AdvancementNode node = manager.tree().get(this.handle);
        if (node == null) return List.of();
        List<Advancement> children = new ArrayList<>();
        for (AdvancementNode child : node.children()) {
            children.add(new CraftAdvancement(child.holder()));
        }
        return List.copyOf(children);
    }

    @Override
    public @NotNull Advancement getRoot() {
        var manager = LunarArcServerAccess.getMinecraftServer().getAdvancements();
        AdvancementNode node = Objects.requireNonNull(manager.tree().get(this.handle),
                "Missing advancement tree node for " + this.handle.id());
        return new CraftAdvancement(node.root().holder());
    }

    @Override public int hashCode() { return this.handle.id().hashCode(); }
    @Override public boolean equals(Object other) {
        return this == other || (other instanceof CraftAdvancement craft && this.handle.id().equals(craft.handle.id()));
    }
    @Override public String toString() { return "CraftAdvancement[" + this.handle.id() + "]"; }
}
