package io.papermc.paper.plugin.entrypoint.classloader;

import io.papermc.paper.plugin.configuration.PluginMeta;

public class PaperClassloaderBytecodeModifier implements ClassloaderBytecodeModifier {

    @Override
    public byte[] modify(PluginMeta configuration, byte[] bytecode) {
        return bytecode;
    }
}
