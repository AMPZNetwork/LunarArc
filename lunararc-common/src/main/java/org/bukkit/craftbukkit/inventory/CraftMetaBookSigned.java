package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.server.network.FilteredText;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Concrete signed-book meta backed by Minecraft 1.21.1 WRITTEN_BOOK_CONTENT. */
public final class CraftMetaBookSigned extends CraftItemMeta implements BookMeta {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final int MAX_PAGE_LENGTH = WritableBookContent.PAGE_EDIT_LENGTH;
    private static final int MAX_TITLE_LENGTH = WrittenBookContent.TITLE_MAX_LENGTH;
    private String title;
    private String author;
    private List<Component> pages;
    private boolean resolved;
    private int generation;
    private BookMeta.Spigot spigot = new SpigotMeta();

    public CraftMetaBookSigned() { super(); }
    public CraftMetaBookSigned(ItemStack nms) {
        super(nms);
        if (nms == null || nms.isEmpty()) return;
        WrittenBookContent content = nms.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (content == null) return;
        this.title = content.title().raw();
        this.author = content.author();
        this.generation = content.generation();
        this.resolved = content.resolved();
        this.pages = new ArrayList<>();
        for (Filterable<net.minecraft.network.chat.Component> page : content.pages()) this.pages.add(fromNms(page.raw()));
    }

