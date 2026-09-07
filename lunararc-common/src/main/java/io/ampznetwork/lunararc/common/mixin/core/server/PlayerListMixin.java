package io.ampznetwork.lunararc.common.mixin.core.server;

import com.mojang.authlib.GameProfile;
import io.ampznetwork.lunararc.common.LunarArcServerAccess;
import io.ampznetwork.lunararc.common.bridge.ConnectionBridge;
import io.ampznetwork.lunararc.common.bridge.EntityBridge;
import io.ampznetwork.lunararc.common.bridge.PlayerListBridge;
import io.ampznetwork.lunararc.common.bridge.ServerLoginPacketListenerBridge;
import io.ampznetwork.lunararc.common.bridge.ServerPlayerBukkitDataBridge;
import io.ampznetwork.lunararc.common.messaging.LunarArcComponentPipeline;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserBanList;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin implements PlayerListBridge {

    @Unique private net.minecraft.world.level.portal.DimensionTransition lunararc$lastRespawnTransition;
    @Unique private boolean lunararc$lastRespawnBedSpawn;

    @Shadow @Final private MinecraftServer server;
    @Shadow @Final private UserBanList bans;
    @Shadow @Final private IpBanList ipBans;
    @Shadow @Final public List<ServerPlayer> players;
    @Shadow public int maxPlayers;

    @Shadow public abstract boolean isWhiteListed(GameProfile profile);
    @Shadow public abstract boolean canBypassPlayerLimit(GameProfile profile);
    @Shadow public abstract Component canPlayerLogin(SocketAddress address, GameProfile profile);
    @Shadow public abstract void broadcastSystemMessage(Component component, boolean overlay);


    @Override
    public CraftServer lunararc$getCraftServer() {
        return LunarArcServerAccess.getCraftServer(this.server);
    }

    @Override
    public void lunararc$reloadRecipeData() {
        net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket packet =
                new net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket(
                        this.server.getRecipeManager().getOrderedRecipes());
        for (ServerPlayer player : this.players) {
            if (player.connection != null && player.connection.isAcceptingMessages()) {
                player.connection.send(packet);
            }
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lunararc$attachCraftServer(CallbackInfo ci) {
        io.ampznetwork.lunararc.common.bridge.MinecraftServerBridge bridge =
                (io.ampznetwork.lunararc.common.bridge.MinecraftServerBridge) this.server;
        if (bridge.lunararc$getCraftServer() == null) {
            bridge.lunararc$setCraftServer(new CraftServer(this.server, (PlayerList) (Object) this));
        }
    }

    @Override
    public ServerPlayer lunararc$canPlayerLogin(SocketAddress address, GameProfile profile, ServerLoginPacketListenerImpl handler) {
        Component vanillaDenial = this.canPlayerLogin(address, profile);
        ServerPlayer player = new ServerPlayer(this.server, this.server.overworld(), profile, ClientInformation.createDefault());
        Player bukkitPlayer = (Player) ((EntityBridge) player).lunararc$getBukkitEntity();

        Connection connection = ((ServerLoginPacketListenerBridge) handler).lunararc$getConnection();
        ConnectionBridge connectionBridge = (ConnectionBridge) connection;
        InetAddress forwardedAddress = address instanceof InetSocketAddress inet ? inet.getAddress() : null;
        SocketAddress rawAddress = connectionBridge.lunararc$getRawAddress();
        InetAddress realAddress = rawAddress instanceof InetSocketAddress inet ? inet.getAddress() : forwardedAddress;

        if (forwardedAddress == null || realAddress == null) {
            throw new IllegalStateException("Player login connection does not expose an InetAddress");
        }

        PlayerLoginEvent event = new PlayerLoginEvent(
                bukkitPlayer,
                connectionBridge.lunararc$getHostname(),
                forwardedAddress,
                realAddress);

        if (vanillaDenial != null) {
            event.disallow(this.lunararc$loginResult(address, profile),
                    LunarArcComponentPipeline.toAdventure(vanillaDenial));
        }

        this.lunararc$getCraftServer().getPluginManager().callEvent(event);
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            ((ServerLoginPacketListenerBridge) handler)
                    .lunararc$disconnect(LunarArcComponentPipeline.fromAdventure(event.kickMessage()));
            return null;
        }

        connectionBridge.lunararc$setLoginPlayer(player);
        return player;
    }

    @Unique
    private PlayerLoginEvent.Result lunararc$loginResult(SocketAddress address, GameProfile profile) {
        if (this.bans.isBanned(profile)) {
            return PlayerLoginEvent.Result.KICK_BANNED;
        }
        if (!this.isWhiteListed(profile)) {
            return PlayerLoginEvent.Result.KICK_WHITELIST;
        }
        if (this.ipBans.isBanned(address)) {
            return PlayerLoginEvent.Result.KICK_BANNED;
        }
        if (this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(profile)) {
            return PlayerLoginEvent.Result.KICK_FULL;
        }
        return PlayerLoginEvent.Result.KICK_OTHER;
    }

    @Inject(method = "placeNewPlayer", at = @At("HEAD"))
    private void lunararc$beginJoin(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        ((ServerPlayerBukkitDataBridge) player).lunararc$setLoginTime(System.currentTimeMillis());
    }

    /**
     * Spigot/Paper expose a final pre-join spawn-location hook before the player is
     * installed into a world. Keep the real ServerPlayer and loader-owned ServerLevel;
     * only let the event select the destination and coordinates. This mirrors the
     * 1.21.1 Arclight/CraftBukkit placement point and intentionally does not create a
     * competing world-transfer implementation.
     */
    @Redirect(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;"),
            require = 0)
    private net.minecraft.server.level.ServerLevel lunararc$playerSpawnLocationEvent(
            MinecraftServer minecraftServer,
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,
            Connection connection,
            ServerPlayer player,
            CommonListenerCookie cookie) {
        net.minecraft.server.level.ServerLevel vanillaLevel = minecraftServer.getLevel(dimension);
        if (vanillaLevel == null) {
            return null;
        }

        Object bukkit = ((EntityBridge) player).lunararc$getBukkitEntity();
        if (!(bukkit instanceof org.bukkit.craftbukkit.entity.CraftPlayer craftPlayer)) {
            return vanillaLevel;
        }

        org.spigotmc.event.player.PlayerSpawnLocationEvent event =
                new org.spigotmc.event.player.PlayerSpawnLocationEvent(craftPlayer, craftPlayer.getLocation());
        this.lunararc$getCraftServer().getPluginManager().callEvent(event);

        org.bukkit.Location selected = event.getSpawnLocation();
        if (selected == null || !(selected.getWorld() instanceof org.bukkit.craftbukkit.CraftWorld craftWorld)) {
            return vanillaLevel;
        }

        net.minecraft.server.level.ServerLevel selectedLevel = craftWorld.getHandle();
        player.setServerLevel(selectedLevel);
        player.absMoveTo(selected.getX(), selected.getY(), selected.getZ(), selected.getYaw(), selected.getPitch());
        return selectedLevel;
    }


    @Redirect(
            method = "respawn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/DimensionTransition$PostDimensionTransition;)Lnet/minecraft/world/level/portal/DimensionTransition;"),
            require = 0)
    private net.minecraft.world.level.portal.DimensionTransition lunararc$respawnEvent(
            ServerPlayer player,
            boolean keepEverything,
            net.minecraft.world.level.portal.DimensionTransition.PostDimensionTransition postTransition) {
        net.minecraft.world.level.portal.DimensionTransition vanilla =
                ((io.ampznetwork.lunararc.common.bridge.ServerPlayerRespawnBridge) (Object) player).lunararc$findRespawnPositionAndUseSpawnBlock(keepEverything, postTransition);
        if (vanilla == null) {
            return null;
        }

        Player bukkitPlayer = (Player) ((EntityBridge) player).lunararc$getBukkitEntity();
        org.bukkit.craftbukkit.CraftWorld craftWorld =
                this.lunararc$getCraftServer().getCraftWorld(vanilla.newLevel());
        org.bukkit.Location location = new org.bukkit.Location(
                craftWorld,
                vanilla.pos().x,
                vanilla.pos().y,
                vanilla.pos().z,
                vanilla.yRot(),
                vanilla.xRot());

        boolean bedSpawn = false;
        boolean anchorSpawn = false;
        net.minecraft.core.BlockPos respawnPos = player.getRespawnPosition();
        if (respawnPos != null && vanilla.newLevel() == this.server.getLevel(player.getRespawnDimension())) {
            net.minecraft.world.level.block.state.BlockState state = vanilla.newLevel().getBlockState(respawnPos);
            bedSpawn = state.getBlock() instanceof net.minecraft.world.level.block.BedBlock;
            anchorSpawn = state.is(net.minecraft.world.level.block.Blocks.RESPAWN_ANCHOR);
        }

        org.bukkit.event.player.PlayerRespawnEvent.RespawnReason reason = keepEverything
                ? org.bukkit.event.player.PlayerRespawnEvent.RespawnReason.END_PORTAL
                : org.bukkit.event.player.PlayerRespawnEvent.RespawnReason.DEATH;
        org.bukkit.event.player.PlayerRespawnEvent event =
                new org.bukkit.event.player.PlayerRespawnEvent(
                        bukkitPlayer, location, bedSpawn, anchorSpawn, reason);
        this.lunararc$getCraftServer().getPluginManager().callEvent(event);

        org.bukkit.Location chosen = event.getRespawnLocation();
        if (!(chosen.getWorld() instanceof org.bukkit.craftbukkit.CraftWorld chosenWorld)) {
            net.minecraft.core.BlockPos vanillaPos = net.minecraft.core.BlockPos.containing(vanilla.pos());
            net.minecraft.world.level.block.state.BlockState vanillaState = vanilla.newLevel().getBlockState(vanillaPos);
            this.lunararc$lastRespawnTransition = vanilla;
            this.lunararc$lastRespawnBedSpawn = vanillaState.is(net.minecraft.tags.BlockTags.BEDS)
                    && !vanilla.missingRespawnBlock();
            return vanilla;
        }
        net.minecraft.server.level.ServerLevel chosenLevel = chosenWorld.getHandle();
        net.minecraft.world.phys.Vec3 chosenPos = new net.minecraft.world.phys.Vec3(
                chosen.getX(), chosen.getY(), chosen.getZ());
        boolean unchanged = chosenLevel == vanilla.newLevel()
                && chosenPos.equals(vanilla.pos())
                && chosen.getYaw() == vanilla.yRot()
                && chosen.getPitch() == vanilla.xRot();
        net.minecraft.world.level.portal.DimensionTransition selected = unchanged
                ? vanilla
                : new net.minecraft.world.level.portal.DimensionTransition(
                        chosenLevel,
                        chosenPos,
                        vanilla.speed(),
                        chosen.getYaw(),
                        chosen.getPitch(),
                        false,
                        vanilla.postDimensionTransition());

        net.minecraft.core.BlockPos selectedPos = net.minecraft.core.BlockPos.containing(selected.pos());
        net.minecraft.world.level.block.state.BlockState selectedState = selected.newLevel().getBlockState(selectedPos);
        this.lunararc$lastRespawnTransition = selected;
        this.lunararc$lastRespawnBedSpawn = selectedState.is(net.minecraft.tags.BlockTags.BEDS)
                && !selected.missingRespawnBlock();
        return selected;
    }

    @Inject(method = "respawn", at = @At("RETURN"), require = 0)
    private void lunararc$postRespawn(ServerPlayer original, boolean keepEverything, net.minecraft.world.entity.Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir) {
        ServerPlayer respawned = cir.getReturnValue();
        net.minecraft.world.level.portal.DimensionTransition transition = this.lunararc$lastRespawnTransition;
        boolean bedSpawn = this.lunararc$lastRespawnBedSpawn;
        this.lunararc$lastRespawnTransition = null;
        this.lunararc$lastRespawnBedSpawn = false;
        if (respawned == null || transition == null || respawned.connection == null) return;

        if (original instanceof io.ampznetwork.lunararc.common.bridge.ServerPlayerDeathBridge deathBridge) {
            var inventoryState = deathBridge.lunararc$getDeathInventoryState();
            if (inventoryState != null && !keepEverything) {
                respawned.getInventory().clearContent();
                int limit = Math.min(respawned.getInventory().getContainerSize(), inventoryState.size());
                for (int slot = 0; slot < limit; slot++) {
                    net.minecraft.world.item.ItemStack stack = inventoryState.get(slot);
                    respawned.getInventory().setItem(slot, stack == null ? net.minecraft.world.item.ItemStack.EMPTY : stack.copy());
                }
                deathBridge.lunararc$setDeathInventoryState(null);
            }
            var exp = deathBridge.lunararc$getDeathExperienceState();
            if (exp != null && !keepEverything) {
                if (exp.keepLevel()) {
                    respawned.experienceLevel = exp.oldLevel();
                    respawned.totalExperience = exp.oldTotalExperience();
                    respawned.experienceProgress = exp.oldProgress();
                } else {
                    respawned.experienceLevel = Math.max(0, exp.newLevel());
                    respawned.totalExperience = Math.max(0, exp.newTotalExperience());
                    respawned.setExperiencePoints(Math.max(0, exp.newExp()));
                }
                respawned.connection.send(new net.minecraft.network.protocol.game.ClientboundSetExperiencePacket(
                        respawned.experienceProgress, respawned.totalExperience, respawned.experienceLevel));
                deathBridge.lunararc$setDeathExperienceState(null);
            }
        }

        Object bukkit = ((EntityBridge) respawned).lunararc$getBukkitEntity();
        if (!(bukkit instanceof org.bukkit.craftbukkit.entity.CraftPlayer craftPlayer)) return;

        // Paper/Spigot refresh these values after ClientboundRespawnPacket because a
        // respawn may also move the player to a world with different settings.
        respawned.connection.send(new net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket(
                craftPlayer.getSendViewDistance()));
        respawned.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket(
                craftPlayer.getSimulationDistance()));
        craftPlayer.sendHealthUpdate();

        // Resend the authoritative entity metadata after the new player instance is live.
        java.util.List<net.minecraft.network.syncher.SynchedEntityData.DataValue<?>> values =
                respawned.getEntityData().getNonDefaultValues();
        if (values != null) {
            respawned.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket(
                    respawned.getId(), values));
        }

        org.bukkit.Location postLocation = new org.bukkit.Location(
                craftPlayer.getWorld(), respawned.getX(), respawned.getY(), respawned.getZ(),
                respawned.getYRot(), respawned.getXRot());
        this.lunararc$getCraftServer().getPluginManager().callEvent(
                new com.destroystokyo.paper.event.player.PlayerPostRespawnEvent(
                        craftPlayer, postLocation, bedSpawn));
    }

    @Redirect(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void lunararc$playerJoinEvent(
            PlayerList instance,
            Component vanillaMessage,
            boolean overlay,
            Connection connection,
            ServerPlayer player,
            CommonListenerCookie cookie) {
        Player bukkitPlayer = (Player) ((EntityBridge) player).lunararc$getBukkitEntity();
        boolean addedForEvent = !this.players.contains(player);
        if (addedForEvent) {
            this.players.add(player);
        }

        PlayerJoinEvent event = new PlayerJoinEvent(bukkitPlayer, LunarArcComponentPipeline.toAdventure(vanillaMessage));
        try {
            this.lunararc$getCraftServer().getPluginManager().callEvent(event);
        } finally {
            if (addedForEvent) {
                this.players.remove(player);
            }
        }

        net.kyori.adventure.text.Component message = event.joinMessage();
        if (message != null && !message.equals(net.kyori.adventure.text.Component.empty())) {
            this.broadcastSystemMessage(LunarArcComponentPipeline.fromAdventure(message), overlay);
        }


    }

    @Inject(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V",
                    shift = At.Shift.AFTER),
            cancellable = true)
    private void lunararc$cancelJoinIfDisconnected(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        if (player.connection != null && !player.connection.isAcceptingMessages()) {
            ci.cancel();
        }
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void lunararc$playerQuitEvent(ServerPlayer player, CallbackInfo ci) {
        io.ampznetwork.lunararc.common.server.LunarArcContext.clear();

        if (player.containerMenu != player.inventoryMenu) {
            // Let the real ServerPlayer close path fire InventoryCloseEvent exactly once.
            ((io.ampznetwork.lunararc.common.bridge.ServerPlayerInventoryBridge) player)
                    .lunararc$setNextInventoryCloseReason(
                            org.bukkit.event.inventory.InventoryCloseEvent.Reason.DISCONNECT);
        }

        Player bukkitPlayer = (Player) ((EntityBridge) player).lunararc$getBukkitEntity();
        net.kyori.adventure.text.Component message = net.kyori.adventure.text.Component.translatable(
                "multiplayer.player.left",
                net.kyori.adventure.text.format.NamedTextColor.YELLOW,
                bukkitPlayer.displayName());
        PlayerQuitEvent event = new PlayerQuitEvent(
                bukkitPlayer,
                message,
                PlayerQuitEvent.QuitReason.DISCONNECTED);
        this.lunararc$getCraftServer().getPluginManager().callEvent(event);
    }
}
