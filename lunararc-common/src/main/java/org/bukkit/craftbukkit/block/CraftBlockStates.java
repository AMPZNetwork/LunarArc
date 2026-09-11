package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class CraftBlockStates {

    private abstract static class BlockStateFactory<B extends CraftBlockState> {
        public final Class<B> blockStateType;

        BlockStateFactory(Class<B> blockStateType) {
            this.blockStateType = blockStateType;
        }

        public abstract B createBlockState(
            World world,
            BlockPos blockPosition,
            net.minecraft.world.level.block.state.BlockState blockData,
            BlockEntity tileEntity
        );
    }

    private static final class BlockEntityStateFactory<T extends BlockEntity, B extends CraftBlockEntityState<T>>
        extends BlockStateFactory<B> {

        private final BiFunction<World, T, B> blockStateConstructor;
        private final BlockEntityType<? extends T> tileEntityConstructor;

        private BlockEntityStateFactory(
            Class<B> blockStateType,
            BiFunction<World, T, B> blockStateConstructor,
            BlockEntityType<? extends T> tileEntityConstructor
        ) {
            super(blockStateType);
            this.blockStateConstructor = blockStateConstructor;
            this.tileEntityConstructor = tileEntityConstructor;
        }

        @Override
        public B createBlockState(
            World world,
            BlockPos blockPosition,
            net.minecraft.world.level.block.state.BlockState blockData,
            BlockEntity tileEntity
        ) {
            if (world != null) {
                Preconditions.checkState(
                    tileEntity != null,
                    "Tile is null, asynchronous access? %s",
                    CraftBlock.at(((CraftWorld) world).getHandle(), blockPosition)
                );
            } else if (tileEntity == null) {
                tileEntity = createTileEntity(blockPosition, blockData);
            }
            return createBlockState(world, (T) tileEntity);
        }

        private T createTileEntity(
            BlockPos blockPosition,
            net.minecraft.world.level.block.state.BlockState blockData
        ) {
            return this.tileEntityConstructor.create(blockPosition, blockData);
        }

        private B createBlockState(World world, T tileEntity) {
            return this.blockStateConstructor.apply(world, tileEntity);
        }
    }

    private static final Map<Material, BlockStateFactory<?>> FACTORIES = new HashMap<>();
    private static final Map<BlockEntityType<?>, BlockStateFactory<?>> FACTORIES_BY_BLOCK_ENTITY_TYPE = new HashMap<>();

    /**
     * Hybrid fallback for loader/mod block entities that do not have a specialized
     * Bukkit state class. They still need a TileState-capable wrapper so placement
     * events can inspect/copy their loader-owned block entity without crashing.
     */
    private static final BlockStateFactory<?> GENERIC_BLOCK_ENTITY_FACTORY =
        new BlockStateFactory<CraftBlockEntityState>(CraftBlockEntityState.class) {
            @Override
            public CraftBlockEntityState createBlockState(
                World world,
                BlockPos blockPosition,
                net.minecraft.world.level.block.state.BlockState blockData,
                BlockEntity tileEntity
            ) {
                Preconditions.checkState(tileEntity != null, "Missing block entity for hybrid TileState at %s", blockPosition);
                return new CraftBlockEntityState(world, tileEntity);
            }
        };

    private static final BlockStateFactory<?> DEFAULT_FACTORY = new BlockStateFactory<CraftBlockState>(CraftBlockState.class) {
        @Override
        public CraftBlockState createBlockState(
            World world,
            BlockPos blockPosition,
            net.minecraft.world.level.block.state.BlockState blockData,
            BlockEntity tileEntity
        ) {
            Preconditions.checkState(
                tileEntity == null,
                "Unexpected BlockState for %s",
                CraftMagicNumbers.getMaterial(blockData.getBlock())
            );
            return new CraftBlockState(world, blockPosition, blockData);
        }
    };

    static {
        // Paper 1.21.1 block-entity state mappings. The registered factories are also
        // keyed by BlockEntityType so modded block entities can fall back correctly.
        register(BlockEntityType.SIGN, CraftSign.class, CraftSign::new);
        register(BlockEntityType.HANGING_SIGN, CraftHangingSign.class, CraftHangingSign::new);
        register(BlockEntityType.SKULL, CraftSkull.class, CraftSkull::new);
        register(BlockEntityType.COMMAND_BLOCK, CraftCommandBlock.class, CraftCommandBlock::new);
        register(BlockEntityType.BANNER, CraftBanner.class, CraftBanner::new);
        register(BlockEntityType.SHULKER_BOX, CraftShulkerBox.class, CraftShulkerBox::new);
        register(BlockEntityType.BED, CraftBed.class, CraftBed::new);
        register(BlockEntityType.BEEHIVE, CraftBeehive.class, CraftBeehive::new);
        register(BlockEntityType.CAMPFIRE, CraftCampfire.class, CraftCampfire::new);
        register(BlockEntityType.BARREL, CraftBarrel.class, CraftBarrel::new);
        register(BlockEntityType.BEACON, CraftBeacon.class, CraftBeacon::new);
        register(BlockEntityType.BELL, CraftBell.class, CraftBell::new);
        register(BlockEntityType.BLAST_FURNACE, CraftBlastFurnace.class, CraftBlastFurnace::new);
        register(BlockEntityType.BREWING_STAND, CraftBrewingStand.class, CraftBrewingStand::new);
        register(BlockEntityType.CHEST, CraftChest.class, CraftChest::new);
        register(BlockEntityType.CHISELED_BOOKSHELF, CraftChiseledBookshelf.class, CraftChiseledBookshelf::new);
        register(BlockEntityType.COMPARATOR, CraftComparator.class, CraftComparator::new);
        register(BlockEntityType.CONDUIT, CraftConduit.class, CraftConduit::new);
        register(BlockEntityType.DAYLIGHT_DETECTOR, CraftDaylightDetector.class, CraftDaylightDetector::new);
        register(BlockEntityType.DECORATED_POT, CraftDecoratedPot.class, CraftDecoratedPot::new);
        register(BlockEntityType.DISPENSER, CraftDispenser.class, CraftDispenser::new);
        register(BlockEntityType.DROPPER, CraftDropper.class, CraftDropper::new);
        register(BlockEntityType.ENCHANTING_TABLE, CraftEnchantingTable.class, CraftEnchantingTable::new);
        register(BlockEntityType.ENDER_CHEST, CraftEnderChest.class, CraftEnderChest::new);
        register(BlockEntityType.END_GATEWAY, CraftEndGateway.class, CraftEndGateway::new);
        register(BlockEntityType.END_PORTAL, CraftEndPortal.class, CraftEndPortal::new);
        register(BlockEntityType.FURNACE, CraftFurnaceFurnace.class, CraftFurnaceFurnace::new);
        register(BlockEntityType.HOPPER, CraftHopper.class, CraftHopper::new);
        register(BlockEntityType.JIGSAW, CraftJigsaw.class, CraftJigsaw::new);
        register(BlockEntityType.JUKEBOX, CraftJukebox.class, CraftJukebox::new);
        register(BlockEntityType.LECTERN, CraftLectern.class, CraftLectern::new);
        register(BlockEntityType.PISTON, CraftMovingPiston.class, CraftMovingPiston::new);
        register(BlockEntityType.SCULK_CATALYST, CraftSculkCatalyst.class, CraftSculkCatalyst::new);
        register(BlockEntityType.SCULK_SENSOR, CraftSculkSensor.class, CraftSculkSensor::new);
        register(BlockEntityType.SCULK_SHRIEKER, CraftSculkShrieker.class, CraftSculkShrieker::new);
        register(BlockEntityType.CALIBRATED_SCULK_SENSOR, CraftCalibratedSculkSensor.class, CraftCalibratedSculkSensor::new);
        register(BlockEntityType.SMOKER, CraftSmoker.class, CraftSmoker::new);
        register(BlockEntityType.MOB_SPAWNER, CraftCreatureSpawner.class, CraftCreatureSpawner::new);
        register(BlockEntityType.STRUCTURE_BLOCK, CraftStructureBlock.class, CraftStructureBlock::new);
        register(BlockEntityType.BRUSHABLE_BLOCK, CraftBrushableBlock.class, CraftBrushableBlock::new);
        register(BlockEntityType.TRAPPED_CHEST, CraftChest.class, CraftChest::new);
        register(BlockEntityType.CRAFTER, CraftCrafter.class, CraftCrafter::new);
        register(BlockEntityType.TRIAL_SPAWNER, CraftTrialSpawner.class, CraftTrialSpawner::new);
        register(BlockEntityType.VAULT, CraftVault.class, CraftVault::new);
    }

    private static void register(Material blockType, BlockStateFactory<?> factory) {
        FACTORIES.put(blockType, factory);
    }

    private static void register(BlockEntityType<?> type, BlockStateFactory<?> factory) {
        FACTORIES_BY_BLOCK_ENTITY_TYPE.put(type, factory);
    }

    private static <T extends BlockEntity, B extends CraftBlockEntityState<T>> void register(
        BlockEntityType<? extends T> blockEntityType,
        Class<B> blockStateType,
        BiFunction<World, T, B> blockStateConstructor
    ) {
        BlockStateFactory<B> factory = new BlockEntityStateFactory<>(blockStateType, blockStateConstructor, blockEntityType);

        // Do not access BlockEntityType.validBlocks directly. Paper widens that field,
        // but loader-owned Minecraft intentionally keeps it encapsulated. The vanilla
        // public isValid(BlockState) method expresses the same membership check without
        // relying on a Mixin-added interface or Paper's widened NMS ABI.
        for (net.minecraft.world.level.block.Block block : net.minecraft.core.registries.BuiltInRegistries.BLOCK) {
            if (blockEntityType.isValid(block.defaultBlockState())) {
                Material material = CraftMagicNumbers.getMaterial(block);
                if (material != null) register(material, factory);
            }
        }
        register(blockEntityType, factory);
    }

    private static BlockStateFactory<?> getFactory(Material material) {
        return FACTORIES.getOrDefault(material, DEFAULT_FACTORY);
    }

    private static BlockStateFactory<?> getFactory(Material material, BlockEntityType<?> type) {
        if (type == null) return getFactory(material);

        BlockStateFactory<?> typed = FACTORIES_BY_BLOCK_ENTITY_TYPE.get(type);
        if (typed != null) return typed;

        // A loader/mod block entity has no vanilla Craft* specialization. Returning a
        // generic CraftBlockEntityState keeps the Bukkit/Paper event path functional
        // while preserving the real loader-owned BlockEntity instance and snapshot.
        return GENERIC_BLOCK_ENTITY_FACTORY;
    }

    public static Class<? extends CraftBlockState> getBlockStateType(Material material) {
        Preconditions.checkNotNull(material, "material is null");
        return getFactory(material).blockStateType;
    }

    public static BlockEntity createNewTileEntity(Material material) {
        BlockStateFactory<?> factory = getFactory(material);
        if (factory instanceof BlockEntityStateFactory) {
            return ((BlockEntityStateFactory<?, ?>) factory).createTileEntity(
                BlockPos.ZERO,
                CraftMagicNumbers.getBlock(material).defaultBlockState()
            );
        }
        return null;
    }

    public static Class<? extends CraftBlockState> getBlockStateType(BlockEntityType<?> blockEntityType) {
        Preconditions.checkNotNull(blockEntityType, "blockEntityType is null");
        return getFactory(null, blockEntityType).blockStateType;
    }

    public static BlockState getBlockState(Block block) {
        return getBlockState(block, true);
    }

    public static BlockState getBlockState(Block block, boolean useSnapshot) {
        Preconditions.checkNotNull(block, "block is null");
        CraftBlock craftBlock = (CraftBlock) block;
        CraftWorld world = (CraftWorld) block.getWorld();
        BlockPos blockPosition = craftBlock.getPosition();
        net.minecraft.world.level.block.state.BlockState blockData = craftBlock.getHandle().getBlockState(blockPosition);
        BlockEntity tileEntity = craftBlock.getHandle().getBlockEntity(blockPosition);

        boolean previous = CraftBlockEntityState.DISABLE_SNAPSHOT;
        CraftBlockEntityState.DISABLE_SNAPSHOT = !useSnapshot;
        try {
            CraftBlockState blockState = getBlockState(world, blockPosition, blockData, tileEntity);
            blockState.setWorldHandle(craftBlock.getHandle());
            return blockState;
        } finally {
            CraftBlockEntityState.DISABLE_SNAPSHOT = previous;
        }
    }

    @Deprecated
    public static BlockState getBlockState(BlockPos blockPosition, Material material, @Nullable CompoundTag blockEntityTag) {
        return getBlockState(io.lunararcdevs.lunararc.common.LunarArcServerAccess.getMinecraftServer().registryAccess(), blockPosition, material, blockEntityTag);
    }

    public static BlockState getBlockState(LevelReader world, BlockPos blockPosition, Material material, @Nullable CompoundTag blockEntityTag) {
        return getBlockState(world.registryAccess(), blockPosition, material, blockEntityTag);
    }

    public static BlockState getBlockState(RegistryAccess registry, BlockPos blockPosition, Material material, @Nullable CompoundTag blockEntityTag) {
        Preconditions.checkNotNull(material, "material is null");
        net.minecraft.world.level.block.state.BlockState blockData = CraftMagicNumbers.getBlock(material).defaultBlockState();
        return getBlockState(registry, blockPosition, blockData, blockEntityTag);
    }

    @Deprecated
    public static BlockState getBlockState(
        net.minecraft.world.level.block.state.BlockState blockData,
        @Nullable CompoundTag blockEntityTag
    ) {
        return getBlockState(io.lunararcdevs.lunararc.common.LunarArcServerAccess.getMinecraftServer().registryAccess(), BlockPos.ZERO, blockData, blockEntityTag);
    }

    public static BlockState getBlockState(
        LevelReader world,
        BlockPos blockPosition,
        net.minecraft.world.level.block.state.BlockState blockData,
        @Nullable CompoundTag blockEntityTag
    ) {
        return getBlockState(world.registryAccess(), blockPosition, blockData, blockEntityTag);
    }

    public static BlockState getBlockState(
        RegistryAccess registry,
        BlockPos blockPosition,
        net.minecraft.world.level.block.state.BlockState blockData,
        @Nullable CompoundTag blockEntityTag
    ) {
        Preconditions.checkNotNull(blockPosition, "blockPosition is null");
        Preconditions.checkNotNull(blockData, "blockData is null");
        BlockEntity tileEntity = blockEntityTag == null
            ? null
            : BlockEntity.loadStatic(blockPosition, blockData, blockEntityTag, registry);
        return getBlockState(null, blockPosition, blockData, tileEntity);
    }

    public static CraftBlockState getBlockState(
        World world,
        BlockPos blockPosition,
        net.minecraft.world.level.block.state.BlockState blockData,
        BlockEntity tileEntity
    ) {
        Material material = CraftMagicNumbers.getMaterial(blockData.getBlock());
        BlockStateFactory<?> factory;

        if (world != null && tileEntity == null && isTileEntityOptional(material)) {
            factory = DEFAULT_FACTORY;
        } else {
            factory = getFactory(material, tileEntity != null ? tileEntity.getType() : null);
        }
        return factory.createBlockState(world, blockPosition, blockData, tileEntity);
    }

    public static boolean isTileEntityOptional(Material material) {
        return material == Material.MOVING_PISTON;
    }

    public static CraftBlockState getBlockState(LevelAccessor world, BlockPos pos) {
        if (world instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            return new CraftBlockState(CraftBlock.at(serverLevel, pos));
        }
        return CraftBlockState.unplacedAt(pos, world.getBlockState(pos));
    }

    public static CraftBlockState getBlockState(LevelAccessor world, BlockPos pos, int flag) {
        if (world instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            return new CraftBlockState(CraftBlock.at(serverLevel, pos), flag);
        }
        CraftBlockState state = CraftBlockState.unplacedAt(pos, world.getBlockState(pos));
        state.flag = flag;
        state.setWorldHandle(world);
        return state;
    }

    @Nullable
    public static BlockEntityType<?> getBlockEntityType(Material material) {
        BlockStateFactory<?> factory = FACTORIES.get(material);
        return factory instanceof BlockEntityStateFactory<?, ?> blockEntityStateFactory
            ? blockEntityStateFactory.tileEntityConstructor
            : null;
    }

    private CraftBlockStates() {
    }
}
