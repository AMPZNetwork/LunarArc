package io.lunararcdevs.lunararc;

import io.papermc.paper.plugin.entrypoint.Entrypoint;
import io.papermc.paper.plugin.entrypoint.EntrypointHandler;
import io.papermc.paper.plugin.provider.PluginProvider;
import io.papermc.paper.plugin.provider.source.FileProviderSource;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public final class PluginJarLifecycleRegressionTest {
    public static void run() throws Exception {
        var path = Files.createTempFile("lunararc-plugin-lifecycle-", ".jar");
        var providers = new ArrayList<PluginProvider<?>>();
        var writer = new ClassWriter(0);
        writer.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "regression/LifecycleBootstrap", null,
                "java/lang/Object", new String[]{"io/papermc/paper/plugin/bootstrap/PluginBootstrap"});
        var constructor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
        var bootstrap = writer.visitMethod(Opcodes.ACC_PUBLIC, "bootstrap",
                "(Lio/papermc/paper/plugin/bootstrap/BootstrapContext;)V", null, null);
        bootstrap.visitCode();
        bootstrap.visitInsn(Opcodes.RETURN);
        bootstrap.visitMaxs(0, 2);
        bootstrap.visitEnd();
        writer.visitEnd();
        try {
            try (var jar = new JarOutputStream(Files.newOutputStream(path))) {
                jar.putNextEntry(new JarEntry("paper-plugin.yml"));
                jar.write("name: LifecycleRegression\nversion: '1.0'\nmain: regression.UnusedPlugin\napi-version: '1.21'\nbootstrapper: regression.LifecycleBootstrap\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                jar.closeEntry();
                jar.putNextEntry(new JarEntry("regression/LifecycleBootstrap.class"));
                jar.write(writer.toByteArray());
                jar.closeEntry();
            }
            new FileProviderSource(Object::toString).registerProviders(new EntrypointHandler() {
                @Override
                public <T> void register(Entrypoint<T> entrypoint, PluginProvider<T> provider) {
                    providers.add(provider);
                }
                @Override
                public void enter(Entrypoint<?> entrypoint) {
                }
            }, path);
            if (providers.size() != 2) throw new AssertionError("bootstrap and plugin providers missing");
            Object instance = providers.getFirst().createInstance();
            if (!instance.getClass().getName().equals("regression.LifecycleBootstrap")) throw new AssertionError("bootstrap was not loaded");
            try (var loader = (java.io.Closeable) instance.getClass().getClassLoader()) {
                if (providers.getFirst().file().getJarEntry("paper-plugin.yml") == null) throw new AssertionError("provider lost its JAR");
            }
            try {
                providers.getFirst().file().getJarEntry("paper-plugin.yml");
                throw new AssertionError("classloader shutdown did not close the JAR");
            } catch (IllegalStateException expected) {
            }
            System.out.println("Plugin JAR lifecycle regression passed");
        } finally {
            for (var provider : providers) provider.file().close();
            Files.delete(path);
        }
    }
}
