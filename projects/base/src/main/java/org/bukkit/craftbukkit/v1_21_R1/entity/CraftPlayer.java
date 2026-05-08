package org.bukkit.craftbukkit.v1_21_R1.entity;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.craftbukkit.v1_21_R1.CraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;
import java.util.Set;
import java.util.Collections;
import java.net.InetSocketAddress;
import net.md_5.bungee.api.chat.BaseComponent;
import com.destroystokyo.paper.Title;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.map.MapView;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.Material;
import org.bukkit.GameMode;
import io.ampznetwork.lunararc.common.bridge.EntityBridge;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import java.util.concurrent.CompletableFuture;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.function.Consumer;
import org.bukkit.util.Vector;
import org.bukkit.entity.Projectile;
import org.bukkit.damage.DamageSource;
import net.kyori.adventure.util.TriState;
import org.bukkit.util.RayTraceResult;
import org.bukkit.entity.Item;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.FluidCollisionMode;
import org.bukkit.entity.FishHook;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.Merchant;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Firework;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.memory.MemoryKey;
import io.papermc.paper.entity.Frictional;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.entity.TargetEntityInfo;

@SuppressWarnings("all")
public class CraftPlayer extends CraftHumanEntity implements Player {
    // Removed redundant player field to fix initialization order NPEs

    public CraftPlayer(CraftServer server, ServerPlayer player) {
        super(server, player);
    }

    public ServerPlayer getHandle() {
        return (ServerPlayer) entity;
    }

    @Override
    public String getName() {
        return getHandle().getScoreboardName();
    }

    @Override
    public UUID getUniqueId() {
        return getHandle().getUUID();
    }

    @Override
    public InetSocketAddress getAddress() {
        if (getHandle().connection == null) return null;
        return (InetSocketAddress) getHandle().connection.getRemoteAddress();
    }

    @Override
    public void sendMessage(String message) {
        getHandle().sendSystemMessage(net.minecraft.network.chat.Component.literal(message));
    }

