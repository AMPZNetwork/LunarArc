package io.lunararcdevs.lunararc.forge.mod;

import cpw.mods.jarhandling.SecureJar;
import cpw.mods.jarhandling.impl.SimpleJarMetadata;
import net.minecraftforge.fml.loading.moddiscovery.ModFileParser;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import net.minecraftforge.forgespi.locating.ModFileFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public final class LunarArcModLocator_Forge implements IModLocator {

    private final Path modFile = Paths.get(".lunararc", "mod_file", "forge.jar");

    @Override
    public List<ModFileOrException> scanMods() {
        SecureJar secureJar = SecureJar.from(
                jar -> new SimpleJarMetadata("lunararc", "1.0.0", jar.getPackages(), List.of()),
                modFile);
        IModFile file = ModFileFactory.FACTORY.build(secureJar, this, ModFileParser::modsTomlParser);
        return List.of(new ModFileOrException(file, null));
    }

    @Override
    public String name() {
        return "lunararc";
    }

    @Override
    public void scanFile(IModFile file, Consumer<Path> pathConsumer) {
        Function<Path, SecureJar.Status> status = p -> file.getSecureJar().verifyPath(p);
        try (Stream<Path> files = Files.find(file.getSecureJar().getRootPath(), Integer.MAX_VALUE,
                (p, a) -> p.getNameCount() > 0 && p.getFileName().toString().endsWith(".class"))) {
            file.setSecurityStatus(files.peek(pathConsumer)
                    .map(status)
                    .reduce((s1, s2) -> SecureJar.Status.values()[Math.min(s1.ordinal(), s2.ordinal())])
                    .orElse(SecureJar.Status.INVALID));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initArguments(Map<String, ?> arguments) {
    }

    @Override
    public boolean isValid(IModFile modFile) {
        return true;
    }
}
