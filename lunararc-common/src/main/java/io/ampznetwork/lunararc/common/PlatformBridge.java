package io.ampznetwork.lunararc.common;

public interface PlatformBridge {
    String getPlatformName();
    void initialize();
    void onServerStarting();
    void onServerStopping();
}
