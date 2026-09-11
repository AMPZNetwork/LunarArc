package io.lunararcdevs.lunararc.neoforge.mixinplugin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class LunarArcNeoForgeCompatMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        List<String> extra = new ArrayList<>();
        if (isPresent("dev.ryanhcode.sable.util.SubLevelInclusiveLevelEntityGetter")) {
            extra.add("compat.SableEntityQueryMixin");
        }
        if (isPresent("com.github.alexthe666.alexsmobs.world.AMWorldRegistry")) {
            extra.add("compat.AlexsMobsSpawnMixin");
        }
        return extra;
    }

    private static boolean isPresent(String className) {
        try {
            Class.forName(className, false, LunarArcNeoForgeCompatMixinPlugin.class.getClassLoader());
            return true;
        } catch (Throwable notPresent) {
            return false;
        }
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
