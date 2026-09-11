package io.lunararcdevs.lunararc.neoforge.mixin.network;

import io.lunararcdevs.lunararc.neoforge.bridge.NeoForgeNetworkRegistryBridge;
import java.util.Map;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.network.registration.PayloadRegistration;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetworkRegistry.class, remap = false)
public abstract class NetworkRegistryAccessor_NeoForge {
    @Shadow @Final
    private static Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration<?>>> PAYLOAD_REGISTRATIONS;

    @Shadow @Final
    private static Map<ResourceLocation, StreamCodec<FriendlyByteBuf, ? extends CustomPacketPayload>> BUILTIN_PAYLOADS;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void lunararc$exportPayloadRegistries(CallbackInfo ci) {
        NeoForgeNetworkRegistryBridge.install(PAYLOAD_REGISTRATIONS, BUILTIN_PAYLOADS);
    }
}
