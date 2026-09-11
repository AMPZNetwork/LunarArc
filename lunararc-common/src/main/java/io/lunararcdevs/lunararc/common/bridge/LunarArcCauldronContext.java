package io.lunararcdevs.lunararc.common.bridge;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/** Per-use context for the vanilla 1.21.1 cauldron interaction helpers. */
public final class LunarArcCauldronContext {
    private static final ThreadLocal<Direction> DIRECTION = new ThreadLocal<>();
    private static final ThreadLocal<ItemStack> RESULT = new ThreadLocal<>();

    private LunarArcCauldronContext() {}

    public static void setDirection(Direction direction) { DIRECTION.set(direction); }
    public static Direction direction() { return DIRECTION.get(); }
    public static void setResult(ItemStack result) { RESULT.set(result); }
    public static ItemStack takeResult() {
        ItemStack result = RESULT.get();
        RESULT.remove();
        return result;
    }
    public static void clear() {
        DIRECTION.remove();
        RESULT.remove();
    }
}
