package io.lunararcdevs.lunararc.common.mixin.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import io.lunararcdevs.lunararc.common.server.LunarArcCommandMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public abstract class CommandsMixin {

    @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lunararc$onCommandsInit(Commands.CommandSelection selection,
                                          net.minecraft.commands.CommandBuildContext context,
                                          CallbackInfo ci) {
        this.lunararc$registerMinecraftNamespaceAliases();
        LunarArcCommandMap.setDispatcher(this.dispatcher);
    }

    @Unique
    private void lunararc$registerMinecraftNamespaceAliases() {
        for (CommandNode<CommandSourceStack> node
                : new java.util.ArrayList<>(this.dispatcher.getRoot().getChildren())) {
            String name = node.getName();
            // Already namespaced, or already aliased: nothing to add.
            if (name.indexOf(':') >= 0) continue;
            String alias = "minecraft:" + name;
            if (this.dispatcher.getRoot().getChild(alias) != null) continue;

            CommandNode<CommandSourceStack> target = node;
            while (target.getRedirect() != null) target = target.getRedirect();

            this.dispatcher.register(
                    LiteralArgumentBuilder.<CommandSourceStack>literal(alias)
                            .executes(target.getCommand())
                            .requires(target.getRequirement())
                            .redirect(target));
        }
    }
}
