package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.server.network.FilteredText;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.WritableBookMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Concrete writable-book meta backed by Minecraft 1.21.1 WRITABLE_BOOK_CONTENT. */
public class CraftMetaBook extends CraftItemMeta implements BookMeta, WritableBookMeta {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final int MAX_PAGES = WritableBookContent.MAX_PAGES;
    private static final int MAX_PAGE_LENGTH = WritableBookContent.PAGE_EDIT_LENGTH;
    protected List<String> pages;
    private BookMeta.Spigot spigot = new SpigotMeta();

    public CraftMetaBook() { super(); }
    public CraftMetaBook(ItemStack nms) {
        super(nms);
        if (nms == null || nms.isEmpty()) return;
        WritableBookContent content = nms.get(DataComponents.WRITABLE_BOOK_CONTENT);
        if (content == null) return;
        this.pages = new ArrayList<>();
        for (int i = 0; i < Math.min(content.pages().size(), MAX_PAGES); i++) {
            this.pages.add(validatePage(content.pages().get(i).raw()));
        }
    }

    @Override public void applyToNms(ItemStack nms) {
        super.applyToNms(nms);
        if (this.pages == null) {
            nms.remove(DataComponents.WRITABLE_BOOK_CONTENT);
            return;
        }
        List<Filterable<String>> encoded = new ArrayList<>(this.pages.size());
        for (String page : this.pages) encoded.add(Filterable.from(FilteredText.passThrough(page)));
        nms.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(encoded));
    }

    private String validatePage(String page) {
        if (page == null) return "";
        return page.length() > MAX_PAGE_LENGTH ? page.substring(0, MAX_PAGE_LENGTH) : page;
    }
    private boolean validPage(int page) { return page > 0 && page <= getPageCount(); }
    private void addInternal(String page) {
        if (this.pages == null) this.pages = new ArrayList<>();
        if (this.pages.size() < MAX_PAGES) this.pages.add(validatePage(page));
    }

    @Override public boolean hasPages() { return this.pages != null && !this.pages.isEmpty(); }
    @Override public @NotNull String getPage(int page) { Preconditions.checkArgument(validPage(page), "Invalid page number (%s/%s)", page, getPageCount()); return this.pages.get(page - 1); }
    @Override public void setPage(int page, @NotNull String data) { Preconditions.checkArgument(validPage(page), "Invalid page number (%s/%s)", page, getPageCount()); this.pages.set(page - 1, validatePage(data)); }
    @Override public @NotNull List<String> getPages() { return this.pages == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(this.pages)); }
    @Override public void setPages(@NotNull List<String> pages) { this.pages = null; for (String page : pages) addInternal(page); }
    @Override public void setPages(@NotNull String... pages) { setPages(Arrays.asList(pages)); }
    @Override public void addPage(@NotNull String... pages) { for (String page : pages) addInternal(page); }
    @Override public int getPageCount() { return this.pages == null ? 0 : this.pages.size(); }

    @Override public boolean hasTitle() { return false; }
    @Override public @Nullable String getTitle() { return null; }
    @Override public boolean setTitle(@Nullable String title) { return false; }
    @Override public boolean hasAuthor() { return false; }
    @Override public @Nullable String getAuthor() { return null; }
    @Override public void setAuthor(@Nullable String author) {}
    @Override public boolean hasGeneration() { return false; }
    @Override public @Nullable Generation getGeneration() { return null; }
    @Override public void setGeneration(@Nullable Generation generation) {}
    @Override public @Nullable Component title() { return null; }
    @Override public @NotNull BookMeta title(@Nullable Component title) { return this; }
    @Override public @Nullable Component author() { return null; }
    @Override public @NotNull BookMeta author(@Nullable Component author) { return this; }
    @Override public @NotNull Component page(int page) { return LEGACY.deserialize(getPage(page)); }
    @Override public void page(int page, @NotNull Component data) { setPage(page, LEGACY.serialize(data)); }
    @Override public void addPages(@NotNull Component @NotNull ... pages) { for (Component page : pages) addInternal(LEGACY.serialize(page == null ? Component.empty() : page)); }
    @Override public @NotNull List<Component> pages() { return getPages().stream().<Component>map(LEGACY::deserialize).toList(); }
    @Override public @NotNull BookMeta pages(@NotNull List<Component> pages) { this.pages = null; for (Component page : pages) addInternal(LEGACY.serialize(page == null ? Component.empty() : page)); return this; }

    protected static class CraftMetaBookBuilder implements BookMeta.BookMetaBuilder {
        protected final List<Component> pages = new ArrayList<>();
        @Override public BookMeta.BookMetaBuilder title(Component title) { return this; }
        @Override public BookMeta.BookMetaBuilder author(Component author) { return this; }
        @Override public BookMeta.BookMetaBuilder addPage(Component page) { this.pages.add(page == null ? Component.empty() : page); return this; }
        @Override public BookMeta.BookMetaBuilder pages(Component... pages) { if (pages != null) for (Component page : pages) addPage(page); return this; }
        @Override public BookMeta.BookMetaBuilder pages(java.util.Collection<Component> pages) { if (pages != null) for (Component page : pages) addPage(page); return this; }
        @Override public BookMeta build() { CraftMetaBook meta = new CraftMetaBook(); meta.pages(this.pages); return meta; }
    }
    @Override public BookMeta.BookMetaBuilder toBuilder() { return new CraftMetaBookBuilder(); }

    @Override public CraftMetaBook clone() {
        CraftMetaBook clone = (CraftMetaBook) super.clone();
        clone.pages = this.pages == null ? null : new ArrayList<>(this.pages);
        clone.spigot = clone.new SpigotMeta();
        return clone;
    }

    private final class SpigotMeta extends BookMeta.Spigot {
        @Override public BaseComponent[] getPage(int page) { return TextComponent.fromLegacyText(CraftMetaBook.this.getPage(page)); }
        @Override public void setPage(int page, BaseComponent... data) { CraftMetaBook.this.setPage(page, data == null ? "" : TextComponent.toLegacyText(data)); }
        @Override public List<BaseComponent[]> getPages() { return CraftMetaBook.this.getPages().stream().map(TextComponent::fromLegacyText).toList(); }
        @Override public void setPages(List<BaseComponent[]> pages) { CraftMetaBook.this.pages = null; for (BaseComponent[] p : pages) CraftMetaBook.this.addInternal(p == null ? "" : TextComponent.toLegacyText(p)); }
        @Override public void setPages(BaseComponent[]... pages) { setPages(Arrays.asList(pages)); }
        @Override public void addPage(BaseComponent[]... pages) { for (BaseComponent[] p : pages) CraftMetaBook.this.addInternal(p == null ? "" : TextComponent.toLegacyText(p)); }
    }
    @Override public BookMeta.@NotNull Spigot spigot() { return this.spigot; }
}
