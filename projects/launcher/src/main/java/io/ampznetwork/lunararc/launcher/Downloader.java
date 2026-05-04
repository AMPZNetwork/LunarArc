package io.ampznetwork.lunararc.launcher;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Downloader {
    public static void download(String url, Path target) throws Exception {
        System.out.print("Downloading ");
        System.out.print(url);
        System.out.print(" to ");
        System.out.print(target);
        System.out.println("...");
        URI uri = URI.create(url);
        try (InputStream in = uri.toURL().openStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
