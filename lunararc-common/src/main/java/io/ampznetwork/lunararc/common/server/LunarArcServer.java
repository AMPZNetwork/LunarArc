package io.ampznetwork.lunararc.common.server;

import net.minecraft.server.MinecraftServer;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.*;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapView;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

public abstract class LunarArcServer implements Server {
    protected final MinecraftServer server;
    protected final Logger logger = Logger.getLogger("Minecraft");
    protected final PluginManager pluginManager = new SimplePluginManager(this, null);
    protected final ServicesManager servicesManager = new SimpleServicesManager();

    public LunarArcServer(MinecraftServer server) {
        this.server = server;
    }

    @Override public @NotNull String getName() { return "LunarArc"; }
    @Override public @NotNull String getVersion() { return "LunarArc-1.21.1"; }
    @Override public @NotNull String getMinecraftVersion() { return "1.21.1"; }
    @Override public @NotNull String getBukkitVersion() { return "1.21.1-R0.1-SNAPSHOT"; }
    @Override public @NotNull Logger getLogger() { return logger; }
    @Override public @NotNull PluginManager getPluginManager() { return pluginManager; }
    @Override public @NotNull ServicesManager getServicesManager() { return servicesManager; }
    
    @Override public @NotNull Collection<? extends Player> getOnlinePlayers() { return Collections.emptyList(); }
    @Override public @Nullable BukkitScheduler getScheduler() { return null; }
    @Override public int getMaxPlayers() { return 20; }
    @Override public void setMaxPlayers(int maxPlayers) {}
    @Override public @NotNull List<World> getWorlds() { return Collections.emptyList(); }
    @Override public @Nullable World getWorld(@NotNull net.kyori.adventure.key.Key key) { return null; }
    @Override public @Nullable Player getPlayer(@NotNull String name) { return null; }
    @Override public @Nullable Player getPlayerExact(@NotNull String name) { return null; }
    @Override public @Nullable Player getPlayer(@NotNull UUID id) { return null; }
    @Override public @Nullable UUID getPlayerUniqueId(@NotNull String name) { return null; }
    @Override public @NotNull List<Player> matchPlayer(@NotNull String name) { return Collections.emptyList(); }
    @Override public @NotNull String getUpdateFolder() { return "update"; }
    @Override public @NotNull File getPluginsFolder() { return new File("plugins"); }
    @Override public @NotNull File getUpdateFolderFile() { return new File("update"); }
    @Override public int broadcastMessage(@NotNull String message) { return 0; }
    @Override public @Nullable org.bukkit.command.PluginCommand getPluginCommand(@NotNull String name) { return null; }
    @Override public boolean dispatchCommand(@NotNull CommandSender sender, @NotNull String commandLine) { return false; }

    @Override public @NotNull List<Entity> selectEntities(@NotNull CommandSender sender, @NotNull String selector) { return Collections.emptyList(); }
    @Override public @NotNull StructureManager getStructureManager() { return null; }
    @Override public @Nullable <T extends Keyed> Registry<T> getRegistry(@NotNull Class<T> type) { return null; }
    @Override public @NotNull UnsafeValues getUnsafe() { return null; }
    @Override public @NotNull Spigot spigot() { return null; }

