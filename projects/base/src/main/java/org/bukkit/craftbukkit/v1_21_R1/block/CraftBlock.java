package org.bukkit.craftbukkit.v1_21_R1.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

public class CraftBlock {
    private final ServerLevel world;
    private final BlockPos position;

    public CraftBlock(ServerLevel world, BlockPos position) {
        this.world = world;
        this.position = position;
    }

    public static Block create(ServerLevel world, BlockPos position) {
        return create(world, position, null);
    }

    public static Block create(ServerLevel world, BlockPos position, BlockState pendingState) {
        CraftBlock internal = new CraftBlock(world, position);
        return (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getWorld": return new CraftWorld(world);
                        case "getX": return position.getX();
                        case "getY": return position.getY();
                        case "getZ": return position.getZ();
                        case "getLocation": 
                            if (args != null && args.length > 0 && args[0] instanceof Location loc) {
                                loc.setWorld(new CraftWorld(world));
                                loc.setX(position.getX());
                                loc.setY(position.getY());
                                loc.setZ(position.getZ());
                                return loc;
                            }
                            return new Location(new CraftWorld(world), position.getX(), position.getY(), position.getZ());
                        case "getType":
                            BlockState state = pendingState != null ? pendingState : world.getBlockState(position);
                            net.minecraft.resources.ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
                            try {
                                return Material.valueOf(key.getPath().toUpperCase(java.util.Locale.ROOT));
                            } catch (IllegalArgumentException e) {
                                return Material.STONE;
                            }
                        case "setType":
                            if (args != null && args.length > 0 && args[0] instanceof Material mat) {
                                boolean applyPhysics = args.length < 2 || (boolean) args[1];
                                net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.parse(mat.name().toLowerCase(java.util.Locale.ROOT));
                                net.minecraft.world.level.block.Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(rl);
                                if (block != null) {
                                    world.setBlock(position, block.defaultBlockState(), applyPhysics ? 3 : 2);
                                }
                            }
                            return null;
                        case "getBlockData":
                            return org.bukkit.craftbukkit.v1_21_R1.block.data.CraftBlockData.create(pendingState != null ? pendingState : world.getBlockState(position));
                        case "setBlockData":
                            if (args != null && args.length > 0 && args[0] instanceof BlockData data) {
                                boolean applyPhysics = args.length < 2 || (boolean) args[1];
                                // We need a way to get the NMS state from BlockData proxy
                                // For now we'll assume it has a getState() method via reflection
                                try {
                                    BlockState nmsState = (BlockState) data.getClass().getMethod("getState").invoke(data);
                                    world.setBlock(position, nmsState, applyPhysics ? 3 : 2);
                                } catch (Exception ignored) {}
                            }
                            return null;
                        case "getState":
                            return (org.bukkit.block.BlockState) Proxy.newProxyInstance(
                                org.bukkit.block.BlockState.class.getClassLoader(),
                                new Class<?>[]{org.bukkit.block.BlockState.class},
                                (sProxy, sMethod, sArgs) -> {
                                    switch (sMethod.getName()) {
                                        case "getBlock": return proxy;
                                        case "getType": return method.invoke(proxy, args);
                                        case "getWorld": return new CraftWorld(world);
                                        case "getX": return position.getX();
                                        case "getY": return position.getY();
                                        case "getZ": return position.getZ();
                                        case "getLocation": return method.invoke(proxy, args);
                                        case "getRawData": return (byte) 0;
                                        case "getBlockData": return org.bukkit.craftbukkit.v1_21_R1.block.data.CraftBlockData.create(pendingState != null ? pendingState : world.getBlockState(position));
                                    }
                                    return null;
                                }
                            );
                        case "getDrops": return Collections.emptyList();
                        case "getMetadata": return Collections.emptyList();
                        case "hasMetadata": return false;
                        case "isSolid": return world.getBlockState(position).isSolid();
                        case "isEmpty": return world.getBlockState(position).isAir();
                        case "isLiquid": return !world.getFluidState(position).isEmpty();
                    }
                    
                    // Default return values for other methods to prevent NPEs
                    Class<?> returnType = method.getReturnType();
                    if (returnType.equals(boolean.class)) return false;
                    if (returnType.equals(int.class)) return 0;
                    if (returnType.equals(double.class)) return 0.0;
                    if (returnType.equals(float.class)) return 0.0f;
                    if (returnType.equals(List.class)) return Collections.emptyList();
                    
                    return null;
                }
        );
    }
}
