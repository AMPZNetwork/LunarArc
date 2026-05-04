package io.ampznetwork.lunararc.neoforge.server;

import io.ampznetwork.lunararc.common.server.LunarArcServer;
import io.ampznetwork.lunararc.neoforge.permissions.LunarArcPermissionHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;

@EventBusSubscriber(modid = "lunararc", bus = EventBusSubscriber.Bus.GAME)
public class NeoForgeServer extends LunarArcServer {
    public NeoForgeServer(MinecraftServer server) {
        super(server);
    }

    @Override
    public String getName() {
        return "LunarArc-NeoForge";
    }

    @SubscribeEvent
    public static void onPermissionGather(PermissionGatherEvent.Handler event) {
        event.addPermissionHandler(
            ResourceLocation.fromNamespaceAndPath("lunararc", "bukkit"),
            LunarArcPermissionHandler::new
        );
    }
}
