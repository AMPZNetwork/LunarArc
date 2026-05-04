package io.ampznetwork.lunararc.launcher;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class LibraryExtractor {
    public static void extractLibraries() {
        try {
            ConsoleUI.printStep("Extracting runtime dependencies...");
            File selfJar = new File(LibraryExtractor.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            
            try (ZipInputStream zin = new ZipInputStream(new FileInputStream(selfJar))) {
                ZipEntry entry;
                while ((entry = zin.getNextEntry()) != null) {
                    String name = entry.getName();
                    if (name.startsWith("META-INF/libraries/")) {
                        String destPath = name.substring("META-INF/".length());
                        Path dest = Paths.get(destPath);
                        
                        if (entry.isDirectory()) {
                            Files.createDirectories(dest);
                        } else {
                            Files.createDirectories(dest.getParent());
                            Files.copy(zin, dest, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                    zin.closeEntry();
                }
            }
            ConsoleUI.printSuccess("Runtime libraries loaded.");
        } catch (Exception e) {
            ConsoleUI.printError("Failed to extract libraries: " + e.getMessage());
        }
    }
}