    @Override
    public void sendMessage(String... messages) {
        for (String msg : messages) sendMessage(msg);
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public void setDisplayName(String name) {}

    @Override
    public void kickPlayer(String message) {
        getHandle().connection.disconnect(net.minecraft.network.chat.Component.literal(message == null ? "Kicked" : message));
    }

    @Override
    public boolean isOp() {
        return server.getHandle().getPlayerList().isOp(getHandle().getGameProfile());
    }

    @Override
    public void setOp(boolean value) {
        if (value) server.getHandle().getPlayerList().op(getHandle().getGameProfile());
        else server.getHandle().getPlayerList().deop(getHandle().getGameProfile());
    }

    // Delegate to Proxy for all other Player methods to avoid 3000 lines of stubs
    private final Player proxy;
    {
        this.proxy = (Player) java.lang.reflect.Proxy.newProxyInstance(
            Player.class.getClassLoader(),
            new Class<?>[] { Player.class },
            (p, method, args) -> {
                try {
                    return CraftPlayer.class.getMethod(method.getName(), method.getParameterTypes()).invoke(this, args);
                } catch (NoSuchMethodException e) {
                    if (method.getReturnType().equals(boolean.class)) return false;
                    if (method.getReturnType().equals(int.class)) return 0;
                    if (method.getReturnType().equals(double.class)) return 0.0;
                    if (method.getReturnType().equals(float.class)) return 0.0f;
                    if (method.getReturnType().equals(long.class)) return 0L;
                    return null;
                }
            }
        );
    }
    
    @Override public InetSocketAddress getHAProxyAddress() { return null; }
    @Override public boolean isTransferred() { return false; }
    @Override public CompletableFuture<byte[]> retrieveCookie(NamespacedKey key) { return CompletableFuture.completedFuture(null); }
    @Override public void storeCookie(NamespacedKey key, byte[] value) {}
    @Override public void transfer(String host, int port) {}
    
    @Override public void kick() { kick(net.kyori.adventure.text.Component.empty()); }
    @Override public void kick(net.kyori.adventure.text.Component message) {}
    @Override public void kick(net.kyori.adventure.text.Component message, org.bukkit.event.player.PlayerKickEvent.Cause cause) {}

    @Override public <E extends org.bukkit.BanEntry<? super PlayerProfile>> E ban(String reason, Date expires, String source, boolean kickPlayer) { return null; }
    @Override public <E extends org.bukkit.BanEntry<? super PlayerProfile>> E ban(String reason, Instant expires, String source, boolean kickPlayer) { return null; }
    @Override public <E extends org.bukkit.BanEntry<? super PlayerProfile>> E ban(String reason, Duration duration, String source, boolean kickPlayer) { return null; }
    @Override public org.bukkit.BanEntry<java.net.InetAddress> banIp(String reason, Date expires, String source, boolean kickPlayer) { return null; }
    @Override public org.bukkit.BanEntry<java.net.InetAddress> banIp(String reason, Instant expires, String source, boolean kickPlayer) { return null; }
    @Override public org.bukkit.BanEntry<java.net.InetAddress> banIp(String reason, Duration duration, String source, boolean kickPlayer) { return null; }

    @Override public void chat(String msg) {}
    @Override public boolean performCommand(String command) { return false; }
    
    @Override public boolean isOnGround() { return getHandle().onGround(); }
    @Override public boolean isSneaking() { return getHandle().isShiftKeyDown(); }
    @Override public void setSneaking(boolean sneak) { getHandle().setShiftKeyDown(sneak); }
    @Override public boolean isSprinting() { return getHandle().isSprinting(); }
    @Override public void setSprinting(boolean sprinting) {}
    
    @Override public void saveData() {}
    @Override public void loadData() {}
    @Override public void setSleepingIgnored(boolean ignored) {}
    @Override public boolean isSleepingIgnored() { return false; }
    
    @Override public Location getBedSpawnLocation() { return null; }
    @Override public Location getRespawnLocation() { return null; }
    @Override public void setBedSpawnLocation(Location loc) {}
    @Override public void setRespawnLocation(Location loc) {}
    @Override public void setBedSpawnLocation(Location loc, boolean force) {}
    @Override public void setRespawnLocation(Location loc, boolean force) {}

    @Override public void playNote(Location loc, byte instrument, byte note) {}
    @Override public void playNote(Location loc, org.bukkit.Instrument instrument, org.bukkit.Note note) {}
    @Override public void playSound(Location loc, org.bukkit.Sound sound, float volume, float pitch) {}
    @Override public void playSound(Location loc, String sound, float volume, float pitch) {}
    @Override public void playSound(Location loc, org.bukkit.Sound sound, org.bukkit.SoundCategory category, float volume, float pitch) {}
    @Override public void playSound(Location loc, String sound, org.bukkit.SoundCategory category, float volume, float pitch) {}
    @Override public void playSound(Location loc, org.bukkit.Sound sound, org.bukkit.SoundCategory category, float volume, float pitch, long seed) {}
    @Override public void playSound(Location loc, String sound, org.bukkit.SoundCategory category, float volume, float pitch, long seed) {}
    @Override public void playSound(Entity entity, org.bukkit.Sound sound, float volume, float pitch) {}
    @Override public void playSound(Entity entity, String sound, float volume, float pitch) {}
    @Override public void playSound(Entity entity, org.bukkit.Sound sound, org.bukkit.SoundCategory category, float volume, float pitch) {}
    @Override public void playSound(Entity entity, String sound, org.bukkit.SoundCategory category, float volume, float pitch) {}
    @Override public void playSound(Entity entity, org.bukkit.Sound sound, org.bukkit.SoundCategory category, float volume, float pitch, long seed) {}
    @Override public void playSound(Entity entity, String sound, org.bukkit.SoundCategory category, float volume, float pitch, long seed) {}
    @Override public void stopSound(org.bukkit.Sound sound) {}
    @Override public void stopSound(String sound) {}
    @Override public void stopSound(org.bukkit.Sound sound, org.bukkit.SoundCategory category) {}
    @Override public void stopSound(String sound, org.bukkit.SoundCategory category) {}
    @Override public void stopSound(org.bukkit.SoundCategory category) {}
    @Override public void stopAllSounds() {}

    @Override public void playEffect(Location loc, org.bukkit.Effect effect, int data) {}
    @Override public <T> void playEffect(Location loc, org.bukkit.Effect effect, T data) {}
    @Override public boolean breakBlock(org.bukkit.block.Block block) { return false; }

    @Override public boolean isOnline() { return true; }
    @Override public boolean isBanned() { return false; }
    @Override public boolean isWhitelisted() { return true; }
    @Override public void setWhitelisted(boolean value) {}
    
    @Override public void setStatistic(Statistic statistic, int newValue) {}
    @Override public int getStatistic(Statistic statistic) { return 0; }
    
    @Override public Location getLocation() { return super.getLocation(); }
    @Override public boolean teleport(Location location) { return super.teleport(location); }
    @Override public Set<Player> getTrackedPlayers() { return Collections.emptySet(); }

    @Override public Player.Spigot spigot() { return new Player.Spigot(); }
    @Override public boolean listPlayer(Player player) { return true; }
    @Override public boolean unlistPlayer(Player player) { return true; }
    @Override public boolean isListed(Player player) { return true; }
    @Override public boolean canSee(Entity entity) { return true; }
    @Override public boolean canSee(Player player) { return true; }
    @Override public void hidePlayer(Player player) {}
    @Override public void showPlayer(Player player) {}
    @Override public void hidePlayer(org.bukkit.plugin.Plugin plugin, Player player) {}
    @Override public void showPlayer(org.bukkit.plugin.Plugin plugin, Player player) {}
    @Override public void hideEntity(org.bukkit.plugin.Plugin plugin, Entity entity) {}
    @Override public void showEntity(org.bukkit.plugin.Plugin plugin, Entity entity) {}


    @Override public float getWalkSpeed() { return 0.2f; }
    @Override public float getFlySpeed() { return 0.1f; }
    @Override public void setFlySpeed(float value) {}
    @Override public void setWalkSpeed(float value) {}
    @Override public void setFlying(boolean value) {}
    @Override public boolean isFlying() { return false; }
    @Override public void setAllowFlight(boolean value) {}
    @Override public boolean getAllowFlight() { return false; }
    @Override public TriState hasFlyingFallDamage() { return TriState.NOT_SET; }
    @Override public void setFlyingFallDamage(TriState value) {}

    @Override public net.kyori.adventure.text.Component name() { return net.kyori.adventure.text.Component.text(getName()); }
    @Override public void sendEntityEffect(EntityEffect effect, Entity entity) {}
    @Override public void showTitle(net.kyori.adventure.title.Title title) { getHandle().sendSystemMessage(net.minecraft.network.chat.Component.literal("Title: " + title.toString())); }
    @Override public void clearTitle() {}
    @Override public void sendTitle(com.destroystokyo.paper.Title title) {}
    @Override public void updateTitle(com.destroystokyo.paper.Title title) {}
    @Override public void setTitleTimes(int fadeIn, int stay, int fadeOut) {}
    @Override public void setSubtitle(BaseComponent[] subtitle) {}
    @Override public void setSubtitle(BaseComponent subtitle) {}
    @Override public void showTitle(BaseComponent[] title) {}
    @Override public void showTitle(BaseComponent title) {}
    @Override public void showTitle(BaseComponent[] title, BaseComponent[] subtitle, int fadeIn, int stay, int fadeOut) {}
    @Override public void showTitle(BaseComponent title, BaseComponent subtitle, int fadeIn, int stay, int fadeOut) {}
    @Override public void setPlayerListHeaderFooter(BaseComponent[] header, BaseComponent[] footer) {}
    @Override public void setPlayerListHeaderFooter(BaseComponent header, BaseComponent footer) {}
    @Override public String getPlayerListHeader() { return ""; }
    @Override public String getPlayerListFooter() { return ""; }
    @Override public void sendActionBar(BaseComponent... message) {}
    @Override public void sendActionBar(char colorChar, String message) {}
    @Override public void sendActionBar(String message) {}
    @Override public void setHasSeenWinScreen(boolean hasSeenWinScreen) {}
    @Override public boolean hasSeenWinScreen() { return false; }
    @Override public void showWinScreen() {}
    @Override public Component displayName() { return Component.text(getName()); }
    @Override public void displayName(Component displayName) {}
    @Override public Component playerListName() { return null; }
    @Override public void playerListName(Component name) {}
    @Override public Component playerListHeader() { return null; }
    @Override public Component playerListFooter() { return null; }
    @Override public void setPlayerListHeader(String header) {}
    @Override public void setPlayerListFooter(String footer) {}
    @Override public void setPlayerListHeaderFooter(String header, String footer) {}
    @Override public String getPlayerListName() { return getName(); }
    @Override public void setPlayerListName(String name) {}
    @Override public void setCompassTarget(Location loc) {}
    @Override public Firework fireworkBoost(ItemStack stack) { return null; }
    @Override public Location getCompassTarget() { return new Location(getWorld(), 0, 0, 0); }
    @Override public Iterable<? extends BossBar> activeBossBars() { return java.util.Collections.emptyList(); }
    @Override public void sendExperienceChange(float progress) {}
    @Override public void sendMap(MapView map) {}
    @Override public void sendRawMessage(String message) { sendMessage(message); }
    @Override public void sendBlockChange(Location loc, Material material, byte data) {}
    @Override public void sendBlockChange(Location loc, org.bukkit.block.data.BlockData block) {}
    @Override public void sendBlockChanges(Collection<org.bukkit.block.BlockState> states) {}
    @Override public void sendBlockChanges(Collection<org.bukkit.block.BlockState> states, boolean ignoreAir) {}
    @Override public void sendBlockDamage(Location loc, float progress) {}
    @Override public void sendBlockDamage(Location loc, float progress, Entity entity) {}
    @Override public void sendBlockDamage(Location loc, float progress, int entityId) {}
    @Override public void sendEquipmentChange(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {}
    @Override public void sendEquipmentChange(LivingEntity entity, Map<EquipmentSlot, ItemStack> equipment) {}
    @Override public void sendSignChange(Location loc, String[] lines) {}
    @Override public void sendSignChange(Location loc, String[] lines, DyeColor dyeColor) {}
    @Override public void sendSignChange(Location loc, String[] lines, DyeColor dyeColor, boolean hasGlowingText) {}
    @Override public void sendPluginMessage(org.bukkit.plugin.Plugin plugin, String channel, byte[] message) {}
    @Override public void sendPotionEffectChange(LivingEntity entity, PotionEffect effect) {}
    @Override public void sendPotionEffectChangeRemove(LivingEntity entity, PotionEffectType type) {}
    @Override public void sendBlockUpdate(Location loc, org.bukkit.block.TileState state) {}
    @Override public void sendSignChange(Location loc, List<? extends net.kyori.adventure.text.Component> lines, DyeColor dyeColor, boolean hasGlowingText) {}
    @Override public void sendHealthUpdate(double health, int foodLevel, float saturation) {}
    @Override public void sendHealthUpdate() {}
    @Override public void sendMultiBlockChange(Map<? extends io.papermc.paper.math.Position, org.bukkit.block.data.BlockData> blocks) {}
    @Override public void hideTitle() {}
    @Override public void sendHurtAnimation(float yaw) {}
    @Override public void sendLinks(org.bukkit.ServerLinks links) {}
    @Override public void addCustomChatCompletions(Collection<String> completions) {}
    @Override public void removeCustomChatCompletions(Collection<String> completions) {}
    @Override public void setCustomChatCompletions(Collection<String> completions) {}
    @Override public void updateInventory() {}
    @Override public GameMode getPreviousGameMode() { return null; }
    @Override public void setPlayerTime(long time, boolean relative) {}
    @Override public long getPlayerTime() { return 0L; }
    @Override public long getPlayerTimeOffset() { return 0L; }
    @Override public boolean isPlayerTimeRelative() { return true; }
    @Override public void resetPlayerTime() {}
    @Override public void setPlayerWeather(org.bukkit.WeatherType type) {}
    @Override public org.bukkit.WeatherType getPlayerWeather() { return null; }
    @Override public void resetPlayerWeather() {}
    @Override public int getExpCooldown() { return 0; }
    @Override public void setExpCooldown(int ticks) {}
    @Override public void giveExp(int amount, boolean applyMending) {}
    @Override public int applyMending(int amount) { return amount; }
    @Override public void giveExpLevels(int levels) {}
    @Override public void setExp(float exp) {}
    @Override public float getExp() { return 0; }
    @Override public void sendExperienceChange(float progress, int level) {}
    @Override public void setExperienceLevelAndProgress(int level) {}
    @Override public void setLevel(int level) {}
    @Override public int getLevel() { return 0; }
    @Override public void setTotalExperience(int exp) {}
    @Override public int getTotalExperience() { return 0; }
    @Override public int getExperiencePointsNeededForNextLevel() { return 0; }
    @Override public int calculateTotalExperiencePoints() { return 0; }
    @Override public boolean isChunkSent(long chunk) { return false; }
    @Override public Set<Chunk> getSentChunks() { return Collections.emptySet(); }
    @Override public Set<Long> getSentChunkKeys() { return Collections.emptySet(); }
    @Override public void resetIdleDuration() {}
    @Override public Duration getIdleDuration() { return Duration.ZERO; }
    @Override public void setRotation(float yaw, float pitch) {}
    @Override public void lookAt(double x, double y, double z, io.papermc.paper.entity.LookAnchor anchor) {}
    @Override public void lookAt(Entity entity, io.papermc.paper.entity.LookAnchor anchor, io.papermc.paper.entity.LookAnchor anchor2) {}
    @Override public void showElderGuardian(boolean silent) {}
    @Override public int getWardenWarningCooldown() { return 0; }
    @Override public void setWardenWarningCooldown(int cooldown) {}
    @Override public int getWardenTimeSinceLastWarning() { return 0; }
    @Override public void setWardenTimeSinceLastWarning(int time) {}
    @Override public int getWardenWarningLevel() { return 0; }
    @Override public void setWardenWarningLevel(int level) {}
    @Override public void increaseWardenWarningLevel() {}
    @Override public String getClientBrandName() { return "vanilla"; }
    @Override public void sendOpLevel(byte level) {}
    @Override public void addAdditionalChatCompletions(Collection<String> completions) {}
    @Override public void removeAdditionalChatCompletions(Collection<String> completions) {}
    @Override public float getCooldownPeriod() { return 1.0f; }
    @Override public float getCooledAttackStrength(float adjustTicks) { return 1.0f; }
    @Override public void resetCooldown() {}
    @Override public <T> T getClientOption(com.destroystokyo.paper.ClientOption<T> option) { return null; }
    @Override public PlayerProfile getPlayerProfile() { return null; }
    @Override public void setPlayerProfile(PlayerProfile profile) {}
    @Override public boolean isAllowingServerListings() { return true; }
    @Override public void showDemoScreen() {}
    @Override public void updateCommands() {}
    @Override public void openBook(ItemStack book) {}
    @Override public void openSign(Sign sign) {}
    @Override public void openSign(Sign sign, Side side) {}
    @Override public int getClientViewDistance() { return 10; }
    @Override public int getPing() { return 0; }
    @Override public String getLocale() { return "en_us"; }
    @Override public java.util.Locale locale() { return java.util.Locale.US; }
    @Override public boolean getAffectsSpawning() { return true; }
    @Override public void setAffectsSpawning(boolean value) {}
    @Override public int getViewDistance() { return 10; }
    @Override public void setViewDistance(int distance) {}
    @Override public int getSimulationDistance() { return 10; }
    @Override public void setSimulationDistance(int distance) {}
    @Override public int getSendViewDistance() { return 10; }
    @Override public void setSendViewDistance(int distance) {}
    @Override public org.bukkit.advancement.AdvancementProgress getAdvancementProgress(org.bukkit.advancement.Advancement advancement) { return null; }
    @Override public void spawnParticle(Particle particle, Location location, int count) {}
    @Override public void spawnParticle(Particle particle, double x, double y, double z, int count) {}
    @Override public <T> void spawnParticle(Particle particle, Location location, int count, T data) {}
    @Override public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, T data) {}
    @Override public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ) {}
    @Override public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {}
    @Override public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, T data) {}
    @Override public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, T data) {}
    @Override public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {}
    @Override public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra) {}
    @Override public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {}
    @Override public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {}
    @Override public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data, boolean force) {}
    @Override public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data, boolean force) {}
    @Override public org.bukkit.scoreboard.Scoreboard getScoreboard() { return Bukkit.getScoreboardManager().getNewScoreboard(); }
    @Override public void setScoreboard(org.bukkit.scoreboard.Scoreboard scoreboard) {}
    @Override public org.bukkit.WorldBorder getWorldBorder() { return null; }
    @Override public void setWorldBorder(org.bukkit.WorldBorder border) {}
    @Override public boolean isHealthScaled() { return false; }
    @Override public void setHealthScaled(boolean scale) {}
    @Override public void setHealthScale(double scale) {}
    @Override public double getHealthScale() { return 20.0; }
    @Override public Entity getSpectatorTarget() { return null; }
    @Override public void setSpectatorTarget(Entity entity) {}
    @Override public void sendTitle(String title, String subtitle) {}
    @Override public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {}
    @Override public void resetTitle() {}
    @Override public void removeResourcePacks() {}
    @Override public void removeResourcePack(UUID id) {}
    @Override public void addResourcePack(UUID id, String url, byte[] hash, String prompt, boolean force) {}
    @Override public org.bukkit.event.player.PlayerResourcePackStatusEvent.Status getResourcePackStatus() { return org.bukkit.event.player.PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED; }
    @Override public void setResourcePack(String url) {}
    @Override public void setResourcePack(String url, byte[] hash) {}
    @Override public void setResourcePack(String url, byte[] hash, String prompt) {}
    @Override public void setResourcePack(String url, byte[] hash, boolean force) {}
    @Override public void setResourcePack(String url, byte[] hash, String prompt, boolean force) {}
    @Override public void setResourcePack(UUID id, String url, byte[] hash, String prompt, boolean force) {}
    @Override public void setResourcePack(UUID id, String url, byte[] hash, net.kyori.adventure.text.Component prompt, boolean force) {}
    @Override public void setTexturePack(String url) {}
    @Override public void setResourcePack(String url, byte[] hash, net.kyori.adventure.text.Component prompt, boolean force) {}
    @Override public String getResourcePackHash() { return null; }
    
    // LivingEntity
    @Override public Player getKiller() { return null; }
    @Override public void setArrowsInBody(int count, boolean remove) {}
    @Override public org.bukkit.Sound getFallDamageSoundBig() { return org.bukkit.Sound.ENTITY_PLAYER_BIG_FALL; }
    @Override public org.bukkit.Sound getFallDamageSoundSmall() { return org.bukkit.Sound.ENTITY_PLAYER_SMALL_FALL; }
    @Override public Entity getTargetEntity(int distance, boolean nonTransparent) { return null; }
    @Override public RayTraceResult rayTraceEntities(int distance, boolean nonTransparent) { return null; }
    @Override public void broadcastSlotBreak(EquipmentSlot slot) {}
    @Override public void broadcastSlotBreak(EquipmentSlot slot, Collection<Player> players) {}
    @Override public void setGliding(boolean gliding) {}
    @Override public void attack(Entity target) {}
    @Override public boolean canBreatheUnderwater() { return false; }
    @Override public void setKiller(Player killer) {}
    @Override public @Nullable Block getTargetBlock(int distance, com.destroystokyo.paper.block.TargetBlockInfo.FluidMode fluidMode) { 
        RayTraceResult result = rayTraceBlocks(distance, fluidMode == com.destroystokyo.paper.block.TargetBlockInfo.FluidMode.ALWAYS ? FluidCollisionMode.ALWAYS : FluidCollisionMode.NEVER);
        return result != null ? result.getHitBlock() : null;
    }
    @Override public @NotNull Block getTargetBlock(Set<Material> transparent, int distance) { return (Block) java.lang.reflect.Proxy.newProxyInstance(Block.class.getClassLoader(), new Class<?>[] { Block.class }, (p, m, a) -> null); }
    @Override public @NotNull List<Block> getLastTwoTargetBlocks(Set<Material> transparent, int distance) { return Collections.emptyList(); }
    @Override public int getNoDamageTicks() { return 0; }
    @Override public EntityCategory getCategory() { return EntityCategory.NONE; }
    @Override public void setRemainingAir(int air) {}
    @Override public double getEyeHeight() { return 1.62; }
    @Override public double getEyeHeight(boolean ignorePose) { return 1.62; }
    @Override public float getBodyYaw() { return 0; }
    @Override public void setBodyYaw(float yaw) {}
    @Override public org.bukkit.Sound getDrinkingSound(ItemStack itemStack) { return org.bukkit.Sound.ENTITY_GENERIC_DRINK; }
    @Override public boolean hasLineOfSight(Location location) { return false; }
    @Override public boolean hasLineOfSight(Entity other) { return false; }
    @Override public org.bukkit.Sound getHurtSound() { return org.bukkit.Sound.ENTITY_PLAYER_HURT; }
    @Override public org.bukkit.Sound getDeathSound() { return org.bukkit.Sound.ENTITY_PLAYER_DEATH; }
    @Override public Block getTargetBlockExact(int distance) { return null; }
    @Override public Block getTargetBlockExact(int distance, FluidCollisionMode fluidCollisionMode) { return null; }
    @Override public float getUpwardsMovement() { return 0; }
    @Override public float getSidewaysMovement() { return 0; }
    @Override public float getForwardsMovement() { return 0; }
    @Override public boolean isRiptiding() { return false; }
    @Override public boolean isClimbing() { return false; }
    @Override public Collection<PotionEffect> getActivePotionEffects() { return Collections.emptyList(); }
    @Override public boolean hasPotionEffect(PotionEffectType type) { return false; }
    @Override public void knockback(double strength, double x, double z) {}
    @Override public void setShieldBlockingDelay(int delay) {}
    @Override public void setArrowCooldown(int ticks) {}
    @Override public void setNoActionTicks(int ticks) {}
    @Override public void completeUsingActiveItem() {}
    @Override public boolean canUseEquipmentSlot(EquipmentSlot slot) { return true; }
    @Override public <T> void setMemory(MemoryKey<T> key, T value) {}
    @Override public <T> T getMemory(MemoryKey<T> key) { return null; }
    @Override public float getHurtDirection() { return 0; }
    @Override public void setHurtDirection(float direction) {}
    @Override public void damageItemStack(EquipmentSlot slot, int amount) {}
    @Override public ItemStack damageItemStack(ItemStack stack, int amount) { return stack; }
    @Override public void setNoDamageTicks(int ticks) {}
    @Override public boolean isGliding() { return false; }
    @Override public Set<UUID> getCollidableExemptions() { return Collections.emptySet(); }
    @Override public org.bukkit.Sound getFallDamageSound(int fallDistance) { return org.bukkit.Sound.ENTITY_GENERIC_SMALL_FALL; }
    @Override public void setNextArrowRemoval(int ticks) {}
    @Override public Entity getLeashHolder() { return null; }
    @Override public org.bukkit.block.BlockFace getTargetBlockFace(int distance, com.destroystokyo.paper.block.TargetBlockInfo.FluidMode fluidMode) { return null; }
    @Override public org.bukkit.block.BlockFace getTargetBlockFace(int distance, FluidCollisionMode fluidMode) { return null; }

    // HumanEntity
    @Override public boolean setWindowProperty(InventoryView.Property prop, int value) { return false; }
    @Override public void wakeup(boolean setSpawnLocation) {}
    @Override public ItemStack getItemOnCursor() { return new ItemStack(Material.AIR); }
    @Override public int discoverRecipes(Collection<NamespacedKey> recipes) { return 0; }
    @Override public int undiscoverRecipes(Collection<NamespacedKey> recipes) { return 0; }
    @Override public EntityEquipment getEquipment() { return (EntityEquipment) java.lang.reflect.Proxy.newProxyInstance(EntityEquipment.class.getClassLoader(), new Class<?>[] { EntityEquipment.class }, (p, m, a) -> null); }
    @Override public void setStarvationRate(int rate) {}
    @Override public void setItemOnCursor(ItemStack item) {}
    @Override public boolean isBlocking() { return getHandle().isBlocking(); }
    @Override public void setSaturatedRegenRate(int rate) {}
    @Override public float getExhaustion() { return 0; }
    @Override public boolean hasCooldown(Material material) { return false; }
    @Override public void setCooldown(Material material, int ticks) {}
    @Override public GameMode getGameMode() { return GameMode.SURVIVAL; }
    @Override public void setGameMode(GameMode mode) {}
    @Override public Entity releaseRightShoulderEntity() { return null; }
    @Override public Entity releaseLeftShoulderEntity() { return null; }
    @Override public Entity getShoulderEntityLeft() { return null; }
    @Override public Entity getShoulderEntityRight() { return null; }
    @Override public void setShoulderEntityLeft(Entity entity) {}
    @Override public void setShoulderEntityRight(Entity entity) {}
    @Override public boolean hasDiscoveredRecipe(NamespacedKey recipe) { return false; }
    @Override public int getUnsaturatedRegenRate() { return 0; }
    @Override public int getSleepTicks() { return 0; }
    @Override public boolean undiscoverRecipe(NamespacedKey recipe) { return false; }
    @Override public boolean sleep(Location location, boolean force) { return false; }
    @Override public boolean isDeeplySleeping() { return false; }
    @Override public int getStarvationRate() { return 0; }
    @Override public Set<NamespacedKey> getDiscoveredRecipes() { return Collections.emptySet(); }
    @Override public int getSaturatedRegenRate() { return 0; }
    @Override public Location getBedLocation() { return null; }
    @Override public void setSaturation(float value) {}
    @Override public FishHook getFishHook() { return null; }
    @Override public void startRiptideAttack(int duration, float attackDamage, ItemStack itemStack) {}
    @Override public boolean isHandRaised() { return false; }
    @Override public void setLastDeathLocation(Location loc) {}
    @Override public int getCooldown(Material material) { return 0; }
    @Override public void setUnsaturatedRegenRate(int rate) {}
    @Override public boolean dropItem(boolean dropAll) { return false; }
    @Override public ItemStack getItemInHand() { return new ItemStack(Material.AIR); }
    @Override public void setItemInHand(ItemStack item) {}
    @Override public float getSaturation() { return 0; }
    @Override public void setExhaustion(float value) {}
    @Override public float getAttackCooldown() { return 0; }
    @Override public int getExpToLevel() { return 0; }
    @Override public Location getPotentialBedLocation() { return null; }
    @Override 
    public PlayerInventory getInventory() { 
        return (PlayerInventory) java.lang.reflect.Proxy.newProxyInstance(
            PlayerInventory.class.getClassLoader(),
            new Class<?>[] { PlayerInventory.class },
            (p, m, a) -> {
                if (m.getName().equals("getItemInMainHand")) return new ItemStack(Material.AIR);
                if (m.getName().equals("getItemInOffHand")) return new ItemStack(Material.AIR);
                if (m.getName().equals("getArmorContents")) return new ItemStack[4];
                if (m.getName().equals("getContents")) return new ItemStack[36];
                if (m.getReturnType().equals(ItemStack.class)) return new ItemStack(Material.AIR);
                if (m.getReturnType().equals(ItemStack[].class)) return new ItemStack[0];
                if (m.getReturnType().equals(int.class)) return 0;
                return null;
            }
        ); 
    }
    @Override public Inventory getEnderChest() { return null; }
    @Override public MainHand getMainHand() { return MainHand.RIGHT; }
    @Override public InventoryView getOpenInventory() { return null; }
    @Override public InventoryView openInventory(Inventory inventory) { return null; }
    @Override public void openInventory(InventoryView inventory) {}
    @Override public InventoryView openWorkbench(Location location, boolean force) { return null; }
    @Override public InventoryView openEnchanting(Location location, boolean force) { return null; }
    @Override public InventoryView openMerchant(org.bukkit.entity.Villager villager, boolean force) { return null; }
    @Override public InventoryView openMerchant(Merchant merchant, boolean force) { return null; }
    @Override public InventoryView openAnvil(Location location, boolean force) { return null; }
    @Override public InventoryView openCartographyTable(Location location, boolean force) { return null; }
    @Override public InventoryView openGrindstone(Location location, boolean force) { return null; }
    @Override public InventoryView openLoom(Location location, boolean force) { return null; }
    @Override public InventoryView openSmithingTable(Location location, boolean force) { return null; }
    @Override public InventoryView openStonecutter(Location location, boolean force) { return null; }
    @Override public void closeInventory() {}
    @Override public void closeInventory(InventoryCloseEvent.Reason reason) {}
    @Override public boolean discoverRecipe(NamespacedKey recipe) { return false; }
    @Override public int getEnchantmentSeed() { return 0; }
    @Override public void setEnchantmentSeed(int seed) {}
    @Override public void setFoodLevel(int value) {}

    // OfflinePlayer
    @Override public long getFirstPlayed() { return 0; }
    @Override public int getStatistic(Statistic statistic, EntityType entityType) { return 0; }
    @Override public int getStatistic(Statistic statistic, Material material) { return 0; }
    @Override public void incrementStatistic(Statistic statistic) {}
    @Override public void incrementStatistic(Statistic statistic, int amount) {}
    @Override public void incrementStatistic(Statistic statistic, Material material) {}
    @Override public void incrementStatistic(Statistic statistic, Material material, int amount) {}
    @Override public void incrementStatistic(Statistic statistic, EntityType entityType) {}
    @Override public void incrementStatistic(Statistic statistic, EntityType entityType, int amount) {}
    @Override public void decrementStatistic(Statistic statistic) {}
    @Override public void decrementStatistic(Statistic statistic, int amount) {}
    @Override public void decrementStatistic(Statistic statistic, Material material) {}
    @Override public void decrementStatistic(Statistic statistic, Material material, int amount) {}
    @Override public void decrementStatistic(Statistic statistic, EntityType entityType) {}
    @Override public void decrementStatistic(Statistic statistic, EntityType entityType, int amount) {}
    @Override public void setStatistic(Statistic statistic, Material material, int newValue) {}
    @Override public Player getPlayer() { return this; }

    // Damageable
    @Override public void setAbsorptionAmount(double amount) {}
    @Override public void setMaxHealth(double health) { getHandle().getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(health); }
    @Override public void setHealth(double health) { getHandle().setHealth((float) health); }
    @Override public void heal(double amount, EntityRegainHealthEvent.RegainReason reason) { getHandle().heal((float) amount); }
    @Override public double getHealth() { return getHandle().getHealth(); }
    @Override public void damage(double amount) {}
    @Override public void damage(double amount, Entity source) {}
    @Override public void damage(double amount, DamageSource damageSource) {}
    @Override public void resetMaxHealth() { setMaxHealth(20.0); }
    @Override public double getMaxHealth() { return getHandle().getMaxHealth(); }
    @Override public double getAbsorptionAmount() { return 0; }

    // Conversable
    @Override public boolean isConversing() { return false; }
    @Override public void acceptConversationInput(String input) {}

    // NetworkClient
    @Override public int getProtocolVersion() { return 0; }

    // Frictional
    @Override public TriState getFrictionState() { return TriState.NOT_SET; }
    @Override public void setFrictionState(TriState state) {}

    // ConfigurationSerializable
    @Override public Map<String, Object> serialize() { return new HashMap<>(); }

    @Override public boolean isSleeping() { return getHandle().isSleeping(); }
    @Override public void setCollidable(boolean collidable) {}
    @Override public boolean isCollidable() { return true; }
    @Override public void setJumping(boolean jumping) {}
    @Override public boolean isJumping() { return false; }
    @Override public void setSwimming(boolean swimming) {}
    @Override public boolean isSwimming() { return getHandle().isSwimming(); }
    @Override public int getActiveItemRemainingTime() { return 0; }
    @Override public void setNextBeeStingerRemoval(int ticks) {}
    @Override public int getNextBeeStingerRemoval() { return 0; }
    @Override public boolean addPotionEffects(Collection<PotionEffect> effects) { return false; }
    @Override public List<Block> getLineOfSight(Set<Material> transparent, int distance) { return Collections.emptyList(); }
    @Override public boolean hasAI() { return false; }
    @Override public void setAI(boolean ai) {}
    @Override public void playPickupItemAnimation(Item item, int quantity) {}
    @Override public ItemStack getItemInUse() { return CraftItemStack.asBukkitCopy(getHandle().getUseItem()); }
    @Override public ItemStack getActiveItem() { return CraftItemStack.asBukkitCopy(getHandle().getUseItem()); }
    @Override public void clearActiveItem() {}
    @Override public void playHurtAnimation(float yaw) {}
    @Override public void setRemoveWhenFarAway(boolean remove) {}
    @Override public boolean getRemoveWhenFarAway() { return false; }
    @Override public int getShieldBlockingDelay() { return 0; }
    @Override public void setBeeStingerCooldown(int ticks) {}
    @Override public int getBeeStingerCooldown() { return 0; }
    @Override public @Nullable com.destroystokyo.paper.block.TargetBlockInfo getTargetBlockInfo(int distance, com.destroystokyo.paper.block.TargetBlockInfo.FluidMode fluidMode) { return (com.destroystokyo.paper.block.TargetBlockInfo) java.lang.reflect.Proxy.newProxyInstance(com.destroystokyo.paper.block.TargetBlockInfo.class.getClassLoader(), new Class<?>[] { com.destroystokyo.paper.block.TargetBlockInfo.class }, (p, m, a) -> null); }
    @Override public @Nullable com.destroystokyo.paper.block.TargetBlockInfo getTargetBlockInfo(int distance) { return getTargetBlockInfo(distance, com.destroystokyo.paper.block.TargetBlockInfo.FluidMode.NEVER); }
    @Override public @Nullable com.destroystokyo.paper.entity.TargetEntityInfo getTargetEntityInfo(int distance, boolean nonTransparent) { return (com.destroystokyo.paper.entity.TargetEntityInfo) java.lang.reflect.Proxy.newProxyInstance(com.destroystokyo.paper.entity.TargetEntityInfo.class.getClassLoader(), new Class<?>[] { com.destroystokyo.paper.entity.TargetEntityInfo.class }, (p, m, a) -> null); }
    @Override public int getMaximumNoDamageTicks() { return 0; }
    @Override public void setMaximumNoDamageTicks(int ticks) {}
    @Override public int getActiveItemUsedTime() { return 0; }
    @Override public boolean isLeashed() { return false; }
    @Override public void setRiptiding(boolean riptiding) {}
    @Override public RayTraceResult rayTraceBlocks(double distance) { return null; }
    @Override public RayTraceResult rayTraceBlocks(double distance, FluidCollisionMode fluidMode) { return null; }
    @Override public Location getEyeLocation() {
        Location loc = getLocation();
        loc.setY(loc.getY() + getEyeHeight());
        return loc;
    }
    @Override public void swingMainHand() {}
    @Override public void swingOffHand() {}
    @Override public boolean setLeashHolder(Entity holder) { return false; }
    @Override public void setArrowsStuck(int arrows) {}
    @Override public int getArrowsStuck() { return 0; }
    @Override public boolean clearActivePotionEffects() { return false; }
    @Override public void setActiveItemRemainingTime(int ticks) {}
    @Override public void setLastDamage(double damage) {}
    @Override public double getLastDamage() { return 0; }
    @Override public void removePotionEffect(PotionEffectType type) {}
    @Override public boolean hasActiveItem() { return false; }
    @Override public int getMaximumAir() { return 300; }
    @Override public int getRemainingAir() { return getHandle().getAirSupply(); }
    @Override public void setMaximumAir(int air) {}
    @Override public int getNextArrowRemoval() { return 0; }
    @Override public void startUsingItem(EquipmentSlot slot) {}
    @Override public int getArrowCooldown() { return 0; }
    @Override public int getItemInUseTicks() { return 0; }
    @Override public void setItemInUseTicks(int ticks) {}
    @Override public void setBeeStingersInBody(int count) {}
    @Override public int getBeeStingersInBody() { return 0; }
    @Override public void setCanPickupItems(boolean pickup) {}
    @Override public boolean getCanPickupItems() { return true; }
    @Override public org.bukkit.Sound getEatingSound(ItemStack itemStack) { return org.bukkit.Sound.ENTITY_GENERIC_EAT; }
    @Override public EquipmentSlot getActiveItemHand() { return EquipmentSlot.HAND; }
    @Override public boolean addPotionEffect(PotionEffect effect) { return false; }
    @Override public boolean addPotionEffect(PotionEffect effect, boolean force) { return false; }
    @Override public PotionEffect getPotionEffect(PotionEffectType type) { return null; }

    // HumanEntity extra
    @Override public int getFoodLevel() { return getHandle().getFoodData().getFoodLevel(); }

    // OfflinePlayer extra
    @Override public long getLastLogin() { return 0; }
    @Override public long getLastSeen() { return 0; }
    @Override public boolean isConnected() { return getHandle().connection != null; }
    @Override public long getLastPlayed() { return 0; }
    @Override public boolean hasPlayedBefore() { return true; }
    @Override public Location getLastDeathLocation() { return null; }
    @Override public <E extends org.bukkit.BanEntry<? super PlayerProfile>> E ban(String reason, Duration duration, String source) { return null; }
    @Override public <E extends org.bukkit.BanEntry<? super PlayerProfile>> E ban(String reason, Date expires, String source) { return null; }
    @Override public <E extends org.bukkit.BanEntry<? super PlayerProfile>> E ban(String reason, Instant expires, String source) { return null; }
    @Override public void setStatistic(Statistic statistic, EntityType entityType, int newValue) {}

    // Damageable extra

    // ProjectileSource extra
    @Override public <T extends Projectile> T launchProjectile(Class<? extends T> projectile) { return proxy.launchProjectile(projectile); }
    @Override public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity) { return proxy.launchProjectile(projectile, velocity); }
    @Override public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity, Consumer<? super T> function) { return proxy.launchProjectile(projectile, velocity, function); }

    // PluginMessageRecipient
    @Override public Set<String> getListeningPluginChannels() { return Collections.emptySet(); }

    // Attributable extra
    @Override public void registerAttribute(Attribute attribute) {}
    @Override public AttributeInstance getAttribute(Attribute attribute) { return null; }

    // Conversable extra
    @Override public void sendRawMessage(UUID sender, String message) {}
    @Override public InetSocketAddress getVirtualHost() { return getAddress(); }
    @Override public boolean beginConversation(Conversation conversation) { return false; }
    @Override public void abandonConversation(Conversation conversation) {}
    @Override public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {}

    @Override public int getArrowsInBody() { return 0; }
    @Override public int getNoActionTicks() { return 0; }
}
