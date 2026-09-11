package io.lunararcdevs.lunararc.common.mixin.core.alchemy;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.lunararcdevs.lunararc.common.bridge.alchemy.BrewingStandBridge;
import io.lunararcdevs.lunararc.common.bridge.alchemy.PotionBrewingBridge;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Allows Paper custom potion inputs into the three brewing input slots. */
@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin implements BrewingStandBridge {
    @Shadow int brewTime;
    @Shadow int fuel;

    @Unique private static final ThreadLocal<ArrayDeque<ItemStack>> lunararc$brewResults = new ThreadLocal<>();

    @Override public int lunararc$getBrewTime() { return brewTime; }
    @Override public void lunararc$setBrewTime(int ticks) { brewTime = ticks; }
    @Override public int lunararc$getFuel() { return fuel; }
    @Override public void lunararc$setFuel(int fuel) { this.fuel = fuel; }

    @WrapMethod(method = "serverTick")
    private static void lunararc$fuelEvent(Level level, BlockPos pos, BlockState state,
            BrewingStandBlockEntity stand, Operation<Void> original) {
        BrewingStandBridge bridge = (BrewingStandBridge) stand;
        ItemStack fuelStack = stand.getItem(4);
        if (bridge.lunararc$getFuel() <= 0 && fuelStack.is(Items.BLAZE_POWDER) && level instanceof ServerLevel serverLevel) {
            org.bukkit.event.inventory.BrewingStandFuelEvent event = new org.bukkit.event.inventory.BrewingStandFuelEvent(
                    org.bukkit.craftbukkit.block.CraftBlock.create(serverLevel, pos),
                    org.bukkit.craftbukkit.inventory.CraftItemStack.asBukkitCopy(fuelStack), 20);
            org.bukkit.Bukkit.getPluginManager().callEvent(event);

            int power = Math.max(0, event.getFuelPower());
            if (event.isCancelled() || power == 0) {
                // Hide only the fuel stack from the vanilla refuel prelude. All other tick logic still runs.
                ItemStack saved = fuelStack.copy();
                stand.setItem(4, ItemStack.EMPTY);
                try {
                    original.call(level, pos, state, stand);
                } finally {
                    stand.setItem(4, saved);
                }
                return;
            }

            bridge.lunararc$setFuel(power);
            if (event.isConsuming()) fuelStack.shrink(1);
            stand.setChanged();
        }
        original.call(level, pos, state, stand);
    }

    @WrapMethod(method = "doBrew")
    private static void lunararc$brewEvent(Level level, BlockPos pos, NonNullList<ItemStack> items, Operation<Void> original) {
        if (!(level instanceof ServerLevel serverLevel)
                || !(level.getBlockEntity(pos) instanceof BrewingStandBlockEntity stand)) {
            original.call(level, pos, items);
            return;
        }

        PotionBrewing brewing = level.potionBrewing();
        ItemStack ingredient = items.get(3);
        List<org.bukkit.inventory.ItemStack> results = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            results.add(org.bukkit.craftbukkit.inventory.CraftItemStack.asBukkitCopy(
                    brewing.mix(ingredient, items.get(i))));
        }

        org.bukkit.event.inventory.BrewEvent event = new org.bukkit.event.inventory.BrewEvent(
                org.bukkit.craftbukkit.block.CraftBlock.create(serverLevel, pos),
                new org.bukkit.craftbukkit.inventory.CraftBrewerInventory(stand),
                results, ((BrewingStandBridge) stand).lunararc$getFuel());
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        ArrayDeque<ItemStack> queue = new ArrayDeque<>(3);
        List<org.bukkit.inventory.ItemStack> chosen = event.getResults();
        for (int i = 0; i < 3; i++) {
            org.bukkit.inventory.ItemStack result = i < chosen.size() ? chosen.get(i) : null;
            ItemStack nms = org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(result);
            queue.addLast(nms.isEmpty() ? ItemStack.EMPTY : nms);
        }

        lunararc$brewResults.set(queue);
        try {
            original.call(level, pos, items);
        } finally {
            lunararc$brewResults.remove();
        }
    }

    @Redirect(method = "doBrew", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/alchemy/PotionBrewing;mix(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"), require = 0)
    private static ItemStack lunararc$applyBukkitBrewResult(PotionBrewing brewing, ItemStack ingredient, ItemStack input) {
        ArrayDeque<ItemStack> queue = lunararc$brewResults.get();
        if (queue != null && !queue.isEmpty()) return queue.removeFirst();
        return brewing.mix(ingredient, input);
    }

    @Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true)
    private void lunararc$allowPaperPotionInput(int slot, ItemStack stack,
            CallbackInfoReturnable<Boolean> cir) {
        if (slot < 0 || slot > 2) return;
        BrewingStandBlockEntity self = (BrewingStandBlockEntity) (Object) this;
        if (self.getLevel() == null || !self.getItem(slot).isEmpty()) return;
        Object brewing = self.getLevel().potionBrewing();
        if (brewing instanceof PotionBrewingBridge bridge && bridge.lunararc$isCustomInput(stack)) {
            cir.setReturnValue(true);
        }
    }
}
