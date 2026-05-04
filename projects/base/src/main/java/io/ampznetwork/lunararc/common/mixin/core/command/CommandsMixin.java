package io.ampznetwork.lunararc.common.mixin.core.command;

import com.mojang.brigadier.CommandDispatcher;
import io.ampznetwork.lunararc.common.LunarArcPlatform;
import io.ampznetwork.lunararc.common.server.BukkitCommandWrapper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.bukkit.command.Command;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public abstract class CommandsMixin {

    @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lunararc$onCommandsInit(Commands.CommandSelection selection, net.minecraft.commands.CommandBuildContext context, CallbackInfo ci) {
        if (LunarArcPlatform.getServer() == null) return;

        org.bukkit.command.CommandMap commandMap = LunarArcPlatform.getServer().getCommandMap();
        if (commandMap instanceof org.bukkit.command.SimpleCommandMap scm) {
            for (Command command : scm.getKnownCommands().values()) {
                new BukkitCommandWrapper(command).register(this.dispatcher);
            }
        }
    }
}