    @Override public @Nullable LootTable getLootTable(@NotNull NamespacedKey key) { return null; }
    @Override public @NotNull BlockData createBlockData(@NotNull Material material) { return null; }
    @Override public @NotNull BlockData createBlockData(@NotNull Material material, @Nullable Consumer<? super BlockData> consumer) { return null; }
    @Override public @NotNull BlockData createBlockData(@NotNull String data) throws IllegalArgumentException { return null; }
    @Override public @NotNull BlockData createBlockData(@Nullable Material material, @Nullable String data) throws IllegalArgumentException { return null; }
    @Override public int getPort() { return 25565; }
    @Override public int getViewDistance() { return 10; }
    @Override public int getSimulationDistance() { return 10; }
    @Override public @NotNull String getIp() { return ""; }
    @Override public @NotNull String getWorldType() { return "DEFAULT"; }
    @Override public boolean getGenerateStructures() { return true; }
    @Override public int getSpawnRadius() { return 10; }
    @Override public void setSpawnRadius(int value) {}
    @Override public boolean isHardcore() { return false; }
    @Override public boolean getAllowFlight() { return true; }
    @Override public boolean getOnlineMode() { return true; }
    @Override public boolean getHideOnlinePlayers() { return false; }
    @Override public boolean getAllowNether() { return true; }
    @Override public boolean getAllowEnd() { return true; }
    @Override public boolean hasWhitelist() { return false; }
    @Override public void setWhitelist(boolean value) {}
    @Override public boolean isWhitelistEnforced() { return false; }
    @Override public void setWhitelistEnforced(boolean value) {}
    @Override public @NotNull Set<OfflinePlayer> getWhitelistedPlayers() { return Collections.emptySet(); }
    @Override public void reloadWhitelist() {}
    @Override public int broadcast(@NotNull String message, @NotNull String permission) { return 0; }
    @Override public int broadcast(@NotNull net.kyori.adventure.text.Component message, @NotNull String permission) { return 0; }
    @Override public int broadcast(@NotNull net.kyori.adventure.text.Component message) { return 0; }
    @Override public @NotNull World createWorld(@NotNull WorldCreator creator) { return null; }
    @Override public boolean unloadWorld(@NotNull String name, boolean save) { return false; }
    @Override public boolean unloadWorld(@NotNull World world, boolean save) { return false; }
    @Override public @Nullable World getWorld(@NotNull String name) { return null; }
    @Override public @Nullable World getWorld(@NotNull UUID uid) { return null; }
    @Override public @NotNull MapView createMap(@NotNull World world) { return null; }
    @Override public @Nullable MapView getMap(int id) { return null; }
    @Override public void reload() {}
    @Override public void reloadData() {}
    @Override public void savePlayers() {}
    @Override public void updateResources() {}
    @Override public boolean addRecipe(@Nullable Recipe recipe) { return false; }
    @Override public boolean addRecipe(@Nullable Recipe recipe, boolean b) { return false; }
    @Override public boolean removeRecipe(@NotNull NamespacedKey key) { return false; }
    @Override public boolean removeRecipe(@NotNull NamespacedKey key, boolean b) { return false; }
    @Override public @Nullable Recipe getRecipe(@NotNull NamespacedKey key) { return null; }
    @Override public @NotNull List<Recipe> getRecipesFor(@NotNull ItemStack result) { return Collections.emptyList(); }
    @Override public @NotNull Iterator<Recipe> recipeIterator() { return Collections.emptyIterator(); }
    @Override public void clearRecipes() {}
    @Override public void resetRecipes() {}
    @Override public void updateRecipes() {}
    @Override public @NotNull org.bukkit.inventory.ItemCraftResult craftItemResult(@NotNull ItemStack[] items, @NotNull World world) { return null; }
    @Override public @NotNull org.bukkit.inventory.ItemCraftResult craftItemResult(@NotNull ItemStack[] items, @NotNull World world, @NotNull Player player) { return null; }
    @Override public @Nullable ItemStack craftItem(@NotNull ItemStack[] items, @NotNull World world) { return null; }
    @Override public @Nullable ItemStack craftItem(@NotNull ItemStack[] items, @NotNull World world, @NotNull Player player) { return null; }
    @Override public @Nullable Recipe getCraftingRecipe(@NotNull ItemStack[] items, @NotNull World world) { return null; }
    @Override public @NotNull Map<String, String[]> getCommandAliases() { return Collections.emptyMap(); }
    @Override public int getTicksPerAnimalSpawns() { return 400; }
    @Override public int getTicksPerMonsterSpawns() { return 1; }
    @Override public int getTicksPerWaterSpawns() { return 1; }
    @Override public int getTicksPerWaterAmbientSpawns() { return 1; }
    @Override public int getTicksPerWaterUndergroundCreatureSpawns() { return 1; }
    @Override public int getTicksPerAmbientSpawns() { return 1; }
    @Override public @NotNull ConsoleCommandSender getConsoleSender() { return null; }
    @Override public @NotNull File getWorldContainer() { return new File("."); }
    @Override public @NotNull OfflinePlayer[] getOfflinePlayers() { return new OfflinePlayer[0]; }
    @Override public @NotNull OfflinePlayer getOfflinePlayer(@NotNull String name) { return null; }
    @Override public @NotNull OfflinePlayer getOfflinePlayer(@NotNull UUID id) { return null; }
    @Override public @Nullable OfflinePlayer getOfflinePlayerIfCached(@NotNull String name) { return null; }
    @Override public @NotNull Messenger getMessenger() { return null; }
    @Override public void sendPluginMessage(@NotNull org.bukkit.plugin.Plugin source, @NotNull String channel, byte[] message) {}
    @Override public @NotNull Set<String> getListeningPluginChannels() { return Collections.emptySet(); }
    @Override public @NotNull HelpMap getHelpMap() { return null; }
    @Override public @NotNull Inventory createInventory(@Nullable InventoryHolder owner, @NotNull InventoryType type) { return null; }
    @Override public @NotNull Inventory createInventory(@Nullable InventoryHolder owner, @NotNull InventoryType type, @NotNull String title) { return null; }
    @Override public @NotNull Inventory createInventory(@Nullable InventoryHolder owner, int size) throws IllegalArgumentException { return null; }
    @Override public @NotNull Inventory createInventory(@Nullable InventoryHolder owner, int size, @NotNull String title) throws IllegalArgumentException { return null; }
    @Override public @NotNull Merchant createMerchant(@Nullable String title) { return null; }
    @Override public int getMonsterSpawnLimit() { return 70; }
    @Override public int getMaxWorldSize() { return 29999984; }
    @Override public int getAnimalSpawnLimit() { return 10; }
    @Override public long getConnectionThrottle() { return 4000; }
    @Override public int getWaterAnimalSpawnLimit() { return 5; }
    @Override public int getWaterAmbientSpawnLimit() { return 20; }
    @Override public int getWaterUndergroundCreatureSpawnLimit() { return 5; }
    @Override public int getAmbientSpawnLimit() { return 15; }
    @Override public boolean isPrimaryThread() { return true; }
    @Override public @NotNull String getMotd() { return "A LunarArc Server"; }
    @Override public @NotNull String getResourcePack() { return ""; }
    @Override public @NotNull String getResourcePackHash() { return ""; }
    @Override public @Nullable org.bukkit.packs.ResourcePack getServerResourcePack() { return null; }
    @Override public @Nullable String getShutdownMessage() { return "Server closed"; }
    @Override public @NotNull Warning.WarningState getWarningState() { return Warning.WarningState.DEFAULT; }
    @Override public @NotNull ScoreboardManager getScoreboardManager() { return null; }
    @Override public @Nullable CachedServerIcon getServerIcon() { return null; }
    @Override public @NotNull CachedServerIcon loadServerIcon(@NotNull File file) throws Exception { return null; }
    @Override public @NotNull CachedServerIcon loadServerIcon(@NotNull BufferedImage image) throws Exception { return null; }
    @Override public void setIdleTimeout(int threshold) {}
    @Override public int getIdleTimeout() { return 0; }
    @Override public @NotNull ChunkGenerator.ChunkData createChunkData(@NotNull World world) { return null; }
    @Override public @NotNull org.bukkit.WorldBorder createWorldBorder() { return null; }
    @Override public @NotNull BossBar createBossBar(@Nullable String title, @NotNull BarColor color, @NotNull BarStyle style, @NotNull BarFlag... flags) { return null; }
    @Override public @NotNull ItemStack createExplorerMap(@NotNull World world, @NotNull Location location, @NotNull org.bukkit.StructureType structureType) { return null; }
    @Override public @NotNull ItemStack createExplorerMap(@NotNull World world, @NotNull Location location, @NotNull org.bukkit.StructureType structureType, int radius, boolean findUnexplored) { return null; }
    @Override public @NotNull ItemStack createExplorerMap(@NotNull World world, @NotNull Location location, @NotNull org.bukkit.generator.structure.StructureType structureType, @NotNull org.bukkit.map.MapCursor.Type mapCursorType, int radius, boolean findUnexplored) { return null; }
    @Override public @NotNull KeyedBossBar createBossBar(@NotNull NamespacedKey key, @Nullable String title, @NotNull BarColor color, @NotNull BarStyle style, @NotNull BarFlag... flags) { return null; }
    @Override public @NotNull Iterator<KeyedBossBar> getBossBars() { return Collections.emptyIterator(); }
    @Override public @Nullable KeyedBossBar getBossBar(@NotNull NamespacedKey key) { return null; }
    @Override public boolean removeBossBar(@NotNull NamespacedKey key) { return false; }
    @Override public @Nullable Entity getEntity(@NotNull UUID uuid) { return null; }
    @Override public @NotNull Advancement getAdvancement(@NotNull NamespacedKey key) { return null; }
    @Override public @NotNull Iterator<Advancement> advancementIterator() { return Collections.emptyIterator(); }
    @Override public @NotNull Set<String> getIPBans() { return Collections.emptySet(); }
    @Override public void banIP(@NotNull String address) {}
    @Override public void unbanIP(@NotNull String address) {}
    @Override public void banIP(@NotNull java.net.InetAddress address) {}
    @Override public void unbanIP(@NotNull java.net.InetAddress address) {}
    @Override public @NotNull Set<OfflinePlayer> getBannedPlayers() { return Collections.emptySet(); }
    @Override public @NotNull BanList getBanList(@NotNull BanList.Type type) { return null; }
    @Override public @NotNull <B extends BanList<E>, E> B getBanList(@NotNull io.papermc.paper.ban.BanListType<B> type) { return null; }
    @Override public @NotNull Set<OfflinePlayer> getOperators() { return Collections.emptySet(); }
    @Override public void shutdown() {}

