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
        @NotNull Optional<String> gitCommit) implements ServerBuildInfo {

    public LunarArcServerBuildInfo() {
        this(
                Key.key("lunararc", "lunararc"),
                LunarArcVersionInfo.projectName(),
                LunarArcVersionInfo.minecraftVersion(),
                LunarArcVersionInfo.minecraftVersion(),
                OptionalInt.of(1),
                Instant.now(),
                Optional.of("master"),
                Optional.of(LunarArcVersionInfo.projectVersion()));
    }

    @Override
    public boolean isBrandCompatible(@NotNull Key brand) {
        return brand.value().equals("paper") || brand.value().equals("lunararc") || brand.value().equals("spigot");
    }

    @Override
    public @NotNull String asString(@NotNull StringRepresentation representation) {
        return LunarArcVersionInfo.projectVersion() + " " + LunarArcVersionInfo.minecraftVersion();
    }
}
