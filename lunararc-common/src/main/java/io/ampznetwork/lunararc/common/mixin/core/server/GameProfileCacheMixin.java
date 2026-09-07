package io.ampznetwork.lunararc.common.mixin.core.server;

import io.ampznetwork.lunararc.common.server.LunarArcProfileCacheWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.Charset;
import net.minecraft.server.players.GameProfileCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameProfileCache.class)
public abstract class GameProfileCacheMixin {
    @Redirect(method = "getAsync", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;backgroundExecutor()Ljava/util/concurrent/ExecutorService;"), require = 1)
    private java.util.concurrent.ExecutorService lunararc$profileLookupExecutor() {
        return io.ampznetwork.lunararc.common.server.LunarArcProfileLookupExecutor.executor();
    }

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Lcom/google/common/io/Files;newWriter(Ljava/io/File;Ljava/nio/charset/Charset;)Ljava/io/BufferedWriter;", remap = false), require = 1)
    private BufferedWriter lunararc$deferProfileWrite(File file, Charset charset) {
        return LunarArcProfileCacheWriter.open(file, charset);
    }
}
