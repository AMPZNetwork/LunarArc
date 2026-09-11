package io.lunararcdevs.lunararc.common.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LunarArcProfileCacheWriter {
    private static final Map<Path, Snapshot> PENDING = new LinkedHashMap<>();
    private static Thread worker;

    private LunarArcProfileCacheWriter() {}

    public static BufferedWriter open(File file, Charset charset) {
        StringWriter contents = new StringWriter();
        return new BufferedWriter(contents) {
            private boolean closed;

            @Override
            public void close() throws IOException {
                if (closed) return;
                super.close();
                closed = true;
                submit(file.toPath(), contents.toString(), charset);
            }
        };
    }

    private static void submit(Path file, String contents, Charset charset) {
        synchronized (PENDING) {
            PENDING.put(file.toAbsolutePath().normalize(), new Snapshot(contents, charset));
            if (worker == null) {
                worker = new Thread(LunarArcProfileCacheWriter::drain, "LunarArc Profile Cache Writer");
                worker.setDaemon(true);
                worker.start();
            }
        }
    }

    private static void drain() {
        while (true) {
            Map.Entry<Path, Snapshot> entry;
            synchronized (PENDING) {
                var iterator = PENDING.entrySet().iterator();
                if (!iterator.hasNext()) {
                    worker = null;
                    PENDING.notifyAll();
                    return;
                }
                var next = iterator.next();
                entry = Map.entry(next.getKey(), next.getValue());
                iterator.remove();
            }
            Path temporary = null;
            try {
                Path target = Files.isSymbolicLink(entry.getKey()) ? entry.getKey().toRealPath() : entry.getKey();
                temporary = Files.createTempFile(target.getParent(), ".lunararc-profile-", ".tmp");
                Files.writeString(temporary, entry.getValue().contents(), entry.getValue().charset());
                try {
                    Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (java.nio.file.AtomicMoveNotSupportedException unsupported) {
                    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception error) {
                org.slf4j.LoggerFactory.getLogger(LunarArcProfileCacheWriter.class)
                        .error("Failed to save profile cache {}", entry.getKey(), error);
            } finally {
                if (temporary != null) {
                    try {
                        Files.deleteIfExists(temporary);
                    } catch (IOException error) {
                        org.slf4j.LoggerFactory.getLogger(LunarArcProfileCacheWriter.class)
                                .warn("Failed to remove profile cache temporary file {}", temporary, error);
                    }
                }
            }
        }
    }

    public static void flush() {
        boolean interrupted = false;
        synchronized (PENDING) {
            while (worker != null) {
                try {
                    PENDING.wait();
                } catch (InterruptedException error) {
                    interrupted = true;
                }
            }
        }
        if (interrupted) Thread.currentThread().interrupt();
    }

    private record Snapshot(String contents, Charset charset) {}
}
