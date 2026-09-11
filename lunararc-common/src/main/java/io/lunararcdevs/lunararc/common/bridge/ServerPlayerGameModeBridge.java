package io.lunararcdevs.lunararc.common.bridge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface ServerPlayerGameModeBridge {
    boolean lunararc$firedInteract();

    boolean lunararc$interactResult();

    BlockPos lunararc$interactPosition();

    InteractionHand lunararc$interactHand();

    ItemStack lunararc$interactItemStack();

    void lunararc$clearFiredInteract();
}
