package io.lunararcdevs.lunararc.common.bridge;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface AbstractContainerMenuBridge {
    @Nullable ServerPlayer lunararc$getOwner();
    void lunararc$setOwner(@Nullable ServerPlayer owner);
    boolean lunararc$getCheckReachable();
    void lunararc$setCheckReachable(boolean checkReachable);

    @Nullable org.bukkit.inventory.InventoryView lunararc$getBukkitView();

    void lunararc$setBukkitView(@Nullable org.bukkit.inventory.InventoryView view);
}
