package io.ampznetwork.lunararc.common.bridge;

import com.mojang.authlib.GameProfile;

public interface ServerLoginPacketListenerBridge {
    void lunararc$preLogin(GameProfile profile) throws Exception;
}
