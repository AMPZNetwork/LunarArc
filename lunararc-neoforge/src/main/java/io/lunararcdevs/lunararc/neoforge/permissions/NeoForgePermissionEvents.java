package io.lunararcdevs.lunararc.neoforge.permissions;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;

@EventBusSubscriber(modid = "lunararc")
public final class NeoForgePermissionEvents {
    private NeoForgePermissionEvents() {}

    @SubscribeEvent
    public static void onPermissionGather(PermissionGatherEvent.Handler event) {
        event.addPermissionHandler(
                ResourceLocation.fromNamespaceAndPath("lunararc", "bukkit"),
                LunarArcPermissionHandler::new);
    }
}
