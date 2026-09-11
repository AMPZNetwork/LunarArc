package io.lunararcdevs.lunararc.common.mixin.core.inventory;

/**
 * Retired in Runtime Fix 04.
 * CraftingMenu fields are accessed directly through LunarArc's access widener.
 * This inert marker remains so overlay updates cannot leave an obsolete accessor behind.
 */
final class CraftingMenuAccessor {
    private CraftingMenuAccessor() {
    }
}
