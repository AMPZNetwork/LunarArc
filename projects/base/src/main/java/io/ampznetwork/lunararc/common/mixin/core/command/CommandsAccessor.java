package io.ampznetwork.lunararc.common.mixin.core.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Commands.class)
public interface CommandsAccessor {
    @Accessor("dispatcher")
    CommandDispatcher<CommandSourceStack> getDispatcher();
}
