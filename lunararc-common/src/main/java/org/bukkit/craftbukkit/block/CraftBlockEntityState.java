package org.bukkit.craftbukkit.block;

import io.lunararcdevs.lunararc.common.LunarArcServerAccess;
import io.lunararcdevs.lunararc.common.bridge.BlockEntityBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.TileState;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings({"unchecked", "deprecation"})
public class CraftBlockEntityState<T extends BlockEntity> extends CraftBlockState implements TileState {
    public static boolean DISABLE_SNAPSHOT = false;

    private final T tileEntity;
    private final T snapshot;
    public boolean snapshotDisabled;
    private CraftPersistentDataContainer persistentDataContainer;

    /** CraftBukkit ABI constructor used by specialized donated block states. */
    public CraftBlockEntityState(@Nullable World world, @NotNull T tileEntity) {
        super(world, tileEntity.getBlockPos(), tileEntity.getBlockState());
        this.tileEntity = Objects.requireNonNull(tileEntity, "tileEntity");
        this.snapshotDisabled = DISABLE_SNAPSHOT;
        this.snapshot = this.snapshotDisabled ? this.tileEntity : createSnapshot(this.tileEntity);
        this.persistentDataContainer = copyPersistentData(this.snapshot);
    }

    /** Existing LunarArc constructor retained for source compatibility. */
    public CraftBlockEntityState(@NotNull ServerLevel world, @NotNull T blockEntity, boolean snapshot) {
        super(world, blockEntity.getBlockPos(), blockEntity.getBlockState());
        this.tileEntity = Objects.requireNonNull(blockEntity, "blockEntity");
        this.snapshotDisabled = !snapshot;
        this.snapshot = snapshot ? createSnapshot(blockEntity) : blockEntity;
        this.persistentDataContainer = copyPersistentData(this.snapshot);
    }

    /** CraftBukkit ABI copy constructor used by specialized state copy methods. */
    protected CraftBlockEntityState(@NotNull CraftBlockEntityState<T> state, @Nullable Location location) {
        super(state, location);
        this.snapshotDisabled = false;
        this.tileEntity = createSnapshot(state.getSnapshot());
        this.snapshot = this.tileEntity;
        this.persistentDataContainer = new CraftPersistentDataContainer(state.persistentDataContainer);
        loadData(state.getSnapshotNBT());
    }

    /**
     * Resolve the 1.21.1 registry view for placed and unplaced block states.
     * Placed states use their owning level; unplaced snapshots use LunarArc's
     * loader-owned live MinecraftServer registry access.
     */
    private net.minecraft.core.RegistryAccess registryAccess() {
        LevelAccessor access = getWorldHandle();
        return access != null ? access.registryAccess() : LunarArcServerAccess.getMinecraftServer().registryAccess();
    }

    private T createSnapshot(@Nullable T source) {
        if (source == null) return null;
        net.minecraft.core.RegistryAccess registry = registryAccess();
        CompoundTag tag = source.saveWithId(registry);
        BlockEntity loaded = BlockEntity.loadStatic(getPosition(), getHandle(), tag, registry);
        if (loaded == null) {
            throw new IllegalStateException("Could not snapshot block entity " + source.getType());
        }
        return (T) loaded;
    }

    private CraftPersistentDataContainer copyPersistentData(@Nullable T source) {
        if (source instanceof BlockEntityBridge bridge) {
            return new CraftPersistentDataContainer(bridge.lunararc$getPersistentDataContainer());
        }
        CraftPersistentDataContainer container = new CraftPersistentDataContainer();
        if (source != null) {
            net.minecraft.core.RegistryAccess registry = registryAccess();
            CompoundTag tag = source.saveWithId(registry);
            if (tag.contains("PublicBukkitValues")) container.putAll(tag.getCompound("PublicBukkitValues"));
        }
        return container;
    }

    public void refreshSnapshot() {
        load(this.tileEntity);
    }

    public T getTileEntity() {
        return this.tileEntity;
    }

    protected T getSnapshot() {
        return this.snapshot;
    }

    protected BlockEntity getTileEntityFromWorld() {
        requirePlaced();
        LevelAccessor access = Objects.requireNonNull(getWorldHandle(), "world handle");
        return access.getBlockEntity(getPosition());
    }

    protected void load(@Nullable T source) {
        if (source == null || source == this.snapshot) return;
        copyData(source, this.snapshot);
    }

    protected void applyTo(@Nullable T target) {
        if (target == null || target == this.snapshot) return;
        copyData(this.snapshot, target);
    }

    protected boolean isApplicable(@Nullable BlockEntity blockEntity) {
        return blockEntity != null && this.tileEntity.getClass() == blockEntity.getClass();
    }

    private void copyData(@NotNull T from, @NotNull T to) {
        net.minecraft.core.RegistryAccess registry = registryAccess();
        CompoundTag tag = from.saveWithId(registry);
        to.loadWithComponents(tag, registry);
    }

    public CompoundTag getSnapshotNBT() {
        applyTo(this.snapshot);
        net.minecraft.core.RegistryAccess registry = registryAccess();
        CompoundTag tag = this.snapshot.saveWithId(registry);
        if (this.persistentDataContainer.isEmpty()) tag.remove("PublicBukkitValues");
        else tag.put("PublicBukkitValues", this.persistentDataContainer.toTagCompound());
        return tag;
    }

    public CompoundTag getUpdateNBT() {
        applyTo(this.snapshot);
        net.minecraft.core.RegistryAccess registry = registryAccess();
        return this.snapshot.getUpdateTag(registry);
    }

    public CompoundTag getItemNBT() {
        // 1.21.1-compatible plugin-facing fallback: custom block-entity data without
        // changing the loader-owned live instance.
        return getSnapshotNBT();
    }

    public void loadData(@NotNull CompoundTag tag) {
        Objects.requireNonNull(tag, "tag");
        net.minecraft.core.RegistryAccess registry = registryAccess();
        this.snapshot.loadWithComponents(tag.copy(), registry);
        CraftPersistentDataContainer container = new CraftPersistentDataContainer();
        if (tag.contains("PublicBukkitValues")) container.putAll(tag.getCompound("PublicBukkitValues"));
        this.persistentDataContainer = container;
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        boolean updated = super.update(force, applyPhysics);
        if (!updated || !isPlaced()) return updated;
        BlockEntity live = getTileEntityFromWorld();
        if (!isApplicable(live)) return updated;
        applyTo((T) live);
        if (live instanceof BlockEntityBridge bridge) {
            this.persistentDataContainer.copyTo(bridge.lunararc$getPersistentDataContainer(), true);
        }
        live.setChanged();
        ServerLevel level = getServerLevelHandle();
        if (level != null) level.getChunkSource().blockChanged(getPosition());
        return true;
    }

    @Override
    public @NotNull PersistentDataContainer getPersistentDataContainer() {
        return this.persistentDataContainer;
    }

    @Override
    public boolean isSnapshot() {
        return !this.snapshotDisabled;
    }

    @Override
    public @NotNull CraftBlockEntityState<T> copy() {
        return new CraftBlockEntityState<>(this, null);
    }

    @Override
    public @NotNull CraftBlockEntityState<T> copy(@NotNull Location location) {
        return new CraftBlockEntityState<>(this, Objects.requireNonNull(location, "location"));
    }
}