    @Override public @NotNull PlayerProfile createPlayerProfile(@Nullable UUID uniqueId, @Nullable String name) { return null; }
    @Override public @NotNull PlayerProfile createPlayerProfile(@NotNull UUID uniqueId) { return null; }
    @Override public @NotNull PlayerProfile createPlayerProfile(@NotNull String name) { return null; }
    @Override public int getSpawnLimit(@NotNull SpawnCategory category) { return 0; }
    @Override public int getTicksPerSpawns(@NotNull SpawnCategory category) { return 0; }
    
    @Override public void sendMessage(@NotNull net.kyori.adventure.identity.Identity source, @NotNull net.kyori.adventure.text.Component message, @NotNull net.kyori.adventure.audience.MessageType type) {}
    @Override public void sendMessage(@NotNull net.kyori.adventure.text.Component message) {}
    
    @Override public @NotNull <T extends Keyed> Tag<T> getTag(@NotNull String registry, @NotNull NamespacedKey tag, @NotNull Class<T> clazz) { return null; }
    @Override public boolean isOwnedByCurrentRegion(@NotNull Entity entity) { return true; }
    @Override public boolean isOwnedByCurrentRegion(@NotNull World world, int x, int z) { return true; }
    @Override public boolean isOwnedByCurrentRegion(@NotNull Location location) { return true; }
    @Override public boolean isOwnedByCurrentRegion(@NotNull World world, int x, int y, int z) { return true; }
    @Override public boolean isOwnedByCurrentRegion(@NotNull Location location, int radius) { return true; }
    @Override public boolean isOwnedByCurrentRegion(@NotNull World world, @NotNull io.papermc.paper.math.Position position, int radius) { return true; }
    @Override public boolean isOwnedByCurrentRegion(@NotNull World world, @NotNull io.papermc.paper.math.Position position) { return true; }
    
