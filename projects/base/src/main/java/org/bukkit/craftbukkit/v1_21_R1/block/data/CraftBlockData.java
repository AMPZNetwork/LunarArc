package org.bukkit.craftbukkit.v1_21_R1.block.data;

import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Proxy;

public class CraftBlockData {
    private final BlockState state;

    public CraftBlockData(BlockState state) {
        this.state = state;
    }

    public BlockState getState() {
        return state;
    }

    public static BlockData create(BlockState state) {
        CraftBlockData internal = new CraftBlockData(state);
        return (BlockData) Proxy.newProxyInstance(
                BlockData.class.getClassLoader(),
                new Class<?>[]{BlockData.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getMaterial":
                            net.minecraft.resources.ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
                            try {
                                return Material.valueOf(key.getPath().toUpperCase(java.util.Locale.ROOT));
                            } catch (IllegalArgumentException e) {
                                return Material.STONE;
                            }
                        case "getAsString":
                            return state.toString();
                        case "getState": // Custom method for internal use
                            return state;
                        case "clone":
                            return create(state);
                        case "matches":
                            if (args != null && args.length > 0 && args[0] instanceof BlockData other) {
                                // Simple comparison
                                return other.getMaterial() == create(state).getMaterial();
                            }
                            return false;
                    }
                    
                    Class<?> returnType = method.getReturnType();
                    if (returnType.equals(boolean.class)) return false;
                    if (returnType.equals(int.class)) return 0;
                    if (returnType.equals(String.class)) return "";
                    
                    return null;
                }
        );
    }
}
