package io.papermc.paper.plugin.entrypoint;

import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import org.bukkit.plugin.java.JavaPlugin;

public final class Entrypoint<T> {

    public static final Entrypoint<PluginBootstrap> BOOTSTRAPPER = new Entrypoint<>("bootstrapper");
    public static final Entrypoint<JavaPlugin> PLUGIN = new Entrypoint<>("plugin");

    private final String debugName;

    private Entrypoint(String debugName) {
        this.debugName = debugName;
    }

    public String getDebugName() {
        return debugName;
    }
}
