package io.lunararcdevs.lunararc.forge.bridge;

import java.lang.invoke.VarHandle;

public final class ForgeNetworkRegistryBridge {
    private static volatile VarHandle lockHandle;

    private ForgeNetworkRegistryBridge() {}

    public static void install(VarHandle handle) {
        lockHandle = java.util.Objects.requireNonNull(handle, "handle");
    }

    private static VarHandle handle() {
        VarHandle value = lockHandle;
        if (value == null) throw new IllegalStateException("Forge network registry bridge was not initialized");
        return value;
    }

    public static boolean isLocked() {
        return (boolean) handle().getVolatile();
    }

    public static void setLocked(boolean locked) {
        handle().setVolatile(locked);
    }
}