    @Override public void applyToNms(ItemStack nms) {
        super.applyToNms(nms);
        List<Filterable<net.minecraft.network.chat.Component>> encoded = new ArrayList<>();
        if (this.pages != null) for (Component page : this.pages) encoded.add(Filterable.passThrough(toNms(page)));
        nms.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.from(this.title == null ? FilteredText.EMPTY : FilteredText.passThrough(this.title)),
                this.author == null ? "" : this.author, this.generation, encoded, this.resolved));
    }

    private static Component fromNms(net.minecraft.network.chat.Component component) {
        try { return GsonComponentSerializer.gson().deserialize(net.minecraft.network.chat.Component.Serializer.toJson(component, net.minecraft.core.RegistryAccess.EMPTY)); }
        catch (Throwable ignored) { return Component.text(component == null ? "" : component.getString()); }
    }
    private static net.minecraft.network.chat.Component toNms(Component component) {
        try { return net.minecraft.network.chat.Component.Serializer.fromJson(GsonComponentSerializer.gson().serialize(component), net.minecraft.core.RegistryAccess.EMPTY); }
        catch (Throwable ignored) { return net.minecraft.network.chat.Component.literal(LEGACY.serialize(component)); }
    }
    private String validatePage(String page) { if (page == null) return ""; return page.length() > MAX_PAGE_LENGTH ? page.substring(0, MAX_PAGE_LENGTH) : page; }
    private boolean validPage(int page) { return page > 0 && page <= getPageCount(); }
    private void addInternal(Component page) { if (this.pages == null) this.pages = new ArrayList<>(); this.pages.add(page == null ? Component.empty() : page); }

    @Override public boolean hasTitle() { return this.title != null; }
    @Override public @Nullable String getTitle() { return this.title; }
    @Override public boolean setTitle(@Nullable String title) { if (title != null && title.length() > MAX_TITLE_LENGTH) return false; this.title = title; return true; }
    @Override public boolean hasAuthor() { return this.author != null; }
    @Override public @Nullable String getAuthor() { return this.author; }
    @Override public void setAuthor(@Nullable String author) { this.author = author; }
    @Override public boolean hasGeneration() { return this.generation != 0; }
    @Override public @Nullable Generation getGeneration() { return this.generation >= 0 && this.generation < Generation.values().length ? Generation.values()[this.generation] : null; }
    @Override public void setGeneration(@Nullable Generation generation) { this.generation = generation == null ? 0 : generation.ordinal(); }
    public boolean isResolved() { return this.resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    @Override public boolean hasPages() { return this.pages != null && !this.pages.isEmpty(); }
    @Override public @NotNull String getPage(int page) { Preconditions.checkArgument(validPage(page), "Invalid page number (%s/%s)", page, getPageCount()); return LEGACY.serialize(this.pages.get(page - 1)); }
    @Override public void setPage(int page, @NotNull String data) { Preconditions.checkArgument(validPage(page), "Invalid page number (%s/%s)", page, getPageCount()); this.pages.set(page - 1, LEGACY.deserialize(validatePage(data))); }
    @Override public @NotNull List<String> getPages() { return this.pages == null ? List.of() : this.pages.stream().map(LEGACY::serialize).toList(); }
    @Override public void setPages(@NotNull List<String> pages) { this.pages = null; for (String page : pages) addInternal(LEGACY.deserialize(validatePage(page))); }
    @Override public void setPages(@NotNull String... pages) { setPages(Arrays.asList(pages)); }
    @Override public void addPage(@NotNull String... pages) { for (String page : pages) addInternal(LEGACY.deserialize(validatePage(page))); }
    @Override public int getPageCount() { return this.pages == null ? 0 : this.pages.size(); }

    @Override public @Nullable Component title() { return this.title == null ? null : LEGACY.deserialize(this.title); }
    @Override public @NotNull BookMeta title(@Nullable Component title) { return setTitle(title == null ? null : LEGACY.serialize(title)) ? this : this; }
    @Override public @Nullable Component author() { return this.author == null ? null : LEGACY.deserialize(this.author); }
    @Override public @NotNull BookMeta author(@Nullable Component author) { this.author = author == null ? null : LEGACY.serialize(author); return this; }
    @Override public @NotNull Component page(int page) { Preconditions.checkArgument(validPage(page), "Invalid page number (%s/%s)", page, getPageCount()); return this.pages.get(page - 1); }
    @Override public void page(int page, @NotNull Component data) { Preconditions.checkArgument(validPage(page), "Invalid page number (%s/%s)", page, getPageCount()); this.pages.set(page - 1, data); }
    @Override public void addPages(@NotNull Component @NotNull ... pages) { for (Component page : pages) addInternal(page); }
    @Override public @NotNull List<Component> pages() { return this.pages == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(this.pages)); }
    @Override public @NotNull BookMeta pages(@NotNull List<Component> pages) { this.pages = new ArrayList<>(pages); return this; }

    private static final class CraftMetaBookSignedBuilder extends CraftMetaBook.CraftMetaBookBuilder {
        private Component title;
        private Component author;
        @Override public BookMeta.BookMetaBuilder title(Component title) { this.title = title; return this; }
        @Override public BookMeta.BookMetaBuilder author(Component author) { this.author = author; return this; }
        @Override public BookMeta build() {
            CraftMetaBookSigned meta = new CraftMetaBookSigned();
            meta.title(this.title);
            meta.author(this.author);
            meta.pages(this.pages);
            return meta;
        }
    }
    @Override public BookMeta.BookMetaBuilder toBuilder() { return new CraftMetaBookSignedBuilder(); }

    @Override public CraftMetaBookSigned clone() { CraftMetaBookSigned c=(CraftMetaBookSigned)super.clone(); c.pages=this.pages==null?null:new ArrayList<>(this.pages); c.spigot=c.new SpigotMeta(); return c; }
    private final class SpigotMeta extends BookMeta.Spigot {
        @Override public BaseComponent[] getPage(int page) { return ComponentSerializer.parse(GsonComponentSerializer.gson().serialize(CraftMetaBookSigned.this.page(page))); }
        @Override public void setPage(int page, BaseComponent... data) { CraftMetaBookSigned.this.page(page, data == null ? Component.empty() : GsonComponentSerializer.gson().deserialize(ComponentSerializer.toString(data))); }
        @Override public List<BaseComponent[]> getPages() { return CraftMetaBookSigned.this.pages().stream().map(c -> ComponentSerializer.parse(GsonComponentSerializer.gson().serialize(c))).toList(); }
        @Override public void setPages(List<BaseComponent[]> pages) { CraftMetaBookSigned.this.pages = null; for (BaseComponent[] p : pages) addPage(p); }
        @Override public void setPages(BaseComponent[]... pages) { setPages(Arrays.asList(pages)); }
        @Override public void addPage(BaseComponent[]... pages) { for (BaseComponent[] p : pages) CraftMetaBookSigned.this.addInternal(p == null ? Component.empty() : GsonComponentSerializer.gson().deserialize(ComponentSerializer.toString(p))); }
    }
    @Override public BookMeta.@NotNull Spigot spigot() { return this.spigot; }
}