    @Override public @NotNull io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler getGlobalRegionScheduler() { return null; }
    @Override public @NotNull io.papermc.paper.threadedregions.scheduler.AsyncScheduler getAsyncScheduler() { return null; }
    @Override public @NotNull io.papermc.paper.threadedregions.scheduler.RegionScheduler getRegionScheduler() { return null; }
    
    @Override public @NotNull org.bukkit.inventory.ItemFactory getItemFactory() { return null; }
    @Override public @NotNull org.bukkit.potion.PotionBrewer getPotionBrewer() { return null; }
    @Override public @NotNull org.bukkit.command.CommandMap getCommandMap() { return null; }
    @Override public @NotNull com.destroystokyo.paper.entity.ai.MobGoals getMobGoals() { return null; }
    @Override public @NotNull org.bukkit.ServerLinks getServerLinks() { return null; }
    @Override public @NotNull org.bukkit.packs.DataPackManager getDataPackManager() { return null; }
    @Override public @NotNull io.papermc.paper.datapack.DatapackManager getDatapackManager() { return null; }
    @Override public @NotNull List<String> getInitialDisabledPacks() { return Collections.emptyList(); }
    @Override public @NotNull List<String> getInitialEnabledPacks() { return Collections.emptyList(); }
    @Override public @NotNull org.bukkit.ServerTickManager getServerTickManager() { return null; }
    @Override public boolean isStopping() { return false; }
    @Override public boolean isLoggingIPs() { return true; }
    @Override public boolean isTickingWorlds() { return false; }
    @Override public boolean isResourcePackRequired() { return false; }
    @Override public @Nullable String getResourcePackPrompt() { return null; }
    @Override public boolean isAcceptingTransfers() { return false; }
    @Override public boolean isEnforcingSecureProfiles() { return false; }
    @Override public boolean shouldSendChatPreviews() { return false; }
    @Override public double[] getTPS() { return new double[]{20.0, 20.0, 20.0}; }
    @Override public long[] getTickTimes() { return new long[0]; }
    @Override public double getAverageTickTime() { return 0.0; }
    @Override public int getCurrentTick() { return 0; }
    @Override public boolean reloadCommandAliases() { return true; }
    @Override public void reloadPermissions() {}
    @Override public @NotNull <T extends Keyed> Iterable<Tag<T>> getTags(@NotNull String registry, @NotNull Class<T> clazz) { return Collections.emptyList(); }
    @Override public @Nullable org.bukkit.scoreboard.Criteria getScoreboardCriteria(@NotNull String name) { return null; }
    
