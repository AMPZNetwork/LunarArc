package io.ampznetwork.lunararc.launcher;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LibraryLoader {
    private final List<URL> urls = new ArrayList<>();

    public void addJar(Path path) throws Exception {
        urls.add(path.toUri().toURL());
    }

    public ClassLoader createLoader(ClassLoader parent) {
        return new URLClassLoader(urls.toArray(new URL[0]), parent);
    }
}
