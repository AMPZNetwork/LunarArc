package io.lunararcdevs.lunararc.common.mixin.core.network;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.EnumSet;

@Mixin(ClientboundPlayerInfoUpdatePacket.class)
public abstract class ClientboundPlayerInfoUpdatePacketMixin {

    @Shadow
    public abstract EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions();

    @Unique
    public EnumSet<ClientboundPlayerInfoUpdatePacket.Action> b() {
        return actions();
    }
}
