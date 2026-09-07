package io.ampznetwork.lunararc;

import com.google.gson.JsonParser;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;

public final class PackagedRecipeRegressionTest {
    public static void main(String[] args) throws Exception {
        String name = "io.ampznetwork.lunararc.common.compat.MarketRecipeFilter";
        try (var loader = new URLClassLoader(new java.net.URL[]{Path.of(args[0]).toUri().toURL()},
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
        try (var launcher = new URLClassLoader(new java.net.URL[]{Path.of(args[0]).toUri().toURL()},
                ClassLoader.getPlatformClassLoader())) {
            var translations = launcher.loadClass("io.ampznetwork.lunararc.i18n.TranslationManager");
            Object result = translations.getMethod("get", String.class, Object[].class)
                    .invoke(null, "regression.missing.key", new Object[0]);
            if (!(result instanceof String)) throw new AssertionError("launcher translations failed");
            launcher.loadClass("io.ampznetwork.lunararc.launcher.UpdateChecker").getDeclaredMethods();
            System.out.println("Isolated launcher Gson boundary passed");
        }
    }
}
