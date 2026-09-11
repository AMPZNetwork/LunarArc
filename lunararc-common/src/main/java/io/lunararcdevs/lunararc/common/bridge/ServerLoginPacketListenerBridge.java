package io.lunararcdevs.lunararc.common.bridge;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;

public interface ServerLoginPacketListenerBridge {
    void lunararc$preLogin(GameProfile profile) throws Exception;
    Connection lunararc$getConnection();
    void lunararc$disconnect(Component reason);
}
