package io.ampznetwork.lunararc.common.stubs.org.bukkit.craftbukkit.v1_21_R1;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.entity.*;
import org.bukkit.entity.Damageable;
import org.bukkit.inventory.*;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.util.TriState;
import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CraftPlayer implements Player {
    private final ServerPlayer player;
    private final org.bukkit.permissions.PermissibleBase perm = new org.bukkit.permissions.PermissibleBase(this);

    public CraftPlayer(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getHandle() {
        return player;
    }

    @Override public @NotNull String getName() { return player.getScoreboardName(); }
    @Override public @NotNull UUID getUniqueId() { return player.getUUID(); }
    @Override public @NotNull World getWorld() { return (World) ((io.ampznetwork.lunararc.common.stubs.org.bukkit.WorldAccessor) player.serverLevel()).getBukkitWorld(); }

    @Override public boolean isPermissionSet(@NotNull String name) { return perm.isPermissionSet(name); }
    @Override public boolean isPermissionSet(@NotNull org.bukkit.permissions.Permission perm) { return this.perm.isPermissionSet(perm); }
    @Override public boolean hasPermission(@NotNull String name) { return perm.hasPermission(name); }
    @Override public boolean hasPermission(@NotNull org.bukkit.permissions.Permission perm) { return this.perm.hasPermission(perm); }

    // Minimal implementation to start with
    @Override public @NotNull String getDisplayName() { return getName(); }
    @Override public void setDisplayName(@Nullable String name) {}
    @Override public @NotNull String getPlayerListName() { return getName(); }
    @Override public void setPlayerListName(@Nullable String name) {}
    @Override public @Nullable String getPlayerListHeader() { return null; }
    @Override public @Nullable String getPlayerListFooter() { return null; }
    @Override public void setPlayerListHeader(@Nullable String header) {}
    @Override public void setPlayerListFooter(@Nullable String footer) { }
    @Override public void setPlayerListHeaderFooter(@Nullable String header, @Nullable String footer) {}
    @Override public void setPlayerListHeaderFooter(net.md_5.bungee.api.chat.BaseComponent header, net.md_5.bungee.api.chat.BaseComponent footer) {}
    @Override public void setPlayerListHeaderFooter(net.md_5.bungee.api.chat.BaseComponent[] header, net.md_5.bungee.api.chat.BaseComponent[] footer) {}
    @Override public void showWinScreen() {}
    @Override public boolean hasSeenWinScreen() { return false; }
    @Override public void setHasSeenWinScreen(boolean value) {}
    @Override public void sendActionBar(@NotNull String message) {}
    @Override public void sendActionBar(char colorChar, @NotNull String message) {}
    @Override public void sendActionBar(net.md_5.bungee.api.chat.BaseComponent... message) {}
    @Override public @NotNull Iterable<? extends net.kyori.adventure.bossbar.BossBar> activeBossBars() { return Collections.emptyList(); }
    @Override public void setCompassTarget(@NotNull Location loc) {}
    @Override public @NotNull Location getCompassTarget() { return getWorld().getSpawnLocation(); }
    @Override public @Nullable java.net.InetSocketAddress getAddress() { return null; }
    @Override public @Nullable java.net.InetSocketAddress getHAProxyAddress() { return null; }
    @Override public void sendMessage(@NotNull String message) {
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message));
    }
    @Override public void acceptConversationInput(@NotNull String input) {}
    @Override public boolean beginConversation(@NotNull org.bukkit.conversations.Conversation conversation) { return false; }
    @Override public void abandonConversation(@NotNull org.bukkit.conversations.Conversation conversation) {}
    @Override public void abandonConversation(@NotNull org.bukkit.conversations.Conversation conversation, @NotNull org.bukkit.conversations.ConversationAbandonedEvent abandonedEvent) {}
    @Override public void sendRawMessage(@NotNull String message) { sendMessage(message); }
    @Override public void sendRawMessage(@Nullable UUID sender, @NotNull String message) { sendMessage(message); }
    @Override public @Nullable net.kyori.adventure.text.Component playerListHeader() { return null; }
    @Override public @Nullable net.kyori.adventure.text.Component playerListFooter() { return null; }
    @Override public @Nullable net.kyori.adventure.text.Component playerListName() { return null; }
    @Override public void playerListName(@Nullable net.kyori.adventure.text.Component name) {}
    @Override public @Nullable net.kyori.adventure.text.Component displayName() { return null; }
    @Override public void displayName(@Nullable net.kyori.adventure.text.Component name) {}
    @Override public void sendMessage(@Nullable UUID sender, @NotNull String message) { sendMessage(message); }
    @Override public void sendMessage(@Nullable UUID sender, @NotNull String... messages) { for (String m : messages) sendMessage(m); }
    @Override public void transfer(@NotNull String host, int port) {}
    @Override public boolean isTransferred() { return false; }
    @Override public void storeCookie(@NotNull org.bukkit.NamespacedKey key, byte[] value) {}
    @Override public @NotNull java.util.concurrent.CompletableFuture<byte[]> retrieveCookie(@NotNull org.bukkit.NamespacedKey key) { return java.util.concurrent.CompletableFuture.completedFuture(new byte[0]); }
    @Override public void sendLinks(@NotNull org.bukkit.ServerLinks links) {}
    @Override public void kickPlayer(@Nullable String message) { player.connection.disconnect(net.minecraft.network.chat.Component.literal(message == null ? "Kicked" : message)); }
    @Override public void kick() {}
    @Override public void kick(@Nullable net.kyori.adventure.text.Component message) {}
    @Override public void kick(@Nullable net.kyori.adventure.text.Component message, @NotNull org.bukkit.event.player.PlayerKickEvent.Cause cause) {}
    @Override public void chat(@NotNull String msg) {}
    @Override public boolean performCommand(@NotNull String command) { return false; }
    @Override public boolean isOnGround() { return false; }
    @Override public boolean isSneaking() { return player.isShiftKeyDown(); }
    @Override public void setSneaking(boolean sneak) {}
    @Override public boolean isSprinting() { return player.isSprinting(); }
    @Override public void setSprinting(boolean sprinting) {}
    @Override public void saveData() {}
    @Override public void loadData() {}
    @Override public <E extends org.bukkit.BanEntry<? super com.destroystokyo.paper.profile.PlayerProfile>> @Nullable E ban(@Nullable String reason, @Nullable java.util.Date expires, @Nullable String source, boolean kickPlayer) { return null; }
    @Override public <E extends org.bukkit.BanEntry<? super com.destroystokyo.paper.profile.PlayerProfile>> @Nullable E ban(@Nullable String reason, @Nullable java.time.Instant expires, @Nullable String source, boolean kickPlayer) { return null; }
    @Override public <E extends org.bukkit.BanEntry<? super com.destroystokyo.paper.profile.PlayerProfile>> @Nullable E ban(@Nullable String reason, @Nullable java.time.Duration expires, @Nullable String source, boolean kickPlayer) { return null; }
    @Override public @Nullable org.bukkit.BanEntry<java.net.InetAddress> banIp(@Nullable String reason, @Nullable java.util.Date expires, @Nullable String source, boolean kickPlayer) { return null; }
    @Override public @Nullable org.bukkit.BanEntry<java.net.InetAddress> banIp(@Nullable String reason, @Nullable java.time.Instant expires, @Nullable String source, boolean kickPlayer) { return null; }
    @Override public @Nullable org.bukkit.BanEntry<java.net.InetAddress> banIp(@Nullable String reason, @Nullable java.time.Duration expires, @Nullable String source, boolean kickPlayer) { return null; }
    @Override public void setSleepingIgnored(boolean isSleeping) {}
    @Override public boolean isSleepingIgnored() { return false; }
    @Override public void sendMap(@NotNull org.bukkit.map.MapView map) {}
    @Override public void setBedSpawnLocation(@Nullable Location loc) {}
    @Override public void setBedSpawnLocation(@Nullable Location loc, boolean force) {}
    @Override public @Nullable Location getBedSpawnLocation() { return null; }
    @Override public @Nullable Location getRespawnLocation() { return null; }
    @Override public void setRespawnLocation(@Nullable Location location) {}
    @Override public void setRespawnLocation(@Nullable Location location, boolean force) {}
    @Override public void playNote(@NotNull Location loc, byte instrument, byte note) {}
    @Override public void playNote(@NotNull Location loc, @NotNull Instrument instrument, @NotNull Note note) {}
    @Override public void playSound(@NotNull Location loc, @NotNull Sound sound, float volume, float pitch) {}
    @Override public void playSound(@NotNull Location loc, @NotNull String sound, float volume, float pitch) {}
    @Override public void playSound(@NotNull Location loc, @NotNull Sound sound, @NotNull SoundCategory category, float volume, float pitch) {}
    @Override public void playSound(@NotNull Location loc, @NotNull String sound, @NotNull SoundCategory category, float volume, float pitch) {}
    @Override public void playSound(@NotNull Location location, @NotNull Sound sound, @NotNull SoundCategory category, float volume, float pitch, long seed) {}
    @Override public void playSound(@NotNull Location location, @NotNull String sound, @NotNull SoundCategory category, float volume, float pitch, long seed) {}
    @Override public void playSound(@NotNull Entity entity, @NotNull Sound sound, float volume, float pitch) {}
    @Override public void playSound(@NotNull Entity entity, @NotNull String sound, float volume, float pitch) {}
    @Override public void playSound(@NotNull Entity entity, @NotNull Sound sound, @NotNull SoundCategory category, float volume, float pitch) {}
    @Override public void playSound(@NotNull Entity entity, @NotNull String sound, @NotNull SoundCategory category, float volume, float pitch) {}
    @Override public void playSound(@NotNull Entity entity, @NotNull Sound sound, @NotNull SoundCategory category, float volume, float pitch, long seed) {}
    @Override public void playSound(@NotNull Entity entity, @NotNull String sound, @NotNull SoundCategory category, float volume, float pitch, long seed) {}
    @Override public void stopSound(@NotNull Sound sound) {}
    @Override public void stopSound(@NotNull String sound) {}
    @Override public void stopSound(@NotNull Sound sound, @Nullable SoundCategory category) {}
    @Override public void stopSound(@NotNull String sound, @Nullable SoundCategory category) {}
    @Override public void stopSound(@NotNull SoundCategory category) {}
    @Override public void stopAllSounds() {}
    @Override public void playEffect(@NotNull Location loc, @NotNull Effect effect, int data) {}
    @Override public <T> void playEffect(@NotNull Location loc, @NotNull Effect effect, @Nullable T data) {}
    @Override public boolean breakBlock(@NotNull org.bukkit.block.Block block) { return false; }
    @Override public void sendBlockChange(@NotNull Location loc, @NotNull Material material, byte data) {}
    @Override public void sendBlockChange(@NotNull Location loc, @NotNull org.bukkit.block.data.BlockData block) {}
    @Override public void sendBlockUpdate(@NotNull Location loc, @NotNull org.bukkit.block.TileState tileState) {}
    @Override public void sendBlockDamage(@NotNull Location loc, float progress) {}
    @Override public void sendBlockDamage(@NotNull Location loc, float progress, @NotNull Entity source) {}
    @Override public void sendBlockDamage(@NotNull Location loc, float progress, int entityId) {}
    @Override public void sendMultiBlockChange(@NotNull Map<? extends io.papermc.paper.math.Position, org.bukkit.block.data.BlockData> changes) {}
    @Override public void sendEquipmentChange(@NotNull LivingEntity entity, @NotNull org.bukkit.inventory.EquipmentSlot slot, @NotNull ItemStack item) {}
    @Override public void sendEquipmentChange(@NotNull LivingEntity entity, @NotNull Map<org.bukkit.inventory.EquipmentSlot, ItemStack> equipment) {}
    @Override public void sendBlockChanges(@NotNull Collection<org.bukkit.block.BlockState> states) {}
    @Override public void sendBlockChanges(@NotNull Collection<org.bukkit.block.BlockState> states, boolean updateLight) {}
    @Override public void updateInventory() {}
    @Override public void sendSignChange(@NotNull Location loc, @Nullable String[] lines) {}
    @Override public void sendSignChange(@NotNull Location loc, @Nullable String[] lines, @NotNull DyeColor dyeColor) {}
    @Override public void sendSignChange(@NotNull Location loc, @Nullable String[] lines, @NotNull DyeColor dyeColor, boolean hasGlowingText) {}
    @Override public void sendSignChange(@NotNull Location loc, @Nullable java.util.List<? extends net.kyori.adventure.text.Component> lines, @NotNull DyeColor dyeColor, boolean hasGlowingText) {}
    @Override public void incrementStatistic(@NotNull Statistic statistic) {}
    @Override public void decrementStatistic(@NotNull Statistic statistic) {}
    @Override public void incrementStatistic(@NotNull Statistic statistic, int amount) {}
    @Override public void decrementStatistic(@NotNull Statistic statistic, int amount) {}
    @Override public void setStatistic(@NotNull Statistic statistic, int newValue) {}
    @Override public int getStatistic(@NotNull Statistic statistic) { return 0; }
    @Override public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material) {}
    @Override public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material) {}
    @Override public int getStatistic(@NotNull Statistic statistic, @NotNull Material material) { return 0; }
    @Override public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int amount) {}
    @Override public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int amount) {}
    @Override public void setStatistic(@NotNull Statistic statistic, @NotNull Material material, int newValue) {}
    @Override public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) {}
    @Override public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) {}
    @Override public int getStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) { return 0; }
    @Override public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int amount) {}
    @Override public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int amount) {}
    @Override public void setStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int newValue) {}
    @Override public void setPlayerTime(long time, boolean relative) {}
    @Override public long getPlayerTime() { return player.serverLevel().getDayTime(); }
    @Override public long getPlayerTimeOffset() { return 0; }
    @Override public boolean isPlayerTimeRelative() { return true; }
    @Override public void resetPlayerTime() {}
    @Override public void setPlayerWeather(@NotNull WeatherType type) {}
    @Override public @Nullable WeatherType getPlayerWeather() { return null; }
    @Override public void resetPlayerWeather() {}
    @Override public void giveExp(int amount) {}
    @Override public void giveExpLevels(int amount) {}
    @Override public float getExp() { return 0; }
    @Override public void setExp(float exp) {}
    @Override public int getLevel() { return 0; }
    @Override public void setLevel(int level) {}
    @Override public int getTotalExperience() { return 0; }
    @Override public void setTotalExperience(int exp) {}
    @Override public void sendExperienceChange(float progress, int level) {}
    @Override public float getExhaustion() { return 0; }
    @Override public void setExhaustion(float value) {}
    @Override public float getSaturation() { return 0; }
    @Override public void setSaturation(float value) {}
    @Override public int getFoodLevel() { return 20; }
    @Override public void setFoodLevel(int value) {}
    @Override public @NotNull Location getLocation() { return new Location(getWorld(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot()); }
    @Override public @Nullable Location getLocation(@Nullable Location loc) { return getLocation(); }
    @Override public void setVelocity(@NotNull Vector velocity) {}
    @Override public @NotNull Vector getVelocity() { return new Vector(0, 0, 0); }
    @Override public boolean isOp() { return player.server.getPlayerList().isOp(player.getGameProfile()); }
    @Override public void setOp(boolean value) {}
    @Override public @NotNull List<org.bukkit.metadata.MetadataValue> getMetadata(@NotNull String metadataKey) { return Collections.emptyList(); }
    @Override public boolean hasMetadata(@NotNull String metadataKey) { return false; }
    @Override public void removeMetadata(@NotNull String metadataKey, @NotNull org.bukkit.plugin.Plugin owningPlugin) {}
    @Override public void setMetadata(@NotNull String metadataKey, @NotNull org.bukkit.metadata.MetadataValue newMetadataValue) {}
    @Override public boolean isOnline() { return true; }
    @Override public boolean isBanned() { return false; }
    @Override public boolean isWhitelisted() { return true; }
    @Override public void setWhitelisted(boolean value) {}
    @Override public @Nullable Player getPlayer() { return this; }
    @Override public long getFirstPlayed() { return 0; }
    @Override public long getLastPlayed() { return 0; }
    @Override public boolean hasPlayedBefore() { return true; }
    @Override public void setTexturePack(@NotNull String url) {}
    @Override public void setResourcePack(@NotNull String url) {}
    @Override public void setResourcePack(@NotNull String url, byte[] hash) {}
    @Override public void setResourcePack(@NotNull String url, byte[] hash, @Nullable String prompt) {}
    @Override public void setResourcePack(@NotNull String url, byte[] hash, boolean force) {}
    @Override public void setResourcePack(@NotNull String url, byte[] hash, @Nullable String prompt, boolean force) {}
    @Override public void setResourcePack(@NotNull UUID uuid, @NotNull String url, byte[] hash, @Nullable String prompt, boolean force) {}
    @Override public void setResourcePack(@NotNull UUID uuid, @NotNull String url, byte[] hash, @Nullable net.kyori.adventure.text.Component prompt, boolean force) {}
    @Override public void addResourcePack(@NotNull UUID uuid, @NotNull String url, byte[] hash, @Nullable String prompt, boolean force) {}
    @Override public void removeResourcePack(@NotNull UUID uuid) {}
    @Override public void removeResourcePacks() {}
    @Override public org.bukkit.event.player.PlayerResourcePackStatusEvent.Status getResourcePackStatus() { return org.bukkit.event.player.PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED; }
    @Override public void setResourcePack(@NotNull String url, @NotNull String hash) {}
    @Override public void setResourcePack(@NotNull String url, @NotNull String hash, boolean force) {}
    @Override public void setResourcePack(@NotNull String url, @NotNull String hash, boolean force, @Nullable net.kyori.adventure.text.Component prompt) {}
    @Override public void setResourcePack(@NotNull UUID uuid, @NotNull String url, @NotNull String hash, @Nullable net.kyori.adventure.text.Component prompt, boolean force) {}
    @Override public @NotNull Scoreboard getScoreboard() { return Bukkit.getScoreboardManager().getMainScoreboard(); }
    @Override public void setScoreboard(@NotNull Scoreboard scoreboard) {}
    @Override public boolean isFlying() { return false; }
    @Override public void setFlying(boolean value) {}
    @Override public boolean getAllowFlight() { return false; }
    @Override public void setAllowFlight(boolean value) {}
    @Override public void hidePlayer(@NotNull org.bukkit.plugin.Plugin plugin, @NotNull Player player) {}
    @Override public void hidePlayer(@NotNull Player player) {}
    @Override public void showPlayer(@NotNull org.bukkit.plugin.Plugin plugin, @NotNull Player player) {}
    @Override public void showPlayer(@NotNull Player player) {}
    @Override public void hideEntity(@NotNull org.bukkit.plugin.Plugin plugin, @NotNull Entity entity) {}
    @Override public void showEntity(@NotNull org.bukkit.plugin.Plugin plugin, @NotNull Entity entity) {}
    @Override public boolean canSee(@NotNull Player player) { return true; }
    @Override public boolean canSee(@NotNull Entity entity) { return true; }
    @Override public boolean listPlayer(@NotNull Player player) { return true; }
    @Override public boolean unlistPlayer(@NotNull Player player) { return true; }
    @Override public boolean isListed(@NotNull Player player) { return true; }
    @Override public @NotNull net.kyori.adventure.util.TriState hasFlyingFallDamage() { return net.kyori.adventure.util.TriState.NOT_SET; }
    @Override public void setFlyingFallDamage(@NotNull net.kyori.adventure.util.TriState state) {}
    @Override public void sendExperienceChange(float progress) {}
    @Override public int getExperiencePointsNeededForNextLevel() { return 0; }
    @Override public int calculateTotalExperiencePoints() { return 0; }
    @Override public void setExperienceLevelAndProgress(int level) {}
    @Override public int applyMending(int amount) { return 0; }
    @Override public void giveExp(int amount, boolean applyMending) {}
    @Override public void setExpCooldown(int ticks) {}
    @Override public int getExpCooldown() { return 0; }
    @Override public boolean isHealthScaled() { return false; }
    @Override public void setHealthScaled(boolean scale) {}
    @Override public void setHealthScale(double scale) {}
    @Override public double getHealthScale() { return 20.0; }
    @Override public void sendTitle(@Nullable String title, @Nullable String subtitle) {}
    @Override public void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut) {}
    @Override public void sendTitle(@NotNull com.destroystokyo.paper.Title title) {}
    @Override public void updateTitle(@NotNull com.destroystokyo.paper.Title title) {}
    @Override public void setTitleTimes(int fadeIn, int stay, int fadeOut) {}
    @Override public void setSubtitle(net.md_5.bungee.api.chat.BaseComponent[] subtitle) {}
    @Override public void setSubtitle(net.md_5.bungee.api.chat.BaseComponent subtitle) {}
    @Override public void showTitle(net.md_5.bungee.api.chat.BaseComponent[] title) {}
    @Override public void showTitle(net.md_5.bungee.api.chat.BaseComponent title) {}
    @Override public void showTitle(net.md_5.bungee.api.chat.BaseComponent[] title, net.md_5.bungee.api.chat.BaseComponent[] subtitle, int fadeIn, int stay, int fadeOut) {}
    @Override public void showTitle(net.md_5.bungee.api.chat.BaseComponent title, net.md_5.bungee.api.chat.BaseComponent subtitle, int fadeIn, int stay, int fadeOut) {}
    @Override public void resetTitle() {}
    @Override public void hideTitle() {}
    @Override public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count) {}
    @Override public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, @Nullable T data) {}
    @Override public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count) {}
    @Override public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, @Nullable T data) {}
    @Override public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ) {}
    @Override public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ, @Nullable T data) {}
    @Override public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {}
    @Override public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, @Nullable T data) {}
    @Override public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {}
    @Override public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data) {}
    @Override public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra) {}
    @Override public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data) {}
    @Override public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data, boolean force) {}
    @Override public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data, boolean force) {}
    @Override public org.bukkit.entity.Player.Spigot spigot() { return null; }
    @Override public @NotNull Map<String, Object> serialize() { return new HashMap<>(); }
    @Override public @NotNull PermissionAttachment addAttachment(@NotNull org.bukkit.plugin.Plugin plugin, @NotNull String name, boolean value) { return perm.addAttachment(plugin, name, value); }
    @Override public @NotNull PermissionAttachment addAttachment(@NotNull org.bukkit.plugin.Plugin plugin) { return perm.addAttachment(plugin); }
    @Override public @Nullable PermissionAttachment addAttachment(@NotNull org.bukkit.plugin.Plugin plugin, @NotNull String name, boolean value, int ticks) { return perm.addAttachment(plugin, name, value, ticks); }
    @Override public @Nullable PermissionAttachment addAttachment(@NotNull org.bukkit.plugin.Plugin plugin, int ticks) { return perm.addAttachment(plugin, ticks); }
    @Override public void removeAttachment(@NotNull PermissionAttachment attachment) { perm.removeAttachment(attachment); }
    @Override public void recalculatePermissions() { perm.recalculatePermissions(); }
    @Override public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() { return perm.getEffectivePermissions(); }

    @Override public void sendPluginMessage(@NotNull org.bukkit.plugin.Plugin source, @NotNull String channel, byte[] message) {}
    @Override public @NotNull Set<String> getListeningPluginChannels() { return Collections.emptySet(); }

    @Override public @NotNull org.bukkit.inventory.PlayerInventory getInventory() { return null; }
    @Override public @NotNull org.bukkit.inventory.Inventory getEnderChest() { return null; }
    @Override public @NotNull org.bukkit.inventory.MainHand getMainHand() { return MainHand.RIGHT; }
    @Override public boolean setWindowProperty(@NotNull org.bukkit.inventory.InventoryView.Property prop, int value) { return false; }
    @Override public @NotNull org.bukkit.inventory.InventoryView getOpenInventory() { return null; }
    @Override public @Nullable org.bukkit.inventory.InventoryView openInventory(@NotNull org.bukkit.inventory.Inventory inventory) { return null; }
    @Override public @Nullable org.bukkit.inventory.InventoryView openWorkbench(@Nullable Location location, boolean force) { return null; }
    @Override public @Nullable org.bukkit.inventory.InventoryView openEnchanting(@Nullable Location location, boolean force) { return null; }
    @Override public void openInventory(@NotNull org.bukkit.inventory.InventoryView inventory) {}
    @Override public @Nullable org.bukkit.inventory.InventoryView openMerchant(@NotNull Villager villager, boolean force) { return null; }
    @Override public @Nullable org.bukkit.inventory.InventoryView openMerchant(@NotNull Merchant merchant, boolean force) { return null; }
    @Override public @Nullable org.bukkit.inventory.InventoryView openAnvil(@Nullable Location location, boolean force) { return null; }
    @Override public @Nullable org.bukkit.inventory.InventoryView openCartographyTable(@Nullable Location location, boolean force) { return null; }
    @Override public @Nullable org.bukkit.inventory.InventoryView openGrindstone(@Nullable Location location, boolean force) { return null; }
    @Override public @Nullable org.bukkit.inventory.InventoryView openLoom(@Nullable Location location, boolean force) { return null; }
    @Override public @Nullable org.bukkit.inventory.InventoryView openSmithingTable(@Nullable Location location, boolean force) { return null; }
    @Override public @Nullable org.bukkit.inventory.InventoryView openStonecutter(@Nullable Location location, boolean force) { return null; }
    @Override public void openSign(@NotNull org.bukkit.block.Sign sign) {}
    @Override public void closeInventory() {}
    @Override public void closeInventory(@NotNull org.bukkit.event.inventory.InventoryCloseEvent.Reason reason) {}
    @Override public @NotNull org.bukkit.inventory.ItemStack getItemInHand() { return null; }
    @Override public void setItemInHand(@Nullable org.bukkit.inventory.ItemStack item) {}
    @Override public @NotNull org.bukkit.inventory.ItemStack getItemOnCursor() { return null; }
    @Override public void setItemOnCursor(@Nullable org.bukkit.inventory.ItemStack item) {}
    @Override public boolean hasCooldown(@NotNull Material material) { return false; }
    @Override public int getCooldown(@NotNull Material material) { return 0; }
    @Override public void setCooldown(@NotNull Material material, int ticks) {}
    @Override public int getSleepTicks() { return 0; }
    @Override public boolean sleep(@NotNull Location location, boolean force) { return false; }
    @Override public void wakeup(boolean setSpawnLocation) {}
    @Override public boolean isSleeping() { return false; }
    @Override public boolean isDeeplySleeping() { return false; }
    @Override public @NotNull Location getBedLocation() { return null; }
    @Override public @NotNull GameMode getGameMode() { return GameMode.SURVIVAL; }
    @Override public @Nullable GameMode getPreviousGameMode() { return null; }
    @Override public void setGameMode(@NotNull GameMode mode) {}
    @Override public boolean isBlocking() { return false; }
    @Override public int getExpToLevel() { return 0; }

    @Override public double getEyeHeight() { return 1.62; }
    @Override public double getEyeHeight(boolean ignorePose) { return 1.62; }
    @Override public @NotNull Location getEyeLocation() { return getLocation().add(0, getEyeHeight(), 0); }
    @Override public @NotNull List<Block> getLineOfSight(@Nullable Set<Material> transparent, int maxDistance) { return Collections.emptyList(); }
    @Override public @NotNull Block getTargetBlock(@Nullable Set<Material> transparent, int maxDistance) { return null; }
    @Override public @NotNull Block getTargetBlock(int maxDistance, @NotNull com.destroystokyo.paper.block.TargetBlockInfo.FluidMode fluidMode) { return null; }
    @Override public @Nullable BlockFace getTargetBlockFace(int maxDistance, @NotNull com.destroystokyo.paper.block.TargetBlockInfo.FluidMode fluidMode) { return null; }
    @Override public @Nullable BlockFace getTargetBlockFace(int maxDistance, @NotNull org.bukkit.FluidCollisionMode fluidCollisionMode) { return null; }
    @Override public @Nullable com.destroystokyo.paper.block.TargetBlockInfo getTargetBlockInfo(int maxDistance, @NotNull com.destroystokyo.paper.block.TargetBlockInfo.FluidMode fluidMode) { return null; }
    @Override public @Nullable Entity getTargetEntity(int maxDistance, boolean ignoreBlocks) { return null; }
    @Override public @Nullable com.destroystokyo.paper.entity.TargetEntityInfo getTargetEntityInfo(int maxDistance, boolean ignoreBlocks) { return null; }
    @Override public @Nullable org.bukkit.util.RayTraceResult rayTraceEntities(int maxDistance, boolean ignoreBlocks) { return null; }
    @Override public @Nullable Block getTargetBlockExact(int maxDistance) { return null; }
    @Override public @Nullable Block getTargetBlockExact(int maxDistance, @NotNull org.bukkit.FluidCollisionMode fluidCollisionMode) { return null; }
    @Override public @Nullable org.bukkit.util.RayTraceResult rayTraceBlocks(double maxDistance) { return null; }
    @Override public @Nullable org.bukkit.util.RayTraceResult rayTraceBlocks(double maxDistance, @NotNull org.bukkit.FluidCollisionMode fluidCollisionMode) { return null; }
    @Override public @NotNull List<Block> getLastTwoTargetBlocks(@Nullable Set<Material> transparent, int maxDistance) { return Collections.emptyList(); }
    @Override public int getRemainingAir() { return 300; }
    @Override public void setRemainingAir(int ticks) {}
    @Override public int getMaximumAir() { return 300; }
    @Override public void setMaximumAir(int ticks) {}
    @Override public int getMaximumNoDamageTicks() { return 20; }
    @Override public void setMaximumNoDamageTicks(int ticks) {}
    @Override public double getLastDamage() { return 0; }
    @Override public void setLastDamage(double damage) {}
    @Override public int getNoDamageTicks() { return 0; }
    @Override public void setNoDamageTicks(int ticks) {}
    @Override public int getNoActionTicks() { return 0; }
    @Override public void setNoActionTicks(int ticks) {}
    @Override public @Nullable Player getKiller() { return null; }
    @Override public void setKiller(@Nullable Player killer) {}
    @Override public boolean addPotionEffect(@NotNull org.bukkit.potion.PotionEffect effect) { return false; }
    @Override public boolean addPotionEffect(@NotNull org.bukkit.potion.PotionEffect effect, boolean force) { return false; }
    @Override public boolean addPotionEffects(@NotNull Collection<org.bukkit.potion.PotionEffect> effects) { return false; }
    @Override public boolean hasPotionEffect(@NotNull org.bukkit.potion.PotionEffectType type) { return false; }
    @Override public @Nullable org.bukkit.potion.PotionEffect getPotionEffect(@NotNull org.bukkit.potion.PotionEffectType type) { return null; }
    @Override public void removePotionEffect(@NotNull org.bukkit.potion.PotionEffectType type) {}
    @Override public @NotNull Collection<org.bukkit.potion.PotionEffect> getActivePotionEffects() { return Collections.emptyList(); }
    @Override public boolean clearActivePotionEffects() { return false; }
    @Override public boolean hasLineOfSight(@NotNull Entity other) { return false; }
    @Override public boolean hasLineOfSight(@NotNull Location location) { return false; }
    @Override public boolean getRemoveWhenFarAway() { return false; }
    @Override public void setRemoveWhenFarAway(boolean remove) {}
    @Override public @Nullable EntityEquipment getEquipment() { return null; }
    @Override public void setCanPickupItems(boolean pickup) {}
    @Override public boolean getCanPickupItems() { return true; }
    @Override public boolean isLeashed() { return false; }
    @Override public @NotNull Entity getLeashHolder() throws IllegalStateException { throw new IllegalStateException(); }
    @Override public boolean setLeashHolder(@Nullable Entity holder) { return false; }
    @Override public boolean isGliding() { return false; }
    @Override public void setGliding(boolean gliding) {}
    @Override public boolean isSwimming() { return false; }
    @Override public void setSwimming(boolean swimming) {}
    @Override public boolean isRiptiding() { return false; }
    @Override public void setRiptiding(boolean riptiding) {}
    @Override public boolean isClimbing() { return false; }
    @Override public void setAI(boolean ai) {}
    @Override public boolean hasAI() { return true; }
    @Override public void setCollidable(boolean collidable) {}
    @Override public boolean isCollidable() { return true; }
    @Override public void attack(@NotNull Entity target) {}
    @Override public void swingMainHand() {}
    @Override public void swingOffHand() {}
    @Override public void playHurtAnimation(float yaw) {}

    @Override public double getHealth() { return 20.0; }
    @Override public void setHealth(double health) {}
    @Override public double getMaxHealth() { return 20.0; }
    @Override public void setMaxHealth(double health) {}
    @Override public void resetMaxHealth() {}

    @Override public @NotNull AttributeInstance getAttribute(@NotNull Attribute attribute) { return null; }
    @Override public void registerAttribute(org.bukkit.attribute.Attribute attribute) {}

    @Override public void damage(double amount) {}
    @Override public void damage(double amount, @Nullable Entity source) {}
    @Override public void damage(double amount, @NotNull org.bukkit.damage.DamageSource source) {}
    @Override public double getAbsorptionAmount() { return 0; }
    @Override public void setAbsorptionAmount(double amount) {}
    @Override public void heal(double amount) {}
    @Override public void heal(double amount, @NotNull org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason reason) {}
    @Override public void sendHurtAnimation(float yaw) {}
    @Override public double getHeight() { return 1.8; }
    @Override public double getWidth() { return 0.6; }
    @Override public @NotNull BoundingBox getBoundingBox() { return new BoundingBox(player.getX() - 0.3, player.getY(), player.getZ() - 0.3, player.getX() + 0.3, player.getY() + 1.8, player.getZ() + 0.3); }
    @Override public boolean isDead() { return false; }
    @Override public boolean isValid() { return true; }
    @Override public void broadcastHurtAnimation(@NotNull Collection<Player> players) {}
    @Override public @NotNull String getScoreboardEntryName() { return getName(); }
    @Override public @NotNull io.papermc.paper.threadedregions.scheduler.EntityScheduler getScheduler() { return null; }
    @Override public boolean wouldCollideUsing(@NotNull BoundingBox box) { return false; }
    @Override public boolean collidesAt(@NotNull Location location) { return false; }
    @Override public float getYaw() { return 0; }
    @Override public float getPitch() { return 0; }
    @Override public double getX() { return player.getX(); }
    @Override public double getY() { return player.getY(); }
    @Override public double getZ() { return player.getZ(); }
    @Override public boolean isInPowderedSnow() { return false; }
    @Override public boolean spawnAt(@NotNull Location location, @NotNull org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) { return false; }
    @Override public @NotNull Set<Player> getTrackedPlayers() { return Collections.emptySet(); }
    @Override public boolean isTicking() { return true; }
    @Override public boolean isInWater() { return false; }
    @Override public boolean isInRain() { return false; }
    @Override public boolean isInBubbleColumn() { return false; }
    @Override public boolean isInWaterOrRain() { return false; }
    @Override public boolean isInWaterOrBubbleColumn() { return false; }
    @Override public boolean isInWaterOrRainOrBubbleColumn() { return false; }
    @Override public boolean isInLava() { return false; }
    @Override public boolean isInWorld() { return true; }
    @Override public boolean isUnderWater() { return false; }
    @Override public @NotNull org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason getEntitySpawnReason() { return org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.DEFAULT; }
    @Override public boolean fromMobSpawner() { return false; }
    @Override public @Nullable Location getOrigin() { return null; }
    @Override public @NotNull net.kyori.adventure.text.Component teamDisplayName() { return net.kyori.adventure.text.Component.text(getName()); }
    @Override public @NotNull Entity copy() { throw new UnsupportedOperationException(); }
    @Override public @NotNull Entity copy(@NotNull Location location) { throw new UnsupportedOperationException(); }
    @Override public @NotNull org.bukkit.entity.EntitySnapshot createSnapshot() { throw new UnsupportedOperationException(); }
    @Override public @NotNull String getAsString() { return getName(); }
    @Override public @NotNull org.bukkit.entity.SpawnCategory getSpawnCategory() { return org.bukkit.entity.SpawnCategory.MISC; }
    @Override public boolean hasFixedPose() { return false; }
    @Override public void setPose(@NotNull Pose pose, boolean fixed) {}
    @Override public @NotNull Set<Player> getTrackedBy() { return Collections.emptySet(); }
    @Override public boolean isVisibleByDefault() { return true; }
    @Override public void setVisibleByDefault(boolean visible) {}
    @Override public @NotNull org.bukkit.Sound getSwimSound() { return org.bukkit.Sound.ENTITY_PLAYER_SWIM; }
    @Override public @NotNull org.bukkit.Sound getSwimSplashSound() { return org.bukkit.Sound.ENTITY_PLAYER_SPLASH; }
    @Override public @NotNull org.bukkit.Sound getSwimHighSpeedSplashSound() { return org.bukkit.Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED; }
    @Override public @Nullable org.bukkit.event.entity.EntityDamageEvent getLastDamageCause() { return null; }
    @Override public void setLastDamageCause(@Nullable org.bukkit.event.entity.EntityDamageEvent cause) {}
    @Override public float getFallDistance() { return 0; }
    @Override public void setFallDistance(float distance) {}
    @Override public int getEntityId() { return player.getId(); }
    @Override public boolean isPersistent() { return true; }
    @Override public void setPersistent(boolean persistent) {}
    @Override public int getFreezeTicks() { return 0; }
    @Override public void setFreezeTicks(int ticks) {}
    @Override public int getMaxFreezeTicks() { return 140; }
    @Override public boolean isFrozen() { return false; }
    @Override public boolean isFreezeTickingLocked() { return false; }
    @Override public void lockFreezeTicks(boolean locked) {}
    @Override public int getFireTicks() { return 0; }
    @Override public void setFireTicks(int ticks) {}
    @Override public int getMaxFireTicks() { return 1; }
    @Override public void setVisualFire(boolean fire) {}
    @Override public boolean isVisualFire() { return false; }
    @Override public void setNoPhysics(boolean noPhysics) {}
    @Override public boolean hasNoPhysics() { return false; }
    @Override public boolean isEmpty() { return true; }
    @Override public boolean eject() { return false; }
    @Override public @Nullable Entity getPassenger() { return null; }
    @Override public boolean setPassenger(@NotNull Entity passenger) { return false; }
    @Override public @NotNull List<Entity> getPassengers() { return Collections.emptyList(); }
    @Override public boolean addPassenger(@NotNull Entity passenger) { return false; }
    @Override public boolean removePassenger(@NotNull Entity passenger) { return false; }
    @Override public void sendMessage(@NotNull String... messages) {}
    @Override public @NotNull net.kyori.adventure.text.Component name() { return net.kyori.adventure.text.Component.text(getName()); }
    @Override public @NotNull org.bukkit.persistence.PersistentDataContainer getPersistentDataContainer() { throw new UnsupportedOperationException(); }
    @Override public @Nullable String getCustomName() { return null; }
    @Override public void setCustomName(@Nullable String name) {}
    @Override public @Nullable net.kyori.adventure.text.Component customName() { return null; }
    @Override public void customName(@Nullable net.kyori.adventure.text.Component name) {}
    @Override public @NotNull TriState getFrictionState() { return TriState.NOT_SET; }
    @Override public void setFrictionState(@NotNull TriState state) {}
    @Override public boolean isConversing() { return false; }
    @Override public long getLastSeen() { return 0; }
    @Override public long getLastLogin() { return 0; }
    @Override public boolean isConnected() { return true; }
    @Override public @Nullable InetSocketAddress getVirtualHost() { return null; }
    @Override public int getProtocolVersion() { return 0; }
    @Override public <E extends org.bukkit.BanEntry<? super com.destroystokyo.paper.profile.PlayerProfile>> E ban(@Nullable String reason, @Nullable Date expires, @Nullable String source) { return null; }
    @Override public <E extends org.bukkit.BanEntry<? super com.destroystokyo.paper.profile.PlayerProfile>> E ban(@Nullable String reason, @Nullable Instant expires, @Nullable String source) { return null; }
    @Override public <E extends org.bukkit.BanEntry<? super com.destroystokyo.paper.profile.PlayerProfile>> E ban(@Nullable String reason, @Nullable Duration duration, @Nullable String source) { return null; }
    @Override public <T extends Projectile> T launchProjectile(@NotNull Class<? extends T> projectile) { return null; }
    @Override public <T extends Projectile> T launchProjectile(@NotNull Class<? extends T> projectile, @Nullable Vector velocity) { return null; }
    @Override public <T extends Projectile> T launchProjectile(@NotNull Class<? extends T> projectile, @Nullable Vector velocity, @Nullable Consumer<? super T> function) { return null; }
    @Override public boolean teleport(@NotNull Location location) { return false; }
    @Override public boolean teleport(@NotNull Location location, @NotNull TeleportCause cause) { return false; }
    @Override public boolean teleport(@NotNull Entity destination) { return false; }
    @Override public boolean teleport(@NotNull Entity destination, @NotNull TeleportCause cause) { return false; }
    @Override public boolean teleport(@NotNull Location location, @NotNull TeleportCause cause, @NotNull TeleportFlag... flags) { return false; }
    @Override public @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Location location, @NotNull TeleportCause cause, @NotNull TeleportFlag... flags) { return CompletableFuture.completedFuture(false); }
    @Override public @NotNull List<Entity> getNearbyEntities(double x, double y, double z) { return Collections.emptyList(); }
    @Override public @NotNull Server getServer() { return Bukkit.getServer(); }
    @Override public void remove() {}
    @Override public boolean isInsideVehicle() { return false; }
    @Override public boolean leaveVehicle() { return false; }
    @Override public @Nullable Entity getVehicle() { return null; }
    @Override public void setCustomNameVisible(boolean visible) {}
    @Override public boolean isCustomNameVisible() { return false; }
    @Override public void setGlowing(boolean flag) {}
    @Override public boolean isGlowing() { return false; }
    @Override public void setInvisible(boolean invisible) {}
    @Override public boolean isInvisible() { return false; }
    @Override public void setInvulnerable(boolean flag) {}
    @Override public boolean isInvulnerable() { return false; }
    @Override public boolean isSilent() { return false; }
    @Override public void setSilent(boolean flag) {}
    @Override public boolean hasGravity() { return true; }
    @Override public void setGravity(boolean gravity) {}
    @Override public int getTicksLived() { return 0; }
    @Override public void setTicksLived(int value) {}
    @Override public @NotNull Set<UUID> getCollidableExemptions() { return Collections.emptySet(); }
    @Override public void playEffect(@NotNull EntityEffect type) {}
    @Override public void sendEntityEffect(@NotNull EntityEffect effect, @NotNull Entity entity) {}
    @Override public void sendPotionEffectChange(@NotNull LivingEntity entity, @NotNull org.bukkit.potion.PotionEffect effect) {}
    @Override public void sendPotionEffectChangeRemove(@NotNull LivingEntity entity, @NotNull org.bukkit.potion.PotionEffectType type) {}
    @Override public @NotNull Set<Long> getSentChunkKeys() { return Collections.emptySet(); }
    @Override public @NotNull Set<Chunk> getSentChunks() { return Collections.emptySet(); }
    @Override public boolean isChunkSent(long key) { return true; }
    @Override public void resetIdleDuration() {}
    @Override public @NotNull java.time.Duration getIdleDuration() { return java.time.Duration.ZERO; }
    @Override public void increaseWardenWarningLevel() {}
    @Override public void setWardenWarningLevel(int level) {}
    @Override public int getWardenWarningLevel() { return 0; }
    @Override public void setWardenTimeSinceLastWarning(int ticks) {}
    @Override public int getWardenTimeSinceLastWarning() { return 0; }
    @Override public void setWardenWarningCooldown(int ticks) {}
    @Override public int getWardenWarningCooldown() { return 0; }
    @Override public void showElderGuardian(boolean silent) {}
    @Override public void lookAt(double x, double y, double z, @NotNull io.papermc.paper.entity.LookAnchor anchor) {}
    @Override public void lookAt(@NotNull Entity entity, @NotNull io.papermc.paper.entity.LookAnchor anchor1, @NotNull io.papermc.paper.entity.LookAnchor anchor2) {}
    @Override public void setRotation(float yaw, float pitch) {}
    @Override public @Nullable String getClientBrandName() { return null; }
    @Override public void removeAdditionalChatCompletions(@NotNull Collection<String> completions) {}
    @Override public void addAdditionalChatCompletions(@NotNull Collection<String> completions) {}
    @Override public void setCustomChatCompletions(@NotNull Collection<String> completions) {}
    @Override public void addCustomChatCompletions(@NotNull Collection<String> completions) {}
    @Override public void removeCustomChatCompletions(@NotNull Collection<String> completions) {}
    @Override public void sendOpLevel(byte level) {}
    @Override public float getAttackCooldown() { return 0; }
    @Override public boolean isHandRaised() { return false; }
    @Override public float getCooldownPeriod() { return 0; }
    @Override public float getCooledAttackStrength(float adjustTicks) { return 0; }
    @Override public void resetCooldown() {}
    @Override public <T> T getClientOption(@NotNull com.destroystokyo.paper.ClientOption<T> option) { return null; }
    @Override public @NotNull com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile() { return null; }
    @Override public void setPlayerProfile(@NotNull com.destroystokyo.paper.profile.PlayerProfile profile) {}
    @Override public boolean isAllowingServerListings() { return true; }
    @Override public void showDemoScreen() {}
    @Override public void openBook(@NotNull ItemStack book) {}
    @Override public void openSign(@NotNull org.bukkit.block.Sign sign, @NotNull org.bukkit.block.sign.Side side) {}
    @Override public void updateCommands() {}
    @Override public int getSendViewDistance() { return 10; }
    @Override public void setSendViewDistance(int distance) {}
    @Override public int getSimulationDistance() { return 10; }
    @Override public void setSimulationDistance(int distance) {}
    @Override public int getViewDistance() { return 10; }
    @Override public void setViewDistance(int distance) {}
    @Override public boolean getAffectsSpawning() { return true; }
    @Override public void setAffectsSpawning(boolean affects) {}
    @Override public int getPing() { return 0; }
    @Override public @NotNull java.util.Locale locale() { return java.util.Locale.US; }
    @Override public @NotNull String getLocale() { return "en_US"; }
    @Override public int getClientViewDistance() { return 10; }
    @Override public @NotNull org.bukkit.advancement.AdvancementProgress getAdvancementProgress(@NotNull org.bukkit.advancement.Advancement advancement) { return null; }
    @Override public @Nullable WorldBorder getWorldBorder() { return null; }
    @Override public void setWorldBorder(@Nullable WorldBorder border) {}
    @Override public @Nullable Entity getSpectatorTarget() { return null; }
    @Override public void setSpectatorTarget(@Nullable Entity entity) {}
    @Override public void sendHealthUpdate(double health, int foodLevel, float saturation) {}
    @Override public void sendHealthUpdate() {}
    @Override public float getFlySpeed() { return 0.1f; }
    @Override public void setFlySpeed(float speed) {}
    @Override public float getWalkSpeed() { return 0.2f; }
    @Override public void setWalkSpeed(float speed) {}
    @Override public @NotNull EntityType getType() { return EntityType.PLAYER; }
    @Override public @NotNull org.bukkit.entity.EntityCategory getCategory() { return org.bukkit.entity.EntityCategory.NONE; }
    @Override public boolean canBreatheUnderwater() { return false; }
    @Override public <T> @Nullable T getMemory(@NotNull org.bukkit.entity.memory.MemoryKey<T> key) { return null; }
    @Override public <T> void setMemory(@NotNull org.bukkit.entity.memory.MemoryKey<T> key, @Nullable T value) {}
    @Override public void setPortalCooldown(int cooldown) {}
    @Override public int getPortalCooldown() { return 0; }
    @Override public @NotNull Set<String> getScoreboardTags() { return Collections.emptySet(); }
    @Override public boolean addScoreboardTag(@NotNull String tag) { return false; }
    @Override public boolean removeScoreboardTag(@NotNull String tag) { return false; }
    @Override public @NotNull PistonMoveReaction getPistonMoveReaction() { return PistonMoveReaction.MOVE; }
    @Override public @NotNull BlockFace getFacing() { return BlockFace.NORTH; }
    @Override public boolean dropItem(boolean dropAll) { return false; }
    @Override public @NotNull Pose getPose() { return Pose.STANDING; }
    @Override public @Nullable org.bukkit.entity.Firework fireworkBoost(@NotNull org.bukkit.inventory.ItemStack itemStack) { return null; }
    @Override public boolean discoverRecipe(@NotNull NamespacedKey recipe) { return false; }
    @Override public int discoverRecipes(@NotNull Collection<NamespacedKey> recipes) { return 0; }
    @Override public boolean undiscoverRecipe(@NotNull NamespacedKey recipe) { return false; }
    @Override public int undiscoverRecipes(@NotNull Collection<NamespacedKey> recipes) { return 0; }
    @Override public boolean hasDiscoveredRecipe(@NotNull NamespacedKey recipe) { return false; }
    @Override public @NotNull Set<NamespacedKey> getDiscoveredRecipes() { return Collections.emptySet(); }
    @Override public @Nullable Entity getShoulderEntityLeft() { return null; }
    @Override public void setShoulderEntityLeft(@Nullable Entity entity) {}
    @Override public @Nullable Entity getShoulderEntityRight() { return null; }
    @Override public void setShoulderEntityRight(@Nullable Entity entity) {}
    @Override public @Nullable Entity releaseLeftShoulderEntity() { return null; }
    @Override public @Nullable Entity releaseRightShoulderEntity() { return null; }
    @Override public boolean canUseEquipmentSlot(@NotNull org.bukkit.inventory.EquipmentSlot slot) { return true; }
    @Override public void setHurtDirection(float direction) {}
    @Override public float getHurtDirection() { return 0; }
    @Override public void setBodyYaw(float yaw) {}
    @Override public float getBodyYaw() { return 0; }
    @Override public void setJumping(boolean jumping) {}
    @Override public boolean isJumping() { return false; }
    @Override public void playPickupItemAnimation(@NotNull org.bukkit.entity.Item item, int quantity) {}
    @Override public void knockback(double strength, double directionX, double directionZ) {}
    @Override public void broadcastSlotBreak(@NotNull org.bukkit.inventory.EquipmentSlot slot) {}
    @Override public void broadcastSlotBreak(@NotNull org.bukkit.inventory.EquipmentSlot slot, @NotNull Collection<org.bukkit.entity.Player> players) {}
    @Override public @NotNull org.bukkit.inventory.ItemStack damageItemStack(@NotNull org.bukkit.inventory.ItemStack item, int amount) { return item; }
    @Override public void damageItemStack(@NotNull org.bukkit.inventory.EquipmentSlot slot, int amount) {}
    @Override public @Nullable org.bukkit.inventory.ItemStack getItemInUse() { return null; }
    @Override public int getItemInUseTicks() { return 0; }
    @Override public void setItemInUseTicks(int ticks) {}
    @Override public @NotNull org.bukkit.inventory.ItemStack getActiveItem() { return null; }
    @Override public void clearActiveItem() {}
    @Override public int getActiveItemRemainingTime() { return 0; }
    @Override public void setActiveItemRemainingTime(int ticks) {}
    @Override public boolean hasActiveItem() { return false; }
    @Override public int getActiveItemUsedTime() { return 0; }
    @Override public @NotNull org.bukkit.inventory.EquipmentSlot getActiveItemHand() { return org.bukkit.inventory.EquipmentSlot.HAND; }
    @Override public void completeUsingActiveItem() {}
    @Override public void startUsingItem(@NotNull org.bukkit.inventory.EquipmentSlot slot) {}
    @Override public float getSidewaysMovement() { return 0; }
    @Override public float getUpwardsMovement() { return 0; }
    @Override public float getForwardsMovement() { return 0; }
    @Override public @Nullable org.bukkit.Sound getHurtSound() { return null; }
    @Override public @Nullable org.bukkit.Sound getDeathSound() { return null; }
    @Override public @NotNull org.bukkit.Sound getFallDamageSound(int distance) { return org.bukkit.Sound.ENTITY_PLAYER_SMALL_FALL; }
    @Override public @NotNull org.bukkit.Sound getFallDamageSoundSmall() { return org.bukkit.Sound.ENTITY_PLAYER_SMALL_FALL; }
    @Override public @NotNull org.bukkit.Sound getFallDamageSoundBig() { return org.bukkit.Sound.ENTITY_PLAYER_BIG_FALL; }
    @Override public @NotNull org.bukkit.Sound getDrinkingSound(@NotNull org.bukkit.inventory.ItemStack itemStack) { return org.bukkit.Sound.ENTITY_GENERIC_DRINK; }
    @Override public @NotNull org.bukkit.Sound getEatingSound(@NotNull org.bukkit.inventory.ItemStack itemStack) { return org.bukkit.Sound.ENTITY_GENERIC_EAT; }
    @Override public int getShieldBlockingDelay() { return 0; }
    @Override public void setShieldBlockingDelay(int ticks) {}
    @Override public int getArrowCooldown() { return 0; }
    @Override public void setArrowCooldown(int ticks) {}
    @Override public int getArrowsStuck() { return 0; }
    @Override public void setArrowsStuck(int arrows) {}
    @Override public int getArrowsInBody() { return 0; }
    @Override public void setArrowsInBody(int count, boolean remove) {}
    @Override public int getNextArrowRemoval() { return 0; }
    @Override public void setNextArrowRemoval(int ticks) {}
    @Override public int getBeeStingerCooldown() { return 0; }
    @Override public void setBeeStingerCooldown(int ticks) {}
    @Override public int getBeeStingersInBody() { return 0; }
    @Override public void setBeeStingersInBody(int count) {}
    @Override public int getNextBeeStingerRemoval() { return 0; }
    @Override public void setNextBeeStingerRemoval(int ticks) {}
    @Override public int getEnchantmentSeed() { return 0; }
    @Override public void setEnchantmentSeed(int seed) {}
    @Override public @Nullable Location getPotentialBedLocation() { return null; }
    @Override public @Nullable org.bukkit.entity.FishHook getFishHook() { return null; }
    @Override public void startRiptideAttack(int duration, float attackDamage, @NotNull org.bukkit.inventory.ItemStack itemStack) {}
    @Override public int getSaturatedRegenRate() { return 10; }
    @Override public void setSaturatedRegenRate(int rate) {}
    @Override public int getUnsaturatedRegenRate() { return 80; }
    @Override public void setUnsaturatedRegenRate(int rate) {}
    @Override public int getStarvationRate() { return 80; }
    @Override public void setStarvationRate(int rate) {}
    @Override public @Nullable Location getLastDeathLocation() { return null; }
    @Override public void setLastDeathLocation(@Nullable Location location) {}
}
