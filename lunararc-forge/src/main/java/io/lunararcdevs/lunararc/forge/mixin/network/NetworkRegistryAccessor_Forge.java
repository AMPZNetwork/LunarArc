package io.lunararcdevs.lunararc.forge.mixin.network;

import io.lunararcdevs.lunararc.forge.bridge.ForgeNetworkRegistryBridge;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import net.minecraftforge.network.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetworkRegistry.class, remap = false)
public abstract class NetworkRegistryAccessor_Forge {
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void lunararc$exportLock(CallbackInfo ci) {
        try {
            VarHandle handle = MethodHandles.lookup().findStaticVarHandle(NetworkRegistry.class, "lock", boolean.class);
            ForgeNetworkRegistryBridge.install(handle);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to expose Forge 1.21.1 NetworkRegistry lock", exception);
        }
    }
}
