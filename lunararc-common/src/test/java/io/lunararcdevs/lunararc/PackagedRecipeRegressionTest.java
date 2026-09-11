package io.lunararcdevs.lunararc;

import com.google.gson.JsonParser;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import net.minecraft.resources.ResourceLocation;

public final class PackagedRecipeRegressionTest {
    public static void main(String[] args) throws Exception {
        String name = "io.lunararcdevs.lunararc.common.compat.MarketRecipeFilter";
        Path hybridJar = Path.of(args[0]);
        Path bridgeJar = extractCommonJarIfPresent(hybridJar);
        if (bridgeJar == null) bridgeJar = hybridJar;
        try (var loader = new URLClassLoader(new java.net.URL[]{bridgeJar.toUri().toURL()},
                PackagedRecipeRegressionTest.class.getClassLoader()) {
            @Override
            protected Class<?> loadClass(String requested, boolean resolve) throws ClassNotFoundException {
                if (!requested.equals(name)) return super.loadClass(requested, resolve);
                Class<?> found = findLoadedClass(requested);
                if (found == null) found = findClass(requested);
                if (resolve) resolveClass(found);
                return found;
            }
        }) {
            var id = ResourceLocation.parse("farmingforblockheads:market/optional");
            var recipes = new LinkedHashMap<ResourceLocation, com.google.gson.JsonElement>();
            recipes.put(id, JsonParser.parseString("{\"type\":\"farmingforblockheads:market\",\"result\":{\"item\":\"optional:missing\"}}"));
            var method = loader.loadClass(name).getMethod("filter", Map.class, Predicate.class);
            Map<?, ?> removed = (Map<?, ?>) method.invoke(null, recipes, (Predicate<ResourceLocation>) item -> false);
            Map<?, ?> retained = (Map<?, ?>) method.invoke(null, recipes, (Predicate<ResourceLocation>) item -> true);
            if (!removed.isEmpty() || retained.size() != 1 || recipes.size() != 1) throw new AssertionError("packaged recipe filtering failed");
            System.out.println("Packaged recipe Gson boundary passed");
        }
        try (var launcher = new URLClassLoader(new java.net.URL[]{hybridJar.toUri().toURL()},
                ClassLoader.getPlatformClassLoader())) {
            var translations = launcher.loadClass("io.lunararcdevs.lunararc.i18n.TranslationManager");
            Object result = translations.getMethod("get", String.class, Object[].class)
                    .invoke(null, "regression.missing.key", new Object[0]);
            if (!(result instanceof String)) throw new AssertionError("launcher translations failed");
            launcher.loadClass("io.lunararcdevs.lunararc.launcher.UpdateChecker").getDeclaredMethods();
            System.out.println("Isolated launcher Gson boundary passed");
        }
    }

    private static Path extractCommonJarIfPresent(Path hybridJar) throws Exception {
        try (JarFile jar = new JarFile(hybridJar.toFile())) {
            var entry = jar.getJarEntry("common.jar");
            if (entry == null) return null;
            Path temp = Files.createTempFile("lunararc-common-scan", ".jar");
            temp.toFile().deleteOnExit();
            try (var in = jar.getInputStream(entry)) {
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            }
            return temp;
        }
    }
}
