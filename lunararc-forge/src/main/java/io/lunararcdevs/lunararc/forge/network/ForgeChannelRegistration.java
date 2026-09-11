package io.lunararcdevs.lunararc.forge.network;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.ServerCommonPacketListenerBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraftforge.event.network.ChannelRegistrationChangeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.bukkit.entity.Player;

public final class ForgeChannelRegistration {
    private ForgeChannelRegistration() {}

    public static void register(IEventBus bus) {
        bus.addListener(ForgeChannelRegistration::onRegistrationChange);
    }

    private static void onRegistrationChange(ChannelRegistrationChangeEvent event) {
        Object listener = event.getSource().getPacketListener();
        if (!(listener instanceof ServerCommonPacketListenerImpl common)) return;

        ServerCommonPacketListenerBridge bridge = (ServerCommonPacketListenerBridge) (Object) common;
        ServerPlayer serverPlayer = bridge.lunararc$getPlayer();
        if (serverPlayer == null) return;

        Runnable task = () -> {
            org.bukkit.entity.Entity entity = ((EntityBridge) (Object) serverPlayer).lunararc$getBukkitEntity();
            if (!(entity instanceof Player player)) return;

            switch (event.getType()) {
                case REGISTER -> event.getChannels().forEach(channel -> bridge.lunararc$addLoaderPluginChannel(player, channel.toString()));
                case UNREGISTER -> event.getChannels().forEach(channel -> bridge.lunararc$removeLoaderPluginChannel(player, channel.toString()));
            }
        };
        if (serverPlayer.server.isSameThread()) task.run();
        else serverPlayer.server.execute(task);
    }
}
