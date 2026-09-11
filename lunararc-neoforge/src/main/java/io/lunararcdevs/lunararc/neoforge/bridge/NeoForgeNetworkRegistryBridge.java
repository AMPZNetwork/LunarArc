package io.lunararcdevs.lunararc.neoforge.bridge;

import java.util.Map;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.registration.PayloadRegistration;

public final class NeoForgeNetworkRegistryBridge {
    private static volatile Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration<?>>> payloadRegistrations;
    private static volatile Map<ResourceLocation, StreamCodec<FriendlyByteBuf, ? extends CustomPacketPayload>> builtinPayloads;

    private NeoForgeNetworkRegistryBridge() {}

    public static void install(
            Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration<?>>> registrations,
            Map<ResourceLocation, StreamCodec<FriendlyByteBuf, ? extends CustomPacketPayload>> builtins) {
        payloadRegistrations = java.util.Objects.requireNonNull(registrations, "registrations");
        builtinPayloads = java.util.Objects.requireNonNull(builtins, "builtins");
    }

    public static Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration<?>>> payloadRegistrations() {
        Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration<?>>> value = payloadRegistrations;
        if (value == null) throw new IllegalStateException("NeoForge payload registry bridge was not initialized");
        return value;
    }

    public static Map<ResourceLocation, StreamCodec<FriendlyByteBuf, ? extends CustomPacketPayload>> builtinPayloads() {
        Map<ResourceLocation, StreamCodec<FriendlyByteBuf, ? extends CustomPacketPayload>> value = builtinPayloads;
        if (value == null) throw new IllegalStateException("NeoForge builtin payload bridge was not initialized");
        return value;
    }
}