    @Override public @NotNull com.destroystokyo.paper.profile.PlayerProfile createProfile(@NotNull UUID uuid) { return null; }
    @Override public @NotNull com.destroystokyo.paper.profile.PlayerProfile createProfile(@NotNull String name) { return null; }
    @Override public @NotNull com.destroystokyo.paper.profile.PlayerProfile createProfile(@Nullable UUID uuid, @Nullable String name) { return null; }
    @Override public @NotNull com.destroystokyo.paper.profile.PlayerProfile createProfileExact(@Nullable UUID uuid, @Nullable String name) { return null; }

    @Override public @NotNull net.kyori.adventure.text.Component motd() { return net.kyori.adventure.text.Component.text(getMotd()); }
    @Override public void motd(@NotNull net.kyori.adventure.text.Component motd) {}
    @Override public @NotNull net.kyori.adventure.text.Component shutdownMessage() { return net.kyori.adventure.text.Component.text(getShutdownMessage()); }
    @Override public @NotNull org.bukkit.entity.EntityFactory getEntityFactory() { return null; }
    @Override public @NotNull org.bukkit.GameMode getDefaultGameMode() { return org.bukkit.GameMode.SURVIVAL; }
    @Override public void setDefaultGameMode(@NotNull org.bukkit.GameMode mode) {}
    @Override public int getMaxChainedNeighborUpdates() { return 1000000; }
    @Override public void setMotd(@NotNull String motd) {}
    @Override public boolean suggestPlayerNamesWhenNullTabCompletions() { return true; }
    @Override public @Nullable String getPermissionMessage() { return ""; }
    @Override public @NotNull net.kyori.adventure.text.Component permissionMessage() { return net.kyori.adventure.text.Component.empty(); }
    @Override public @NotNull Iterable<? extends net.kyori.adventure.audience.Audience> audiences() { return Collections.emptyList(); }
    
    @Override public @NotNull CommandSender createCommandSender(@NotNull Consumer<? super net.kyori.adventure.text.Component> feedback) { return null; }
    @Override public @NotNull Inventory createInventory(@Nullable InventoryHolder owner, @NotNull InventoryType type, @NotNull net.kyori.adventure.text.Component title) { return null; }
    @Override public @NotNull Inventory createInventory(@Nullable InventoryHolder owner, int size, @NotNull net.kyori.adventure.text.Component title) throws IllegalArgumentException { return null; }
    @Override public @NotNull Merchant createMerchant(@NotNull net.kyori.adventure.text.Component title) { return null; }
}
