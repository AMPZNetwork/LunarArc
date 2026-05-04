package io.ampznetwork.lunararc.common.mixin.core.server;

import io.ampznetwork.lunararc.common.LunarArcPlatform;
import io.ampznetwork.lunararc.common.stubs.org.bukkit.craftbukkit.v1_21_R1.CraftPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {

    @Shadow @Final protected ServerPlayer player;

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void lunararc$onUseItemOn(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        try {
            Object craftServer = LunarArcPlatform.getServer();
            if (craftServer == null) return;

            Class<?> eventClass = Class.forName("org.bukkit.event.player.PlayerInteractEvent");
            Class<?> actionClass = Class.forName("org.bukkit.event.block.Action");
            Object bukkitPlayer = new CraftPlayer(this.player);
            Object action = actionClass.getField("RIGHT_CLICK_BLOCK").get(null);

            // PlayerInteractEvent(Player who, Action action, ItemStack item, Block block, BlockFace face)
            Object event = eventClass.getConstructor(Class.forName("org.bukkit.entity.Player"), actionClass, Class.forName("org.bukkit.inventory.ItemStack"), Class.forName("org.bukkit.block.Block"), Class.forName("org.bukkit.block.BlockFace"))
                    .newInstance(bukkitPlayer, action, null, null, null);

            Object pm = craftServer.getClass().getMethod("getPluginManager").invoke(craftServer);
            pm.getClass().getMethod("callEvent", Class.forName("org.bukkit.event.Event")).invoke(pm, event);
            
            Boolean isCancelled = (Boolean) eventClass.getMethod("isCancelled").invoke(event);
            if (isCancelled) {
                cir.setReturnValue(InteractionResult.FAIL);
            }
        } catch (Throwable ignored) {}
    }

    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void lunararc$onUseItem(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        try {
            Object craftServer = LunarArcPlatform.getServer();
            if (craftServer == null) return;

            Class<?> eventClass = Class.forName("org.bukkit.event.player.PlayerInteractEvent");
            Class<?> actionClass = Class.forName("org.bukkit.event.block.Action");
            Object bukkitPlayer = new CraftPlayer(this.player);
            Object action = actionClass.getField("RIGHT_CLICK_AIR").get(null);

            Object event = eventClass.getConstructor(Class.forName("org.bukkit.entity.Player"), actionClass, Class.forName("org.bukkit.inventory.ItemStack"), Class.forName("org.bukkit.block.Block"), Class.forName("org.bukkit.block.BlockFace"))
                    .newInstance(bukkitPlayer, action, null, null, null);

            Object pm = craftServer.getClass().getMethod("getPluginManager").invoke(craftServer);
            pm.getClass().getMethod("callEvent", Class.forName("org.bukkit.event.Event")).invoke(pm, event);
            
            Boolean isCancelled = (Boolean) eventClass.getMethod("isCancelled").invoke(event);
            if (isCancelled) {
                cir.setReturnValue(InteractionResult.FAIL);
            }
        } catch (Throwable ignored) {}
    }
}
