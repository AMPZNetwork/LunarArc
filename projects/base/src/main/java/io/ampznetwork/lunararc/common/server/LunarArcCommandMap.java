package io.ampznetwork.lunararc.common.server;

import org.bukkit.Server;
import org.bukkit.command.SimpleCommandMap;
import java.util.HashMap;

public class LunarArcCommandMap extends SimpleCommandMap {
    public LunarArcCommandMap(Server server) {
        super(server, new HashMap<>());
    }
}
