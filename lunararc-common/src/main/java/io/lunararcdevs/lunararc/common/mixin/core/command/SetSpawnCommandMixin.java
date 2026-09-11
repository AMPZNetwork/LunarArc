package io.lunararcdevs.lunararc.common.mixin.core.command;

import io.lunararcdevs.lunararc.common.bridge.ServerPlayerSpawnBridge;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.SetSpawnCommand;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.player.PlayerSpawnChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Carries the verified vanilla /spawnpoint cause into PlayerSpawnChangeEvent. */
@Mixin(SetSpawnCommand.class)
public abstract class SetSpawnCommandMixin {
    @Inject(method = "setSpawn", at = @At("HEAD"), require = 0)
    private static void lunararc$spawnCommandCause(
            CommandSourceStack source, Collection<ServerPlayer> players, BlockPos pos, float angle,
            CallbackInfoReturnable<Integer> cir) {
        for (ServerPlayer player : players) {
            ((ServerPlayerSpawnBridge) player)
                    .lunararc$pushSpawnChangeCause(PlayerSpawnChangeEvent.Cause.COMMAND);
        }
    }
}
