package org.bukkit.craftbukkit.block.banner;

import net.minecraft.world.level.block.entity.BannerPattern;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.PatternType;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("removal")
public final class CraftPatternType implements PatternType, org.bukkit.craftbukkit.util.Handleable<BannerPattern> {
    private static final AtomicInteger NEXT_ORDINAL = new AtomicInteger();
    private final NamespacedKey key;
    private final BannerPattern handle;
    private final int ordinal = NEXT_ORDINAL.getAndIncrement();

    public CraftPatternType(@NotNull NamespacedKey key, @NotNull BannerPattern handle) {
        this.key = Objects.requireNonNull(key, "key");
        this.handle = Objects.requireNonNull(handle, "handle");
    }

    public @NotNull BannerPattern getHandle() { return this.handle; }
    @Override public @NotNull NamespacedKey getKey() { return this.key; }
    @Override public String name() { return NamespacedKey.MINECRAFT.equals(this.key.getNamespace())
            ? this.key.getKey().toUpperCase(Locale.ROOT) : this.key.toString(); }
    @Override public int ordinal() { return this.ordinal; }
    @Override public int compareTo(PatternType other) { return Integer.compare(this.ordinal, other.ordinal()); }
    @Override public String toString() { return this.name(); }

    @Override
    public String getIdentifier() {
        return switch (this.name()) {
            case "BASE" -> "b"; case "SQUARE_BOTTOM_LEFT" -> "bl"; case "SQUARE_BOTTOM_RIGHT" -> "br";
            case "SQUARE_TOP_LEFT" -> "tl"; case "SQUARE_TOP_RIGHT" -> "tr"; case "STRIPE_BOTTOM" -> "bs";
            case "STRIPE_TOP" -> "ts"; case "STRIPE_LEFT" -> "ls"; case "STRIPE_RIGHT" -> "rs";
            case "STRIPE_CENTER" -> "cs"; case "STRIPE_MIDDLE" -> "ms"; case "STRIPE_DOWNRIGHT" -> "drs";
            case "STRIPE_DOWNLEFT" -> "dls"; case "SMALL_STRIPES" -> "ss"; case "CROSS" -> "cr";
            case "STRAIGHT_CROSS" -> "sc"; case "TRIANGLE_BOTTOM" -> "bt"; case "TRIANGLE_TOP" -> "tt";
            case "TRIANGLES_BOTTOM" -> "bts"; case "TRIANGLES_TOP" -> "tts"; case "DIAGONAL_LEFT" -> "ld";
            case "DIAGONAL_UP_RIGHT" -> "rd"; case "DIAGONAL_UP_LEFT" -> "lud"; case "DIAGONAL_RIGHT" -> "rud";
            case "CIRCLE" -> "mc"; case "RHOMBUS" -> "mr"; case "HALF_VERTICAL" -> "vh";
            case "HALF_HORIZONTAL" -> "hh"; case "HALF_VERTICAL_RIGHT" -> "vhr"; case "HALF_HORIZONTAL_BOTTOM" -> "hhb";
            case "BORDER" -> "bo"; case "CURLY_BORDER" -> "cbo"; case "CREEPER" -> "cre";
            case "GRADIENT" -> "gra"; case "GRADIENT_UP" -> "gru"; case "BRICKS" -> "bri";
            case "SKULL" -> "sku"; case "FLOWER" -> "flo"; case "MOJANG" -> "moj";
            case "GLOBE" -> "glb"; case "PIGLIN" -> "pig"; case "FLOW" -> "flw"; case "GUSTER" -> "gus";
            default -> this.key.toString();
        };
    }

    @Override public boolean equals(Object other) {
        return this == other || (other instanceof PatternType pattern && this.key.equals(pattern.getKey()));
    }
    @Override public int hashCode() { return this.key.hashCode(); }

    // CraftBukkit's conversion pair for this registry type. Plugins call these directly to cross
    // between the Bukkit handle and the NMS object, so they carry CraftBukkit's names verbatim.
    public static PatternType minecraftToBukkit(BannerPattern minecraft) {
        return org.bukkit.craftbukkit.CraftRegistry.minecraftToBukkit(minecraft, net.minecraft.core.registries.Registries.BANNER_PATTERN, org.bukkit.Registry.BANNER_PATTERN);
    }

    public static PatternType minecraftHolderToBukkit(net.minecraft.core.Holder<BannerPattern> minecraft) {
        return CraftPatternType.minecraftToBukkit(minecraft.value());
    }

    public static BannerPattern bukkitToMinecraft(PatternType bukkit) {
        return org.bukkit.craftbukkit.CraftRegistry.bukkitToMinecraft(bukkit);
    }

    public static net.minecraft.core.Holder<BannerPattern> bukkitToMinecraftHolder(PatternType bukkit) {
        com.google.common.base.Preconditions.checkArgument(bukkit != null);

        net.minecraft.core.Registry<BannerPattern> registry = org.bukkit.craftbukkit.CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.BANNER_PATTERN);

        if (registry.wrapAsHolder(CraftPatternType.bukkitToMinecraft(bukkit)) instanceof net.minecraft.core.Holder.Reference<BannerPattern> holder) {
            return holder;
        }

        throw new IllegalArgumentException("No Reference holder found for " + bukkit
                + ", this can happen if a plugin creates its own banner pattern without properly registering it.");
    }
}
