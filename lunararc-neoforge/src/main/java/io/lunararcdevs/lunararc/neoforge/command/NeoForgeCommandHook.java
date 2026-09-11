package io.lunararcdevs.lunararc.neoforge.command;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import io.lunararcdevs.lunararc.common.mod.server.LunarArcServer;
import io.lunararcdevs.lunararc.common.server.LunarArcCommandRouter;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.CommandEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;

public final class NeoForgeCommandHook {
    private NeoForgeCommandHook() {}

    public static void install() {
        LunarArcCommandRouter.installPlatformCommandHook(NeoForgeCommandHook::apply);
    }

    private static LunarArcCommandRouter.HookResult apply(CraftServer server, CommandSender sender, String commandLine) {
        CommandSourceStack source = source(sender);
        if (source == null) {
            return LunarArcCommandRouter.HookResult.pass(commandLine);
        }

        StringReader reader = new StringReader(commandLine);
        if (reader.canRead() && reader.peek() == '/') reader.skip();
        ParseResults<CommandSourceStack> parse = LunarArcServer.requireMinecraftServer()
                .getCommands().getDispatcher().parse(reader, source);
        CommandEvent event = new CommandEvent(parse);
        if (NeoForge.EVENT_BUS.post(event).isCanceled() || event.getException() != null) {
            return LunarArcCommandRouter.HookResult.cancel();
        }

        String rewritten = event.getParseResults().getReader().getString();
        if (rewritten.startsWith("/")) rewritten = rewritten.substring(1);
        return LunarArcCommandRouter.HookResult.pass(rewritten);
    }

    private static CommandSourceStack source(CommandSender sender) {
        if (sender instanceof CraftEntity craftEntity) {
            return craftEntity.getHandle().createCommandSourceStack();
        }
        if (sender == Bukkit.getConsoleSender()) {
            return LunarArcServer.requireMinecraftServer().createCommandSourceStack();
        }
        return null;
    }
}
