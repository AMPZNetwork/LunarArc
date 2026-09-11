package io.lunararcdevs.lunararc.common.bridge;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.event.player.PlayerUnregisterChannelEvent;
import org.bukkit.plugin.messaging.StandardMessenger;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface ServerCommonPacketListenerBridge {
    boolean DISABLE_CHANNEL_LIMIT = System.getProperty("paper.disableChannelLimit") != null;

    Connection lunararc$getConnection();

    ServerPlayer lunararc$getPlayer();
    void lunararc$setPlayer(ServerPlayer player);

    /** Combined Bukkit-visible channel set. */
    Set<String> lunararc$getPluginChannels();

    /** Channels explicitly advertised through vanilla minecraft:register. */
    Set<String> lunararc$getVanillaPluginChannels();

    /** Channels negotiated/advertised by the active modloader. */
    Set<String> lunararc$getLoaderPluginChannels();

    String lunararc$getClientBrand();
    void lunararc$setClientBrand(String brand);

    boolean lunararc$isTransferred();
    CompletableFuture<byte[]> lunararc$retrieveCookie(NamespacedKey key);
    void lunararc$storeCookie(NamespacedKey key, byte[] value);

    default boolean lunararc$addPluginChannel(Player player, String channel) {
        return lunararc$addChannelFromSource(player, channel, lunararc$getVanillaPluginChannels());
    }

    default boolean lunararc$removePluginChannel(Player player, String channel) {
        return lunararc$removeChannelFromSource(player, channel, lunararc$getVanillaPluginChannels(), java.util.Collections.emptySet());
    }

    /**
     * Track a loader-native payload channel without exposing it as a Bukkit plugin
     * messaging registration. Paper's listening-channel set is populated by the
     * client's minecraft:register payload and is capped at 128 entries; a modloader
     * may legitimately negotiate hundreds of native payload ids and those must not
     * consume that Bukkit limit or fire PlayerRegisterChannelEvent.
     */
    default boolean lunararc$addLoaderPluginChannel(Player player, String channel) {
        String corrected = StandardMessenger.validateAndCorrectChannel(channel);
        return lunararc$getLoaderPluginChannels().add(corrected);
    }

    default boolean lunararc$removeLoaderPluginChannel(Player player, String channel) {
        String corrected = StandardMessenger.validateAndCorrectChannel(channel);
        return lunararc$getLoaderPluginChannels().remove(corrected);
    }

    default void lunararc$replaceLoaderPluginChannels(Player player, java.util.Collection<String> channels) {
        java.util.LinkedHashSet<String> corrected = new java.util.LinkedHashSet<>();
        for (String channel : channels) corrected.add(StandardMessenger.validateAndCorrectChannel(channel));

        Set<String> loaderChannels = lunararc$getLoaderPluginChannels();
        loaderChannels.retainAll(corrected);
        loaderChannels.addAll(corrected);
    }

    private boolean lunararc$addChannelFromSource(Player player, String channel, Set<String> source) {
        String corrected = StandardMessenger.validateAndCorrectChannel(channel);
        if (!source.add(corrected)) return false;

        Set<String> combined = lunararc$getPluginChannels();
        boolean fireEvent;
        synchronized (combined) {
            if (combined.contains(corrected)) return false;
            if (!DISABLE_CHANNEL_LIMIT && combined.size() >= 128) {
                source.remove(corrected);
                throw new IllegalStateException("Cannot register channel. Too many channels registered!");
            }
            fireEvent = combined.add(corrected);
        }
        if (fireEvent) lunararc$fireChannelEvent(new PlayerRegisterChannelEvent(player, corrected));
        return fireEvent;
    }

    private boolean lunararc$removeChannelFromSource(Player player, String channel, Set<String> source, Set<String> otherSource) {
        String corrected = StandardMessenger.validateAndCorrectChannel(channel);
        if (!source.remove(corrected)) return false;

        Set<String> combined = lunararc$getPluginChannels();
        boolean fireEvent;
        synchronized (combined) {
            if (otherSource.contains(corrected)) return false;
            fireEvent = combined.remove(corrected);
        }
        if (fireEvent) lunararc$fireChannelEvent(new PlayerUnregisterChannelEvent(player, corrected));
        return fireEvent;
    }

    private static void lunararc$fireChannelEvent(org.bukkit.event.Event event) {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getPluginManager().callEvent(event);
            return;
        }

        // Loader/network callbacks may arrive off-thread. Keep the channel data update
        // thread-safe, but always marshal Bukkit event delivery back to the server thread.
        org.bukkit.Server server = Bukkit.getServer();
        if (server instanceof org.bukkit.craftbukkit.CraftServer craftServer) {
            craftServer.getServer().execute(() -> Bukkit.getPluginManager().callEvent(event));
        }
    }

}
