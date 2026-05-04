package io.ampznetwork.lunararc.common.server;

import io.papermc.paper.ServerBuildInfo;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;

public record LunarArcServerBuildInfo(
    @NotNull Key brandId,
    @NotNull String brandName,
    @NotNull String minecraftVersionId,
    @NotNull String minecraftVersionName,
    @NotNull OptionalInt buildNumber,
    @NotNull Instant buildTime,
    @NotNull Optional<String> gitBranch,
    @NotNull Optional<String> gitCommit
) implements ServerBuildInfo {

    private static final java.util.Properties PROPERTIES = new java.util.Properties();
    private static final String BUILD_NAME;
    private static final String MC_VERSION;

    static {
        try (java.io.InputStream in = LunarArcServerBuildInfo.class.getClassLoader().getResourceAsStream(".lunararc/lunararc-launcher.properties")) {
            if (in != null) {
                PROPERTIES.load(in);
            }
        } catch (java.io.IOException ignored) {
        }
        BUILD_NAME = PROPERTIES.getProperty("buildName", "Trial Zenith");
        MC_VERSION = PROPERTIES.getProperty("minecraft", "1.21.1");
    }

    public LunarArcServerBuildInfo() {
        this(
            Key.key("lunararc", "lunararc"),
            "LunarArc",
            MC_VERSION,
            MC_VERSION,
            OptionalInt.of(1),
            Instant.now(),
            Optional.of("master"),
            Optional.of(BUILD_NAME)
        );
    }

    @Override
    public boolean isBrandCompatible(@NotNull Key brand) {
        return brand.value().equals("paper") || brand.value().equals("lunararc") || brand.value().equals("spigot");
    }

    @Override
    public @NotNull String asString(@NotNull StringRepresentation representation) {
        return "LunarArc-" + MC_VERSION + " (" + BUILD_NAME + ")";
    }
}
