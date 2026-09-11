package io.lunararcdevs.lunararc.common.server;

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


    public static final LunarArcServerBuildInfo INSTANCE = new LunarArcServerBuildInfo();

    private LunarArcServerBuildInfo() {
        this(
                Key.key("paper", "paper"),
                LunarArcVersionInfo.projectName(),
                LunarArcVersionInfo.minecraftVersion(),
                LunarArcVersionInfo.minecraftVersion(),
                LunarArcVersionInfo.paperBuild() > 0 ? OptionalInt.of(LunarArcVersionInfo.paperBuild()) : OptionalInt.empty(),
                Instant.EPOCH,
                Optional.of("main"),
                Optional.of(LunarArcVersionInfo.projectVersion()));
    }

    @Override
    public boolean isBrandCompatible(@NotNull Key brand) {
        String ns = brand.namespace();
        String val = brand.value();
        return "paper".equals(ns) || "paper".equals(val) || "lunararc".equals(val) || "spigot".equals(val);
    }

    @Override
    public @NotNull String asString(@NotNull StringRepresentation representation) {
        return LunarArcVersionInfo.projectVersion() + " " + LunarArcVersionInfo.minecraftVersion();
    }
}
