package io.lunararcdevs.lunararc.common.mixin.core.world.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.portal.PortalShape;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.event.world.PortalCreateEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bukkit FIRE portal-create event around the real PortalShape mutation.
 * The vanilla/loader-owned validation and portal algorithm remain authoritative.
 */
@Mixin(PortalShape.class)
public abstract class PortalShapeMixin {
    @Shadow @Final private LevelAccessor level;
    @Shadow @Final private Direction.Axis axis;
    @Shadow @Nullable private BlockPos bottomLeft;
    @Shadow private int height;
    @Shadow @Final private Direction rightDir;
    @Shadow @Final private int width;

    @Inject(method = "createPortalBlocks", at = @At("HEAD"), cancellable = true, require = 0)
    private void lunararc$portalCreateEvent(CallbackInfo ci) {
        if (this.bottomLeft == null || this.width <= 0 || this.height <= 0) return;
        if (!(this.level instanceof ServerLevel serverLevel)) return;
        // createPortalBlocks is reachable from worldgen (vanilla's own ruined portal
        // structures) on worker threads, same class of risk as a real confirmed crash
        // elsewhere on this exact pattern. This is a plain HEAD/cancellable hook — returning
        // early without cancelling lets the real vanilla portal creation proceed unmodified.
        if (!org.bukkit.Bukkit.isPrimaryThread()) return;

        CraftWorld world = null;
        for (org.bukkit.World candidate : Bukkit.getWorlds()) {
            if (candidate instanceof CraftWorld craft && craft.getHandle() == serverLevel) {
                world = craft;
                break;
            }
        }
        if (world == null) return;

        List<org.bukkit.block.BlockState> blocks = new ArrayList<>();
        net.minecraft.world.level.block.state.BlockState portalState = Blocks.NETHER_PORTAL.defaultBlockState()
                .setValue(NetherPortalBlock.AXIS, this.axis);

        // Interior blocks are the actual mutation this method is about to perform.
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                BlockPos pos = this.bottomLeft.relative(Direction.UP, y).relative(this.rightDir, x);
                blocks.add(new CraftBlockState(serverLevel, pos, portalState));
            }
        }

        // Include the surrounding frame snapshots, matching Bukkit's associated-block semantics.
        for (int y = -1; y <= this.height; y++) {
            for (int x = -1; x <= this.width; x++) {
                if (x >= 0 && x < this.width && y >= 0 && y < this.height) continue;
                BlockPos pos = this.bottomLeft.relative(Direction.UP, y).relative(this.rightDir, x);
                blocks.add(new CraftBlockState(serverLevel, pos, serverLevel.getBlockState(pos)));
            }
        }

        PortalCreateEvent event = new PortalCreateEvent(blocks, world, null, PortalCreateEvent.CreateReason.FIRE);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) ci.cancel();
    }
}
