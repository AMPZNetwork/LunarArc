package io.lunararcdevs.lunararc.common.mixin.core.server;

import io.lunararcdevs.lunararc.common.bridge.ServerPlayerGameModeBridge;
import net.minecraft.core.BlockPos;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin implements ServerPlayerGameModeBridge {

    @Shadow
    @Final
    protected ServerPlayer player;

    @Unique private boolean lunararc$firedInteract;
    @Unique private boolean lunararc$interactResult;
    @Unique private BlockPos lunararc$interactPosition;
    @Unique private InteractionHand lunararc$interactHand;
    @Unique private ItemStack lunararc$interactItemStack;

    @Override public boolean lunararc$firedInteract() { return this.lunararc$firedInteract; }
    @Override public boolean lunararc$interactResult() { return this.lunararc$interactResult; }
    @Override public BlockPos lunararc$interactPosition() { return this.lunararc$interactPosition; }
    @Override public InteractionHand lunararc$interactHand() { return this.lunararc$interactHand; }
    @Override public ItemStack lunararc$interactItemStack() { return this.lunararc$interactItemStack; }
    @Override public void lunararc$clearFiredInteract() { this.lunararc$firedInteract = false; }

    /**
     * Real CraftBukkit's {@code useItemOn} fires exactly one {@code RIGHT_CLICK_BLOCK}
     * {@link org.bukkit.event.player.PlayerInteractEvent} and records the block/hand/item it
     * fired for, so the network-layer {@code handleUseItem} handler - which runs right after,
     * for the same physical click, whenever this block use did not consume the interaction -
     * can reuse the result instead of firing a second, wrongly-classified {@code RIGHT_CLICK_AIR}
     * event. Denying {@code useInteractedBlock()} cancels the block-half of the interaction and
     * resyncs the client; denying {@code useItemInHand()} only marks the dedup state so the
     * item-half is skipped downstream.
     */
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void lunararc$onUseItemOn(ServerPlayer player, Level level, net.minecraft.world.item.ItemStack stack, InteractionHand hand,
            BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        io.lunararcdevs.lunararc.common.server.LunarArcContext.setCurrentPlayer(player);

        org.bukkit.event.player.PlayerInteractEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerInteractEvent(
            player,
            org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK,
            hitResult.getBlockPos(),
            hitResult.getDirection(),
            stack,
            hand == InteractionHand.OFF_HAND ? org.bukkit.inventory.EquipmentSlot.OFF_HAND : org.bukkit.inventory.EquipmentSlot.HAND
        );
        if (event == null) return;

        this.lunararc$firedInteract = true;
        this.lunararc$interactResult = event.useItemInHand() == org.bukkit.event.Event.Result.DENY;
        this.lunararc$interactPosition = hitResult.getBlockPos().immutable();
        this.lunararc$interactHand = hand;
        this.lunararc$interactItemStack = stack.copy();

        if (event.useInteractedBlock() == org.bukkit.event.Event.Result.DENY) {
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(
                    player.serverLevel(), hitResult.getBlockPos()));
            Object bukkit = ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) player).lunararc$getBukkitEntity();
            if (bukkit instanceof org.bukkit.craftbukkit.entity.CraftPlayer craftPlayer) craftPlayer.updateInventory();
            cir.setReturnValue(event.useItemInHand() != org.bukkit.event.Event.Result.ALLOW
                    ? InteractionResult.SUCCESS : InteractionResult.PASS);
        }
    }

    /**
     * No Bukkit event belongs here - real CraftBukkit's {@code useItem} is otherwise vanilla.
     * The {@code RIGHT_CLICK_AIR}/{@code RIGHT_CLICK_BLOCK} event for this action already fired
     * at the network layer ({@code handleUseItem}), before {@code gameMode.useItem()} was called.
     */
    @Inject(method = "useItem", at = @At("HEAD"))
    private void lunararc$onUseItem(ServerPlayer player, Level level, net.minecraft.world.item.ItemStack stack, InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        io.lunararcdevs.lunararc.common.server.LunarArcContext.setCurrentPlayer(player);
    }

    @Inject(method = "useItemOn", at = @At("RETURN"))
    private void lunararc$afterUseItemOn(ServerPlayer player, Level level, net.minecraft.world.item.ItemStack stack, InteractionHand hand,
            BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        io.lunararcdevs.lunararc.common.server.LunarArcContext.clear();
    }

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"), cancellable = true)
    private void lunararc$onBlockBreakAction(net.minecraft.core.BlockPos pos,
            net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action action,
            net.minecraft.core.Direction direction, int worldHeight, int sequence,
            CallbackInfo ci) {


        if (action != net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            return;
        }
        io.lunararcdevs.lunararc.common.server.LunarArcContext.setCurrentPlayer(this.player);
        try {
            org.bukkit.event.player.PlayerInteractEvent event =
                    org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerInteractEvent(
                            this.player,
                            org.bukkit.event.block.Action.LEFT_CLICK_BLOCK,
                            pos,
                            direction,
                            this.player.getMainHandItem(),
                            org.bukkit.inventory.EquipmentSlot.HAND);
            if (event != null && (event.isCancelled()
                    || event.useInteractedBlock() == org.bukkit.event.Event.Result.DENY
                    || event.useItemInHand() == org.bukkit.event.Event.Result.DENY)) {
                this.player.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(
                        this.player.serverLevel(), pos));
                ci.cancel();
            }
            // Real Paper's onPlayerLeftClickBlock anti-xray hook: starting to break a block near a
            // hidden ore reveals it, the same as actually breaking a neighbouring block would.
            io.lunararcdevs.lunararc.common.server.LunarArcAntiXrayEngine.forLevel(this.player.serverLevel())
                    .onBlockInteractStart(this.player.serverLevel(), pos);
        } finally {
            io.lunararcdevs.lunararc.common.server.LunarArcContext.clear();
        }
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void lunararc$onDestroyBlock(net.minecraft.core.BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        org.bukkit.event.block.BlockBreakEvent event =
                io.lunararcdevs.lunararc.common.mod.util.LunarArcBlockBreakCapture.matching(player, pos);
        if (event == null) {
            event = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockBreakEvent(player.serverLevel(), pos, player);
            io.lunararcdevs.lunararc.common.mod.util.LunarArcBlockBreakCapture.capture(player, pos, event);
        }
        boolean loaderOwnsCancellation = ((io.lunararcdevs.lunararc.common.bridge.MinecraftServerBridge) (Object) player.server)
                .lunararc$loaderHandlesBlockBreakEvent();
        if (event.isCancelled() && !loaderOwnsCancellation) {
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(player.serverLevel(), pos));
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void lunararc$clearBlockBreakCapture(net.minecraft.core.BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        io.lunararcdevs.lunararc.common.mod.util.LunarArcBlockBreakCapture.clear();
    }

    @Inject(method = "useItem", at = @At("RETURN"))
    private void lunararc$afterUseItem(ServerPlayer player, Level level, net.minecraft.world.item.ItemStack stack, InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        io.lunararcdevs.lunararc.common.server.LunarArcContext.clear();
    }
}
