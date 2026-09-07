package org.bukkit.craftbukkit.inventory;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.bukkit.Color;
import org.bukkit.inventory.meta.ColorableArmorMeta;

public final class CraftMetaColorableArmor extends CraftMetaArmor implements ColorableArmorMeta {
    private Color color;
    private boolean showInTooltip = true;

    public CraftMetaColorableArmor(ItemStack stack) {
        super(stack);
        if (stack == null) return;
        DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
        if (dyed != null) {
            color = Color.fromRGB(dyed.rgb());
            showInTooltip = dyed.showInTooltip();
        }
    }

    @Override public Color getColor() { return color == null ? Color.fromRGB(0xA06540) : color; }
    @Override public void setColor(Color color) { this.color = color; }
    @Override public boolean isDyed() { return color != null; }
    @Override public CraftMetaColorableArmor clone() { return (CraftMetaColorableArmor) super.clone(); }

    @Override public void applyToNms(ItemStack stack) {
        super.applyToNms(stack);
        if (color == null) stack.remove(DataComponents.DYED_COLOR);
        else stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color.asRGB(), showInTooltip));
    }

    @Override public java.util.Map<String, Object> serialize() {
        var values = super.serialize();
        if (color != null) values.put("color", color);
        return values;
    }
}
