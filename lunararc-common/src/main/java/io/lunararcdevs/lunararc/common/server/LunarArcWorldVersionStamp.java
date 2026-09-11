package io.lunararcdevs.lunararc.common.server;

import io.lunararcdevs.lunararc.common.mod.server.LunarArcServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Properties;

/**
 * Records which LunarArc build last loaded this world, in the world's own save folder rather than
 * under .lunararc - so the record survives an operator deleting/resetting that runtime cache.
 */
public final class LunarArcWorldVersionStamp {
    private static final Logger LOGGER = LoggerFactory.getLogger("LunarArc");

    private LunarArcWorldVersionStamp() {}

    public static void stamp(MinecraftServer server) {
        try {
            Path worldRoot = server.getWorldPath(LevelResource.ROOT);
            Files.createDirectories(worldRoot);
            Properties props = new Properties();
            props.setProperty("version", LunarArcVersionInfo.lunarArcVersion());
            props.setProperty("minecraft", LunarArcVersionInfo.minecraftVersion());
            props.setProperty("platform", LunarArcServer.platformName());
            props.setProperty("lastBoot", Instant.now().toString());
            Path target = worldRoot.resolve("lunararc-version.properties");
            Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
            try (OutputStream out = Files.newOutputStream(tmp)) {
                props.store(out, "LunarArc version that last loaded this world - informational only, safe to delete");
            }
            try {
                Files.move(tmp, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException notSupported) {
                Files.move(tmp, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException error) {
            LOGGER.warn("[LunarArc] Could not write world-folder version stamp", error);
        }
    }
}
