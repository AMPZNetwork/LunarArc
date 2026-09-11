package io.papermc.paper.plugin.provider.type;

import io.papermc.paper.plugin.configuration.PluginMeta;

import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public interface PluginTypeFactory<T, C extends PluginMeta> {

    T build(JarFile file, C configuration, Path source) throws Exception;

    C create(JarFile file, JarEntry config) throws Exception;
}
