package io.lunararcdevs.lunararc.common.mixin.core.server;

import io.lunararcdevs.lunararc.common.bridge.ConnectionBridge;
import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.ServerCommonPacketListenerBridge;
import io.netty.buffer.Unpooled;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.ClientboundStoreCookiePacket;
import net.minecraft.network.protocol.cookie.ClientboundCookieRequestPacket;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin implements ServerCommonPacketListenerBridge {

    @Unique private static final ResourceLocation LUNARARC_REGISTER = ResourceLocation.withDefaultNamespace("register");
    @Unique private static final ResourceLocation LUNARARC_UNREGISTER = ResourceLocation.withDefaultNamespace("unregister");
    @Unique private static final ResourceLocation LUNARARC_BRAND = ResourceLocation.withDefaultNamespace("brand");

    @Shadow @Final protected MinecraftServer server;
    @Shadow @Final public Connection connection;

    @Unique protected ServerPlayer lunararc$player;
    @Unique private final Set<String> lunararc$pluginChannels = ConcurrentHashMap.newKeySet();
    @Unique private final Set<String> lunararc$vanillaPluginChannels = ConcurrentHashMap.newKeySet();
    @Unique private final Set<String> lunararc$loaderPluginChannels = ConcurrentHashMap.newKeySet();
    @Unique private final java.util.Map<ResourceLocation, CompletableFuture<byte[]>> lunararc$requestedCookies = new ConcurrentHashMap<>();
    @Unique private String lunararc$clientBrand;
    @Unique private boolean lunararc$transferred;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lunararc$init(MinecraftServer server, Connection connection, CommonListenerCookie cookie, CallbackInfo ci) {
        this.lunararc$player = ((ConnectionBridge) connection).lunararc$getLoginPlayer();
        this.lunararc$transferred = cookie.transferred();
    }

    @Override
    public Connection lunararc$getConnection() {
        return this.connection;
    }

    @Override
    public ServerPlayer lunararc$getPlayer() {
        return this.lunararc$player;
    }

    @Override
    public void lunararc$setPlayer(ServerPlayer player) {
        this.lunararc$player = player;
    }

    @Override
    public Set<String> lunararc$getPluginChannels() {
        return this.lunararc$pluginChannels;
    }

    @Override
    public Set<String> lunararc$getVanillaPluginChannels() {
        return this.lunararc$vanillaPluginChannels;
    }

    @Override
    public Set<String> lunararc$getLoaderPluginChannels() {
        return this.lunararc$loaderPluginChannels;
    }

    @Override
    public String lunararc$getClientBrand() {
        return this.lunararc$clientBrand;
    }

    @Override
    public void lunararc$setClientBrand(String brand) {
        this.lunararc$clientBrand = brand;
    }

    @Override
    public boolean lunararc$isTransferred() {
        return this.lunararc$transferred;
    }

    @Override
    public CompletableFuture<byte[]> lunararc$retrieveCookie(NamespacedKey key) {
        java.util.Objects.requireNonNull(key, "key");
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey());
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        CompletableFuture<byte[]> previous = this.lunararc$requestedCookies.put(id, future);
        if (previous != null) {
            previous.completeExceptionally(new IllegalStateException("Cookie request replaced for " + key));
        }
        this.connection.send(new ClientboundCookieRequestPacket(id));
        return future;
    }

    @Override
    public void lunararc$storeCookie(NamespacedKey key, byte[] value) {
        java.util.Objects.requireNonNull(key, "key");
        java.util.Objects.requireNonNull(value, "value");
        if (value.length > 5120) {
            throw new IllegalArgumentException("Cookie value too large, must be smaller than 5120 bytes");
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey());
        this.connection.send(new ClientboundStoreCookiePacket(id, value));
    }

    @Inject(method = "handleCookieResponse", at = @At("HEAD"), cancellable = true)
    private void lunararc$handleCookieResponse(ServerboundCookieResponsePacket packet, CallbackInfo ci) {
        CompletableFuture<byte[]> future = this.lunararc$requestedCookies.remove(packet.key());
        if (future == null) {
            return;
        }
        future.complete(packet.payload());
        ci.cancel();
    }

    @Inject(method = "handleResourcePackResponse", at = @At("HEAD"), require = 0)
    private void lunararc$recordResourcePackStatus(ServerboundResourcePackPacket packet, CallbackInfo ci) {
        if (this.lunararc$player == null) return;
        org.bukkit.entity.Entity bukkitEntity = ((EntityBridge) this.lunararc$player).lunararc$getBukkitEntity();
        if (bukkitEntity instanceof org.bukkit.craftbukkit.entity.CraftPlayer craftPlayer) {
            org.bukkit.event.player.PlayerResourcePackStatusEvent.Status[] values =
                    org.bukkit.event.player.PlayerResourcePackStatusEvent.Status.values();
            int ordinal = packet.action().ordinal();
            if (ordinal >= 0 && ordinal < values.length) {
                craftPlayer.lunararc$setResourcePackStatus(values[ordinal]);
            }
        }
    }

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void lunararc$observeCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        // Paper 1.21.1 has a typed BrandPayload. Record it before the generic
        // discarded-payload path so Fabric/Forge/NeoForge/Quilt do not need
        // loader-specific brand interception.
        if (packet.payload() instanceof BrandPayload brandPayload) {
            this.lunararc$setClientBrand(brandPayload.brand());
        }

        if (!(packet.payload() instanceof io.lunararcdevs.lunararc.common.network.LunarArcRawPayload payload)
                || !((Object) this instanceof ServerGamePacketListenerImpl gameListener)) {
            return;
        }

        ResourceLocation id = payload.id();
        if (!LUNARARC_REGISTER.equals(id) && !LUNARARC_UNREGISTER.equals(id) && !LUNARARC_BRAND.equals(id)
                && io.lunararcdevs.lunararc.common.network.LunarArcPluginMessageOwnership.isNativeInbound(id)) return;
        byte[] data = payload.data();
        Runnable task = () -> this.lunararc$processCustomPayload(gameListener, id, data);
        if (this.server.isSameThread()) {
            task.run();
        } else {
            this.server.execute(task);
        }
    }

    @Unique
    private void lunararc$processCustomPayload(ServerGamePacketListenerImpl gameListener, ResourceLocation id, byte[] data) {
        org.bukkit.entity.Entity bukkitEntity = ((EntityBridge) gameListener.player).lunararc$getBukkitEntity();
        if (!(bukkitEntity instanceof Player player)) {
            throw new IllegalStateException("ServerGamePacketListenerImpl player is not a Bukkit Player");
        }

        if (LUNARARC_REGISTER.equals(id) || LUNARARC_UNREGISTER.equals(id)) {
            boolean register = LUNARARC_REGISTER.equals(id);
            int start = 0;
            for (int i = 0; i <= data.length; i++) {
                if (i != data.length && data[i] != 0) {
                    continue;
                }
                if (i > start) {
                    String channel = new String(data, start, i - start, StandardCharsets.US_ASCII);
                    if (register) {
                        this.lunararc$addPluginChannel(player, channel);
                    } else {
                        this.lunararc$removePluginChannel(player, channel);
                    }
                }
                start = i + 1;
            }
            return;
        }

        if (LUNARARC_BRAND.equals(id)) {
            try {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
                this.lunararc$setClientBrand(buffer.readUtf(256));
            } catch (RuntimeException malformedBrand) {
                this.lunararc$setClientBrand("illegal");
            }
        }

        // Loader-native receivers own ids they registered. The common hook is a
        // fallback for raw/unmanaged payloads only, otherwise Fabric/Quilt/NeoForge
        // can invoke the same Bukkit listener twice for one packet.
        if (io.lunararcdevs.lunararc.common.network.LunarArcPluginMessageOwnership.isNativeInbound(id)) {
            return;
        }

        io.lunararcdevs.lunararc.common.network.LunarArcPluginMessageDispatcher
                .dispatch(this.server, player, id, data);
    }
}
