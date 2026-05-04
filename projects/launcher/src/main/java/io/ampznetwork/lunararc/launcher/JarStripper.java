package io.ampznetwork.lunararc.launcher;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class JarStripper {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) return;
        for (String path : args) {
            strip(new File(path));
        }
    }

    public static void strip(File jarFile) throws Exception {
        if (!jarFile.exists()) return;
        System.out.println("[JarStripper] Stripping " + jarFile.getName());
        File tempFile = new File(jarFile.getParent(), jarFile.getName() + ".tmp");
        try (ZipInputStream zin = new ZipInputStream(new FileInputStream(jarFile));
             ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tempFile))) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.equals("META-INF/services/cpw.mods.modlauncher.api.ITransformationService") ||
                    name.equals("META-INF/services/cpw.mods.modlauncher.serviceapi.ILaunchPluginService") ||
                    name.startsWith("META-INF/services/org.spongepowered.asm.")) {
                    System.out.println("[JarStripper] Excluding " + name);
                    continue;
                }
                zout.putNextEntry(new ZipEntry(name));
                byte[] buffer = new byte[4096];
                int len;
                while ((len = zin.read(buffer)) > 0) {
                    zout.write(buffer, 0, len);
                }
                zout.closeEntry();
            }
        }
        if (!jarFile.delete()) {
            System.err.println("[JarStripper] Failed to delete original " + jarFile.getName());
            return;
        }
        if (!tempFile.renameTo(jarFile)) {
            System.err.println("[JarStripper] Failed to rename temp file to " + jarFile.getName());
        }
    }
}
