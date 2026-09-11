package io.lunararcdevs.lunararc.neoforge.mod;

import net.neoforged.neoforgespi.ILaunchContext;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFileCandidateLocator;
import net.neoforged.neoforgespi.locating.IncompatibleFileReporting;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class LunarArcModLocator_NeoForge implements IModFileCandidateLocator {

    private final Path modFile = Paths.get(".lunararc", "mod_file", "neoforge.jar");

    @Override
    public void findCandidates(ILaunchContext context, IDiscoveryPipeline pipeline) {
        pipeline.addPath(modFile, ModFileDiscoveryAttributes.DEFAULT, IncompatibleFileReporting.WARN_ALWAYS);
    }

    @Override
    public String toString() {
        return "lunararc";
    }
}
