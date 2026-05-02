package io.ampznetwork.lunararc.neoforge;

import io.ampznetwork.lunararc.common.PlatformBridge;

public class NeoForgeBridge implements PlatformBridge {
    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public void initialize() {
        System.out.println("Initializing LunarArc NeoForge Bridge...");
    }

    @Override
    public void onServerStarting() {
    }

    @Override
    public void onServerStopping() {
    }
}
