package org.bukkit.craftbukkit.v1_21_R1;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.*;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import io.ampznetwork.lunararc.common.server.LunarArcLogger;
import io.papermc.paper.configuration.FeatureFlagConfig;

public class CraftServer implements Server {
    private final MinecraftServer console;
    private final PlayerList playerList;
    private final Logger logger = LunarArcLogger.getLogger("Minecraft");
    private final SimpleCommandMap commandMap = new io.ampznetwork.lunararc.common.server.LunarArcCommandMap(this);
    private final PluginManager pluginManager;
    private final SimplePluginManager simplePluginManager;
    private final ServicesManager servicesManager = new SimpleServicesManager();
    private final UnsafeValues unsafeValues;
    private final Map<UUID, Player> playerCache = new HashMap<>();
    private final org.bukkit.metadata.MetadataStore<org.bukkit.entity.Entity> entityMetadata = (org.bukkit.metadata.MetadataStore<org.bukkit.entity.Entity>) java.lang.reflect.Proxy.newProxyInstance(
        org.bukkit.metadata.MetadataStore.class.getClassLoader(),
        new Class<?>[] { org.bukkit.metadata.MetadataStore.class },
        new java.lang.reflect.InvocationHandler() {
            private final Map<String, Map<org.bukkit.entity.Entity, List<org.bukkit.metadata.MetadataValue>>> metadata = new java.util.concurrent.ConcurrentHashMap<>();
            @Override
            public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if (name.equals("setMetadata")) {
                    org.bukkit.entity.Entity subject = (org.bukkit.entity.Entity) args[0];
                    String key = (String) args[1];
                    org.bukkit.metadata.MetadataValue value = (org.bukkit.metadata.MetadataValue) args[2];
                    metadata.computeIfAbsent(key, k -> new java.util.concurrent.ConcurrentHashMap<>())
                            .computeIfAbsent(subject, s -> new java.util.concurrent.CopyOnWriteArrayList<>())
                            .add(value);
                    return null;
                } else if (name.equals("getMetadata")) {
                    org.bukkit.entity.Entity subject = (org.bukkit.entity.Entity) args[0];
                    String key = (String) args[1];
                    Map<org.bukkit.entity.Entity, List<org.bukkit.metadata.MetadataValue>> keyMap = metadata.get(key);
                    return keyMap != null ? keyMap.getOrDefault(subject, Collections.emptyList()) : Collections.emptyList();
                } else if (name.equals("hasMetadata")) {
                    org.bukkit.entity.Entity subject = (org.bukkit.entity.Entity) args[0];
                    String key = (String) args[1];
                    Map<org.bukkit.entity.Entity, List<org.bukkit.metadata.MetadataValue>> keyMap = metadata.get(key);
                    return keyMap != null && keyMap.containsKey(subject);
                } else if (name.equals("removeMetadata")) {
                    org.bukkit.entity.Entity subject = (org.bukkit.entity.Entity) args[0];
                    String key = (String) args[1];
                    Map<org.bukkit.entity.Entity, List<org.bukkit.metadata.MetadataValue>> keyMap = metadata.get(key);
                    if (keyMap != null) keyMap.remove(subject);
                    return null;
                }
                return null;
            }
        }
    );

    public org.bukkit.metadata.MetadataStore<org.bukkit.entity.Entity> getEntityMetadata() {
        return entityMetadata;
    }

    private final YamlConfiguration bukkitConfig = new YamlConfiguration();
    private final YamlConfiguration spigotConfig = new YamlConfiguration();
    private final YamlConfiguration commandsConfig = new YamlConfiguration();
    private final YamlConfiguration paperGlobalConfig = new YamlConfiguration();
    private final YamlConfiguration paperWorldConfig = new YamlConfiguration();

    private final Spigot spigot = new Server.Spigot() {
        @Override
        public YamlConfiguration getConfig() {
            return spigotConfig;
        }
    };
    private final StandardMessenger messenger = new StandardMessenger();
    private final org.bukkit.craftbukkit.v1_21_R1.scheduler.CraftScheduler scheduler = new org.bukkit.craftbukkit.v1_21_R1.scheduler.CraftScheduler();
    private final ItemFactory itemFactory;
    private final HelpMap helpMap = new HelpMap() {
        @Override
        public @Nullable org.bukkit.help.HelpTopic getHelpTopic(@NotNull String cmdName) {
            return null;
        }

        @Override
        public @NotNull Collection<org.bukkit.help.HelpTopic> getHelpTopics() {
            return Collections.emptyList();
        }

        @Override
        public void addTopic(@NotNull org.bukkit.help.HelpTopic topic) {
        }

        @Override
        public void clear() {
        }

        @Override
        public void registerHelpTopicFactory(@NotNull Class<?> commandClass,
                @NotNull org.bukkit.help.HelpTopicFactory<?> factory) {
        }

        @Override
        public @NotNull List<String> getIgnoredPlugins() {
            return Collections.emptyList();
        }
    };

    private final CraftConsoleCommandSender consoleSender;

    public CraftServer(MinecraftServer console, PlayerList playerList) {
        this.console = console;
        this.playerList = playerList;
        this.consoleSender = new CraftConsoleCommandSender(console);
        
        logger.info("[LunarArc] CraftServer initialized: " + getName() + " version " + getVersion() + " (Bukkit: " + getBukkitVersion() + ")");
        
        this.simplePluginManager = new SimplePluginManager(this, commandMap);
        this.pluginManager = new io.papermc.paper.plugin.manager.PaperPluginManagerImpl(this, commandMap, simplePluginManager);
        this.simplePluginManager.setInternalManager(this.pluginManager);

        this.simplePluginManager.registerInterface(io.ampznetwork.lunararc.common.server.LunarArcPluginLoader.class);

        this.unsafeValues = (UnsafeValues) java.lang.reflect.Proxy.newProxyInstance(
                UnsafeValues.class.getClassLoader(),
                new Class<?>[] { UnsafeValues.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("getVersionFetcher")) {
                        return new com.destroystokyo.paper.util.VersionFetcher() {
                            @Override
                            public long getCacheTime() {
                                return 0;
                            }

                            @Override
                            public @NotNull net.kyori.adventure.text.Component getVersionMessage(
                                    @NotNull String serverVersion) {
                                return net.kyori.adventure.text.Component.text(
                                        io.ampznetwork.lunararc.common.server.LunarArcVersionInfo.projectName()
                                                + " " + serverVersion);
                            }
                        };
                    }
                    if (method.getName().equals("getDataVersion"))
                        return io.ampznetwork.lunararc.common.server.LunarArcVersionInfo.dataVersion().orElse(0);
                    if (method.getName().equals("processClass"))
                        return args[2]; // Return class bytes unmodified
                    if (method.getName().equals("fromLegacy")) {
                        if (args != null && args.length > 0 && args[0] instanceof Material leg) {
                            if (leg == Material.AIR)
                                return Material.AIR;
                            try {
                                String name = leg.name().replace("LEGACY_", "");
                                return Material.valueOf(name);
                            } catch (Exception e) {
                                return Material.STONE;
                            }
                        }
                    }
                    if (method.getName().equals("getMaterial")) {
                        if (args != null && args.length > 0 && args[0] instanceof String name) {
                            try {
                                return Material.valueOf(name);
                            } catch (Exception e) {
                                return null;
                            }
                        }
                    }
                    if (method.getReturnType().equals(boolean.class))
                        return false;
                    if (method.getReturnType().equals(int.class))
                        return 0;
                    if (method.getReturnType().equals(byte[].class))
                        return new byte[0];
                    if (method.getReturnType().equals(Material.class))
                        return Material.STONE;
                    return null;
                });

        // Scheduler initialized inline

        this.itemFactory = (ItemFactory) java.lang.reflect.Proxy.newProxyInstance(
                ItemFactory.class.getClassLoader(),
                new Class<?>[] { ItemFactory.class },
                (proxy, method, args) -> {
                    if (method.getReturnType().equals(boolean.class))
                        return false;
                    if (method.getName().equals("getItemMeta") && args != null && args.length > 0) {
                        // Return a safe dummy ItemMeta proxy to prevent NPEs
                        return java.lang.reflect.Proxy.newProxyInstance(
                                org.bukkit.inventory.meta.ItemMeta.class.getClassLoader(),
                                new Class<?>[] { org.bukkit.inventory.meta.ItemMeta.class }, (p, m, a) -> {
                                    if (m.getReturnType().equals(boolean.class))
                                        return false;
                                    if (m.getReturnType().equals(int.class))
                                        return 0;
                                    if (m.getReturnType().equals(List.class))
                                        return Collections.emptyList();
                                    if (m.getReturnType().equals(Map.class))
                                        return Collections.emptyMap();
                                    if (m.getReturnType().equals(Set.class))
                                        return Collections.emptySet();
                                    return null;
                                });
                    }
                    return null;
                });

        this.loadConfigurations();

        Bukkit.setServer(this);

        commandMap.register("bukkit", new org.bukkit.command.defaults.VersionCommand("version"));
        commandMap.register("bukkit", new org.bukkit.command.defaults.PluginsCommand("plugins"));
        commandMap.register("bukkit", new org.bukkit.command.defaults.ReloadCommand("reload"));
    }

    private void loadConfigurations() {
        File bukkitFile = new File("bukkit.yml");
        File spigotFile = new File("spigot.yml");
        File commandsFile = new File("commands.yml");
        File paperDir = new File("config");
        if (!paperDir.exists())
            paperDir.mkdirs();
        File paperGlobalFile = new File(paperDir, "paper-global.yml");
        File paperWorldFile = new File(paperDir, "paper-world-defaults.yml");

        try {
            if (bukkitFile.exists())
                bukkitConfig.load(bukkitFile);
            if (spigotFile.exists())
                spigotConfig.load(spigotFile);
            if (commandsFile.exists())
                commandsConfig.load(commandsFile);
            if (paperGlobalFile.exists())
                paperGlobalConfig.load(paperGlobalFile);
            if (paperWorldFile.exists())
                paperWorldConfig.load(paperWorldFile);

            // Bukkit Defaults
            bukkitConfig.set("settings.allow-end", true);
            bukkitConfig.set("settings.warn-on-overload", true);
            bukkitConfig.set("settings.permissions-file", "permissions.yml");
            bukkitConfig.set("settings.update-checker", true);
            bukkitConfig.set("settings.plugin-profiling", false);
            bukkitConfig.set("settings.connection-throttle", 4000);
            bukkitConfig.set("settings.query-plugins", true);
            bukkitConfig.set("settings.deprecated-verbose", "default");
            bukkitConfig.set("settings.shutdown-message", "Server closed");
            bukkitConfig.set("spawn-limits.monsters", 70);
            bukkitConfig.set("spawn-limits.animals", 10);
            bukkitConfig.set("spawn-limits.water-animals", 5);
            bukkitConfig.set("spawn-limits.ambient", 15);
            bukkitConfig.set("chunk-gc.period-in-ticks", 600);
            bukkitConfig.set("ticks-per.animal-spawns", 400);
            bukkitConfig.set("ticks-per.monster-spawns", 1);
            bukkitConfig.set("aliases", "noworld");
            bukkitConfig.save(bukkitFile);

            // Spigot Defaults
            spigotConfig.set("settings.debug", false);
            spigotConfig.set("settings.bungeecord", false);
            spigotConfig.set("settings.sample-count", 12);
            spigotConfig.set("settings.player-shuffle", 0);
            spigotConfig.set("settings.user-cache-size", 1000);
            spigotConfig.set("settings.save-user-cache-on-stop-only", false);
            spigotConfig.set("settings.moved-wrongly-threshold", 0.0625);
            spigotConfig.set("settings.moved-too-quickly-multiplier", 10.0);
            spigotConfig.set("settings.timeout-time", 60);
            spigotConfig.set("settings.restart-on-crash", true);
            spigotConfig.set("settings.restart-script", "./start.sh");
            spigotConfig.set("settings.netty-threads", 4);
            spigotConfig.set("settings.attribute.maxHealth.max", 2048.0);
            spigotConfig.set("settings.attribute.movementSpeed.max", 2048.0);
            spigotConfig.set("settings.attribute.attackDamage.max", 2048.0);
            spigotConfig.set("settings.log-villager-deaths", true);
            spigotConfig.set("settings.log-named-deaths", true);
            spigotConfig.set("messages.whitelist", "You are not whitelisted on this server!");
            spigotConfig.set("messages.unknown-command", "Unknown command. Type \"/help\" for help.");
            spigotConfig.set("messages.server-full", "The server is full!");
            spigotConfig.set("messages.outdated-client", "Outdated client! Please use {0}");
            spigotConfig.set("messages.outdated-server", "Outdated server! I'm still on {0}");
            spigotConfig.set("messages.restart", "Server is restarting");
            spigotConfig.set("advancements.disable-saving", false);
            spigotConfig.set("advancements.disabled", Collections.singletonList("minecraft:story/disabled"));
            spigotConfig.save(spigotFile);

            // Commands Defaults
            commandsConfig.set("command-block-overrides", Collections.emptyList());
            commandsConfig.set("aliases.icanhasbukkit", Collections.singletonList("version"));
            commandsConfig.save(commandsFile);

            // Paper Global Defaults
            paperGlobalConfig.set("proxies.bungee-cord.enabled", false);
            paperGlobalConfig.set("proxies.velocity.enabled", false);
            paperGlobalConfig.set("proxies.velocity.online-mode", false);
            paperGlobalConfig.set("proxies.velocity.secret", "");
            paperGlobalConfig.set("settings.chunk-loading.min-loadable-tick-rate", 1);
            paperGlobalConfig.set("settings.incoming-packet-spam-threshold", 300);
            paperGlobalConfig.save(paperGlobalFile);

            // Paper World Defaults
            paperWorldConfig.set("anticheat.obfuscation.items.enabled", false);
            paperWorldConfig.set("entities.spawning.despawn-ranges.ambient.hard", 128);
            paperWorldConfig.set("entities.spawning.despawn-ranges.ambient.soft", 32);
            paperWorldConfig.save(paperWorldFile);

        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Failed to load/generate server configuration files", e);
        }
    }

    public MinecraftServer getServer() {
        return console;
    }

    public MinecraftServer getHandle() {
        return console;
    }

    @Override
    public @NotNull String getName() {
        return "Paper";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.21.1-R0.1-SNAPSHOT (LunarArc)";
    }

    @Override
    public @NotNull String getMinecraftVersion() {
        return io.ampznetwork.lunararc.common.server.LunarArcVersionInfo.minecraftVersion();
    }

    @Override
    public @NotNull String getBukkitVersion() {
        return io.ampznetwork.lunararc.common.server.LunarArcVersionInfo.paperApiVersion();
    }

    @Override
    public @NotNull Logger getLogger() {
        return logger;
    }

    @Override
    public @NotNull PluginManager getPluginManager() {
        return simplePluginManager;
    }

    public Plugin[] getPlugins() {
        return pluginManager.getPlugins();
    }

    @Override
    public @NotNull ServicesManager getServicesManager() {
        return servicesManager;
    }

    private final Map<UUID, World> worldCache = new HashMap<>();

    @Override
    public @NotNull Collection<? extends Player> getOnlinePlayers() {
        List<Player> players = new ArrayList<>();
        for (net.minecraft.server.level.ServerPlayer player : playerList.getPlayers()) {
            Player cp = getPlayer(player.getUUID());
            if (cp != null)
                players.add(cp);
        }
        return players;
    }

    @Override
    public @NotNull List<World> getWorlds() {
        List<World> worlds = new ArrayList<>();
        for (net.minecraft.server.level.ServerLevel level : console.getAllLevels()) {
            World world = getWorld(level.dimension().location().toString());
            if (world != null)
                worlds.add(world);
        }
        return worlds;
    }

    @Override
    public @NotNull ConsoleCommandSender getConsoleSender() {
        return consoleSender;
    }

    @Override
    public @NotNull org.bukkit.command.CommandMap getCommandMap() {
        return commandMap;
    }

    @SuppressWarnings("unchecked")
    private <T extends Keyed> Registry<T> createDummyRegistry(Class<T> type) {
        final Class<T> finalType = type == null ? (Class<T>) Keyed.class : type;
        return (Registry<T>) java.lang.reflect.Proxy.newProxyInstance(
                Registry.class.getClassLoader(),
                new Class<?>[] { Registry.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("get") && args != null && args.length > 0
                            && args[0] instanceof NamespacedKey key) {
                        if (finalType == Material.class) {
                            Material mat = Material.matchMaterial(key.getKey());
                            if (mat != null && mat.isLegacy()) {
                                Material modern = Material.getMaterial(mat.name());
                                if (modern != null && !modern.isLegacy()) return modern;
                            }
                            return mat;
                        }
                        String name = key.getKey().toUpperCase(java.util.Locale.ROOT);
                        if (finalType.isEnum()) {
                            for (T constant : finalType.getEnumConstants()) {
                                if (((Enum<?>) constant).name().equals(name))
                                    return constant;
                            }
                        }
                        try {
                            java.lang.reflect.Field field = finalType.getDeclaredField(name);
                            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                                return field.get(null);
                            }
                        } catch (Exception ignored) {
                        }
                        return null;
                    }
                    if (method.getName().equals("iterator")) {
                        List<T> values = new ArrayList<>();
                        if (finalType.isEnum()) {
                            for (T constant : finalType.getEnumConstants()) {
                                try {
                                    if (!(Boolean) constant.getClass().getMethod("isLegacy").invoke(constant))
                                        values.add(constant);
                                } catch (Exception e) {
                                    values.add(constant);
                                }
                            }
                        } else {
                            for (java.lang.reflect.Field field : finalType.getDeclaredFields()) {
                                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                                        && finalType.isAssignableFrom(field.getType())) {
                                    try {
                                        T value = (T) field.get(null);
                                        if (value != null)
                                            values.add(value);
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }
                        return values.iterator();
                    }
                    return null;
                });
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Registry<?> createDummyRegistryUnchecked(Class<?> type) {
        if (type == null || !Keyed.class.isAssignableFrom(type)) {
            return createDummyRegistry(Keyed.class);
        }
        return createDummyRegistry((Class) type);
    }

    public void loadPlugins() {
        logger.info("[LunarArc] Scanning plugins folder...");
        File pluginsFolder = new File("plugins");
        if (!pluginsFolder.exists()) {
            pluginsFolder.mkdirs();
        }

        File[] files = pluginsFolder.listFiles(f -> f.getName().endsWith(".jar"));
        if (files != null) {
            logger.info("[LunarArc] Found " + files.length + " potential plugins. Loading...");
            simplePluginManager.loadPlugins(files);
        }
    }

    public void enablePlugins(org.bukkit.plugin.PluginLoadOrder type) {
        logger.info("[LunarArc] Enabling Bukkit plugins (Order: " + type + ")...");
        simplePluginManager.enablePlugins(type);
        if (type == org.bukkit.plugin.PluginLoadOrder.POSTWORLD || type == null) {
            syncCommands();
        }
    }

    public void syncCommands() {
        com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher = console.getCommands().getDispatcher();
        for (org.bukkit.command.Command command : commandMap.getKnownCommands().values()) {
            try {
                new io.ampznetwork.lunararc.common.server.BukkitCommandWrapper(command).register(dispatcher);
            } catch (Exception e) {
                logger.log(java.util.logging.Level.SEVERE, "Failed to register command " + command.getName(), e);
            }
        }
        for (net.minecraft.server.level.ServerPlayer player : console.getPlayerList().getPlayers()) {
            console.getCommands().sendCommands(player);
        }
    }

    @Override
    public int getMaxPlayers() {
        return playerList.getMaxPlayers();
    }

    @Override
    public void setMaxPlayers(int maxPlayers) {
    }

    @Override
    public int getPort() {
        try {
            Object value = console.getClass().getMethod("getPort").invoke(console);
            if (value instanceof Integer port) {
                return port;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return -1;
    }

    @Override
    public long getConnectionThrottle() {
        return 4000;
    }

    @Override
    public int getViewDistance() {
        return 10;
    }

    @Override
    public int getSimulationDistance() {
        return 10;
    }

    @Override
    public @NotNull String getIp() {
        return "";
    }

    @Override
    public @NotNull String getWorldType() {
        return "DEFAULT";
    }

    @Override
    public boolean getGenerateStructures() {
        return true;
    }

    @Override
    public int getSpawnRadius() {
        return 10;
    }

    @Override
    public void setSpawnRadius(int value) {
    }

    @Override
    public boolean isHardcore() {
        return false;
    }

    @Override
    public boolean getAllowFlight() {
        return true;
    }

    @Override
    public boolean getOnlineMode() {
        return console.usesAuthentication();
    }

    @Override
    public boolean getHideOnlinePlayers() {
        return false;
    }

    @Override
    public boolean getAllowNether() {
        return true;
    }

    @Override
    public boolean getAllowEnd() {
        return true;
    }

    @Override
    public boolean hasWhitelist() {
        return playerList.isUsingWhitelist();
    }

    @Override
    public void setWhitelist(boolean value) {
        playerList.setUsingWhiteList(value);
    }

    @Override
    public boolean isWhitelistEnforced() {
        return false;
    }

    @Override
    public void setWhitelistEnforced(boolean value) {
    }

    @Override
    public @NotNull Set<OfflinePlayer> getWhitelistedPlayers() {
        return Collections.emptySet();
    }

    @Override
    public void reloadWhitelist() {
        playerList.reloadWhiteList();
    }

    @Override
    public int broadcastMessage(@NotNull String message) {
        return 0;
    }

    @Override
    public int broadcast(@NotNull String message, @NotNull String permission) {
        return 0;
    }

    @Override
    public int broadcast(@NotNull net.kyori.adventure.text.Component message, @NotNull String permission) {
        return 0;
    }

    @Override
    public int broadcast(@NotNull net.kyori.adventure.text.Component message) {
        return 0;
    }

    @Override
    public @Nullable Player getPlayer(@NotNull String name) {
        net.minecraft.server.level.ServerPlayer player = playerList.getPlayerByName(name);
        return player != null ? getPlayer(player.getUUID()) : null;
    }

    @Override
    public @Nullable Player getPlayerExact(@NotNull String name) {
        return getPlayer(name);
    }

    @Override
    public @NotNull List<Player> matchPlayer(@NotNull String name) {
        Player player = getPlayer(name);
        if (player != null)
            return Collections.singletonList(player);
        return Collections.emptyList();
    }

    @Override
    public @Nullable Player getPlayer(@NotNull UUID id) {
        net.minecraft.server.level.ServerPlayer player = playerList.getPlayer(id);
        if (player == null) {
            playerCache.remove(id);
            return null;
        }
        return playerCache.computeIfAbsent(id, k -> {
            try {
                return new CraftPlayer(this, player);
            } catch (Exception e) {
                logger.log(java.util.logging.Level.SEVERE, "Failed to initialize CraftPlayer for "
                        + player.getName().getString() + " (Is CraftPlayerMixin registered?)", e);
                return null;
            }
        });
    }

    @Override
    public @Nullable World getWorld(@NotNull String name) {
        net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.tryParse(name);
        if (rl == null)
            return null;

        net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> key = net.minecraft.resources.ResourceKey
                .create(net.minecraft.core.registries.Registries.DIMENSION, rl);
        net.minecraft.server.level.ServerLevel level = console.getLevel(key);
        if (level == null)
            return null;

        UUID uid = UUID.nameUUIDFromBytes(name.getBytes());
        return worldCache.computeIfAbsent(uid, k -> new CraftWorld(level));
    }

    @Override
    public @Nullable World getWorld(@NotNull UUID uid) {
        for (World world : getWorlds()) {
            if (world.getUID().equals(uid)) {
                return world;
            }
        }
        return null;
    }

    @Override
    public @Nullable World getWorld(@NotNull net.kyori.adventure.key.Key key) {
        return null;
    }

    @Override
    public @NotNull MapView createMap(@NotNull World world) {
        return null;
    }

    @Override
    public @Nullable MapView getMap(int id) {
        return null;
    }

    @Override
    public void reload() {
    }

    @Override
    public void reloadData() {
    }

    @Override
    public void savePlayers() {
        playerList.saveAll();
    }

    @Override
    public void shutdown() {
        console.halt(false);
    }

    @Override
    public boolean isPrimaryThread() {
        return console.isSameThread();
    }

    @Override
    public @NotNull String getMotd() {
        return console.getMotd();
    }

    @Override
    public @Nullable String getShutdownMessage() {
        return "Server closed";
    }

    @Override
    public @NotNull Spigot spigot() {
        return spigot;
    }

    @Override
    public @Nullable BukkitScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public @NotNull Messenger getMessenger() {
        return messenger;
    }

    @Override
    public void sendPluginMessage(@NotNull org.bukkit.plugin.Plugin source, @NotNull String channel, byte[] message) {
    }

    @Override
    public @NotNull Set<String> getListeningPluginChannels() {
        return Collections.emptySet();
    }

    @Override
    public @NotNull HelpMap getHelpMap() {
        return helpMap;
    }

    @Override
    public @NotNull Inventory createInventory(@Nullable InventoryHolder owner, @NotNull InventoryType type) {
        return null;
    }

    @Override
    public @NotNull Inventory createInventory(@Nullable InventoryHolder owner, @NotNull InventoryType type,
            @NotNull String title) {
        return null;
    }

    @Override
    public @NotNull Inventory createInventory(@Nullable InventoryHolder owner, @NotNull InventoryType type,
            @NotNull net.kyori.adventure.text.Component title) {
        return null;
    }

    @Override
    public @NotNull Inventory createInventory(@Nullable InventoryHolder owner, int size)
            throws IllegalArgumentException {
        return null;
    }

    @Override
    public @NotNull Inventory createInventory(@Nullable InventoryHolder owner, int size, @NotNull String title)
            throws IllegalArgumentException {
        return null;
    }

    @Override
    public @NotNull Inventory createInventory(@Nullable InventoryHolder owner, int size,
            @NotNull net.kyori.adventure.text.Component title) throws IllegalArgumentException {
        return null;
    }

    @Override
    public @NotNull Merchant createMerchant(@Nullable String title) {
        return null;
    }

    @Override
    public @NotNull Merchant createMerchant(@NotNull net.kyori.adventure.text.Component title) {
        return null;
    }

    @Override
    public int getMonsterSpawnLimit() {
        return 70;
    }

    @Override
    public int getAnimalSpawnLimit() {
        return 10;
    }

    @Override
    public int getWaterAnimalSpawnLimit() {
        return 5;
    }

    @Override
    public int getWaterAmbientSpawnLimit() {
        return 20;
    }

    @Override
    public int getWaterUndergroundCreatureSpawnLimit() {
        return 5;
    }

    @Override
    public int getAmbientSpawnLimit() {
        return 15;
    }

    @Override
    public @NotNull File getWorldContainer() {
        return new File(".");
    }

    @Override
    public @NotNull OfflinePlayer[] getOfflinePlayers() {
        return new OfflinePlayer[0];
    }

    @Override
    public @NotNull OfflinePlayer getOfflinePlayer(@NotNull String name) {
        return null;
    }

    @Override
    public @NotNull OfflinePlayer getOfflinePlayer(@NotNull UUID id) {
        return null;
    }

    @Override
    public @Nullable OfflinePlayer getOfflinePlayerIfCached(@NotNull String name) {
        return null;
    }

    @Override
    public @NotNull Set<String> getIPBans() {
        return Collections.emptySet();
    }

    @Override
    public void banIP(@NotNull String address) {
    }

    @Override
    public void unbanIP(@NotNull String address) {
    }

    @Override
    public void banIP(@NotNull java.net.InetAddress address) {
    }

    @Override
    public void unbanIP(@NotNull java.net.InetAddress address) {
    }

    @Override
    public @NotNull Set<OfflinePlayer> getBannedPlayers() {
        return Collections.emptySet();
    }

    @Override
    public @NotNull BanList getBanList(@NotNull BanList.Type type) {
        return null;
    }

    @Override
    public @NotNull <B extends BanList<E>, E> B getBanList(@NotNull io.papermc.paper.ban.BanListType<B> type) {
        return null;
    }

    @Override
    public @NotNull Set<OfflinePlayer> getOperators() {
        return Collections.emptySet();
    }

    @Override
    public @NotNull Warning.WarningState getWarningState() {
        return Warning.WarningState.DEFAULT;
    }

    @Override
    public @Nullable CachedServerIcon getServerIcon() {
        return null;
    }

    @Override
    public @NotNull CachedServerIcon loadServerIcon(@NotNull File file) throws Exception {
        return null;
    }

    @Override
    public @NotNull CachedServerIcon loadServerIcon(@NotNull BufferedImage image) throws Exception {
        return null;
    }

    @Override
    public void setIdleTimeout(int threshold) {
    }

    @Override
    public int getIdleTimeout() {
        return 0;
    }

    @Override
    public @NotNull UnsafeValues getUnsafe() {
        return unsafeValues;
    }

    @Override
    public @NotNull StructureManager getStructureManager() {
        return null;
    }

    @Override
    public @NotNull io.papermc.paper.threadedregions.scheduler.AsyncScheduler getAsyncScheduler() {
        return (io.papermc.paper.threadedregions.scheduler.AsyncScheduler) java.lang.reflect.Proxy.newProxyInstance(
                io.papermc.paper.threadedregions.scheduler.AsyncScheduler.class.getClassLoader(),
                new Class<?>[] { io.papermc.paper.threadedregions.scheduler.AsyncScheduler.class },
                (proxy, method, args) -> {
                    if (method.getName().startsWith("run") || method.getName().startsWith("create")) {
                        for (Object arg : args) {
                            if (arg instanceof Runnable r) {
                                new Thread(r).start();
                                break;
                            }
                            if (arg instanceof java.util.function.Consumer c) {
                                new Thread(() -> c.accept(null)).start();
                                break;
                            }
                        }
                    }
                    return null;
                });
    }

    @Override
    public @NotNull io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler getGlobalRegionScheduler() {
        return (io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler) java.lang.reflect.Proxy.newProxyInstance(
                io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler.class.getClassLoader(),
                new Class<?>[] { io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler.class },
                (proxy, method, args) -> {
                    if (method.getName().startsWith("run") || method.getName().startsWith("execute")) {
                        for (Object arg : args) {
                            if (arg instanceof Runnable r) {
                                ((io.ampznetwork.lunararc.common.bridge.MinecraftServerBridge) console)
                                        .lunararc$queueTask(r);
                                break;
                            }
                            if (arg instanceof java.util.function.Consumer c) {
                                ((io.ampznetwork.lunararc.common.bridge.MinecraftServerBridge) console)
                                        .lunararc$queueTask(() -> c.accept(null));
                                break;
                            }
                        }
                    }
                    return null;
                });
    }

    @Override
    public @NotNull io.papermc.paper.threadedregions.scheduler.RegionScheduler getRegionScheduler() {
        return (io.papermc.paper.threadedregions.scheduler.RegionScheduler) java.lang.reflect.Proxy.newProxyInstance(
                io.papermc.paper.threadedregions.scheduler.RegionScheduler.class.getClassLoader(),
                new Class<?>[] { io.papermc.paper.threadedregions.scheduler.RegionScheduler.class },
                (proxy, method, args) -> {
                    if (method.getName().startsWith("run") || method.getName().startsWith("execute")) {
                        for (Object arg : args) {
                            if (arg instanceof Runnable r) {
                                ((io.ampznetwork.lunararc.common.bridge.MinecraftServerBridge) console)
                                        .lunararc$queueTask(r);
                                break;
                            }
                            if (arg instanceof java.util.function.Consumer c) {
                                ((io.ampznetwork.lunararc.common.bridge.MinecraftServerBridge) console)
                                        .lunararc$queueTask(() -> c.accept(null));
                                break;
                            }
                        }
                    }
                    return null;
                });
    }

    @Override
    public @NotNull org.bukkit.entity.EntityFactory getEntityFactory() {
        return null;
    }

    @Override
    public @NotNull org.bukkit.inventory.ItemFactory getItemFactory() {
        return itemFactory;
    }

    @Override
    public @NotNull org.bukkit.potion.PotionBrewer getPotionBrewer() {
        return null;
    }

    @Override
    public @NotNull com.destroystokyo.paper.entity.ai.MobGoals getMobGoals() {
        return null;
    }

    @Override
    public @NotNull org.bukkit.ServerLinks getServerLinks() {
        return null;
    }

    @Override
    public @Nullable org.bukkit.packs.DataPackManager getDataPackManager() {
        return null;
    }

    @Override
    public @NotNull io.papermc.paper.datapack.DatapackManager getDatapackManager() {
        return null;
    }

    @Override
    public @NotNull List<String> getInitialDisabledPacks() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull List<String> getInitialEnabledPacks() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull org.bukkit.ServerTickManager getServerTickManager() {
        return null;
    }

    @Override
    public boolean isStopping() {
        return console.isStopped();
    }

    @Override
    public boolean isLoggingIPs() {
        return true;
    }

    @Override
    public boolean isTickingWorlds() {
        return true;
    }

    @Override
    public boolean isResourcePackRequired() {
        return false;
    }

    @Override
    public @NotNull String getResourcePackHash() {
        return "";
    }

    @Override
    public @Nullable String getResourcePack() {
        return "";
    }

    @Override
    public @Nullable String getResourcePackPrompt() {
        return null;
    }

    @Override
    public boolean isAcceptingTransfers() {
        return false;
    }

    @Override
    public boolean isEnforcingSecureProfiles() {
        return false;
    }

    @Override
    public boolean shouldSendChatPreviews() {
        return false;
    }

    @Override
    public double[] getTPS() {
        return new double[] { 20.0, 20.0, 20.0 };
    }

    @Override
    public long[] getTickTimes() {
        return new long[0];
    }

    @Override
    public double getAverageTickTime() {
        return 0.0;
    }

    @Override
    public int getCurrentTick() {
        return 0;
    }

    @Override
    public boolean reloadCommandAliases() {
        return true;
    }

    @Override
    public void reloadPermissions() {
    }

    @Override
    public @NotNull <T extends Keyed> Iterable<Tag<T>> getTags(@NotNull String registry, @NotNull Class<T> clazz) {
        return Collections.emptyList();
    }

    @Override
    public @Nullable org.bukkit.scoreboard.Criteria getScoreboardCriteria(@NotNull String name) {
        return null;
    }

    @Override
    public @NotNull com.destroystokyo.paper.profile.PlayerProfile createProfile(@NotNull UUID uuid) {
        return createProfile(uuid, null);
    }

    @Override
    public @NotNull com.destroystokyo.paper.profile.PlayerProfile createProfile(@NotNull String name) {
        return createProfile(null, name);
    }

    @Override
    public @NotNull com.destroystokyo.paper.profile.PlayerProfile createProfile(@Nullable UUID uuid,
            @Nullable String name) {
        return new io.ampznetwork.lunararc.common.server.LunarArcPlayerProfile(uuid, name);
    }

    @Override
    public @NotNull com.destroystokyo.paper.profile.PlayerProfile createProfileExact(@Nullable UUID uuid,
            @Nullable String name) {
        return null;
    }

    @Override
    public @NotNull net.kyori.adventure.text.Component motd() {
        return net.kyori.adventure.text.Component.text(getMotd());
    }

    @Override
    public void motd(@NotNull net.kyori.adventure.text.Component motd) {
    }

    @Override
    public @NotNull net.kyori.adventure.text.Component shutdownMessage() {
        return net.kyori.adventure.text.Component.text(getShutdownMessage());
    }

    @Override
    public @NotNull org.bukkit.GameMode getDefaultGameMode() {
        return org.bukkit.GameMode.SURVIVAL;
    }

    @Override
    public void setDefaultGameMode(@NotNull org.bukkit.GameMode mode) {
    }

    @Override
    public int getMaxChainedNeighborUpdates() {
        return 1000000;
    }

    @Override
    public void setMotd(@NotNull String motd) {
    }

    @Override
    public boolean suggestPlayerNamesWhenNullTabCompletions() {
        return true;
    }

    @Override
    public @Nullable String getPermissionMessage() {
        return "";
    }

    @Override
    public @NotNull net.kyori.adventure.text.Component permissionMessage() {
        return net.kyori.adventure.text.Component.empty();
    }

    @Override
    public @NotNull Iterable<? extends net.kyori.adventure.audience.Audience> audiences() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull CommandSender createCommandSender(
            @NotNull Consumer<? super net.kyori.adventure.text.Component> feedback) {
        return null;
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull Entity entity) {
        return true;
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull World world, int x, int z) {
        return true;
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull Location location) {
        return true;
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull World world, int x, int y, int z) {
        return true;
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull Location location, int radius) {
        return true;
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull World world, @NotNull io.papermc.paper.math.Position position,
            int radius) {
        return true;
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull World world, @NotNull io.papermc.paper.math.Position position) {
        return true;
    }

    @Override
    public @NotNull String getUpdateFolder() {
        return "update";
    }

    @Override
    public @NotNull File getPluginsFolder() {
        return new File("plugins");
    }

    @Override
    public @NotNull File getUpdateFolderFile() {
        return new File("plugins");
    }

    @Override
    public @Nullable org.bukkit.command.PluginCommand getPluginCommand(@NotNull String name) {
        org.bukkit.command.Command command = commandMap.getCommand(name);
        if (command instanceof org.bukkit.command.PluginCommand) {
            return (org.bukkit.command.PluginCommand) command;
        }
        return null;
    }

    @Override
    public boolean dispatchCommand(@NotNull CommandSender sender, @NotNull String commandLine) {
        logger.info("[LunarArc] Dispatching command for " + sender.getName() + ": " + commandLine);
        try {
            boolean result = commandMap.dispatch(sender, commandLine);
            if (!result) {
                logger.warning("[LunarArc] Command map failed to dispatch: " + commandLine);
            }
            return result;
        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Error dispatching command: " + commandLine, e);
            return false;
        }
    }

    @Override
    public @NotNull List<Entity> selectEntities(@NotNull CommandSender sender, @NotNull String selector) {
        return Collections.emptyList();
    }

    @Override
    public @Nullable <T extends Keyed> Registry<T> getRegistry(@NotNull Class<T> type) {
        if (type == null) return null;
        return (Registry<T>) java.lang.reflect.Proxy.newProxyInstance(
                Registry.class.getClassLoader(),
                new Class<?>[] { Registry.class },
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if (methodName.equals("get") && args != null && args.length > 0) {
                        if (args[0] instanceof NamespacedKey key) {
                            if (type == Material.class) {
                                Material mat = Material.matchMaterial(key.getKey());
                                if (mat != null) return mat;
                                net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation
                                        .fromNamespaceAndPath(key.getNamespace(), key.getKey());
                                if (net.minecraft.core.registries.BuiltInRegistries.BLOCK.containsKey(rl)
                                        || net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(rl)) {
                                    return Material.matchMaterial(key.getKey().toUpperCase(java.util.Locale.ROOT));
                                }
                            }
                            if (type.isEnum()) {
                                String name = key.getKey().toUpperCase(java.util.Locale.ROOT);
                                for (T constant : type.getEnumConstants()) {
                                    if (((Enum<?>) constant).name().equalsIgnoreCase(name)) return constant;
                                }
                            }
                        }
                        return null;
                    }
                    if (methodName.equals("iterator")) {
                        List<T> values = new ArrayList<>();
                        if (type.isEnum()) {
                            for (T constant : type.getEnumConstants()) {
                                try {
                                    java.lang.reflect.Method isLegacy = constant.getClass().getMethod("isLegacy");
                                    if (!(Boolean) isLegacy.invoke(constant)) values.add(constant);
                                } catch (Exception e) {
                                    values.add(constant);
                                }
                            }
                        }
                        return values.iterator();
                    }
                    if (methodName.equals("stream")) {
                        List<T> values = new ArrayList<>();
                        if (type.isEnum()) {
                            Collections.addAll(values, type.getEnumConstants());
                        }
                        return values.stream();
                    }
                    if (methodName.equals("hashCode")) return System.identityHashCode(proxy);
                    if (methodName.equals("equals")) return proxy == args[0];
                    return null;
                });
    }

    @Override
    public @Nullable <T extends Keyed> Tag<T> getTag(@NotNull String registry, @NotNull NamespacedKey tag,
            @NotNull Class<T> clazz) {
        return null;
    }

    @Override
    public @Nullable LootTable getLootTable(@NotNull NamespacedKey key) {
        return null;
    }

    @Override
    public @NotNull BlockData createBlockData(@NotNull Material material) {
        return null;
    }

    @Override
    public @NotNull BlockData createBlockData(@NotNull Material material,
            @Nullable Consumer<? super BlockData> consumer) {
        return null;
    }

    @Override
    public @NotNull BlockData createBlockData(@NotNull String data) throws IllegalArgumentException {
        return null;
    }

    @Override
    public @NotNull BlockData createBlockData(@Nullable Material material, @Nullable String data)
            throws IllegalArgumentException {
        return null;
    }

    @Override
    public int getMaxWorldSize() {
        return 29999984;
    }

    @Override
    public @Nullable Entity getEntity(@NotNull UUID uuid) {
        return null;
    }

    @Override
    public @Nullable Advancement getAdvancement(@NotNull NamespacedKey key) {
        return null;
    }

    @Override
    public @NotNull Iterator<Advancement> advancementIterator() {
        return Collections.emptyIterator();
    }

    @Override
    public boolean removeBossBar(@NotNull NamespacedKey key) {
        return false;
    }

    @Override
    public @Nullable KeyedBossBar getBossBar(@NotNull NamespacedKey key) {
        return null;
    }

    @Override
    public @NotNull Iterator<KeyedBossBar> getBossBars() {
        return Collections.emptyIterator();
    }

    @Override
    public @NotNull KeyedBossBar createBossBar(@NotNull NamespacedKey key, @Nullable String title,
            @NotNull BarColor color, @NotNull BarStyle style, @NotNull BarFlag... flags) {
        return null;
    }

    @Override
    public @NotNull BossBar createBossBar(@Nullable String title, @NotNull BarColor color, @NotNull BarStyle style,
            @NotNull BarFlag... flags) {
        return null;
    }

    @Override
    public int getSpawnLimit(@NotNull SpawnCategory category) {
        return 70;
    }

    @Override
    public int getTicksPerSpawns(@NotNull SpawnCategory category) {
        return 1;
    }

    @Override
    public int getTicksPerAnimalSpawns() {
        return 400;
    }

    @Override
    public int getTicksPerMonsterSpawns() {
        return 1;
    }

    @Override
    public int getTicksPerWaterSpawns() {
        return 1;
    }

    @Override
    public int getTicksPerWaterAmbientSpawns() {
        return 1;
    }

    @Override
    public int getTicksPerWaterUndergroundCreatureSpawns() {
        return 1;
    }

    @Override
    public int getTicksPerAmbientSpawns() {
        return 1;
    }

    @Override
    public @NotNull PlayerProfile createPlayerProfile(@Nullable UUID uniqueId, @Nullable String name) {
        return null;
    }

    @Override
    public @NotNull PlayerProfile createPlayerProfile(@NotNull UUID uniqueId) {
        return null;
    }

    @Override
    public @NotNull PlayerProfile createPlayerProfile(@NotNull String name) {
        return null;
    }

    @Override
    public @Nullable UUID getPlayerUniqueId(@NotNull String name) {
        return null;
    }

    @Override
    public @NotNull ChunkGenerator.ChunkData createChunkData(@NotNull World world) {
        return null;
    }

    @Override
    public @NotNull Map<String, String[]> getCommandAliases() {
        return Collections.emptyMap();
    }

    @Override
    public boolean addRecipe(@Nullable Recipe recipe) {
        return false;
    }

    @Override
    public boolean addRecipe(@Nullable Recipe recipe, boolean b) {
        return false;
    }

    @Override
    public boolean removeRecipe(@NotNull NamespacedKey key) {
        return false;
    }

    @Override
    public boolean removeRecipe(@NotNull NamespacedKey key, boolean b) {
        return false;
    }

    @Override
    public @Nullable Recipe getRecipe(@NotNull NamespacedKey key) {
        return null;
    }

    @Override
    public @NotNull List<Recipe> getRecipesFor(@NotNull ItemStack result) {
        return Collections.emptyList();
    }

    @Override
    public @NotNull Iterator<Recipe> recipeIterator() {
        return Collections.emptyIterator();
    }

    @Override
    public void clearRecipes() {
    }

    @Override
    public void resetRecipes() {
    }

    @Override
    public void updateRecipes() {
    }

    @Override
    public @NotNull org.bukkit.inventory.ItemCraftResult craftItemResult(@NotNull ItemStack[] items,
            @NotNull World world) {
        return null;
    }

    @Override
    public @NotNull org.bukkit.inventory.ItemCraftResult craftItemResult(@NotNull ItemStack[] items,
            @NotNull World world, @NotNull Player player) {
        return null;
    }

    @Override
    public @Nullable ItemStack craftItem(@NotNull ItemStack[] items, @NotNull World world) {
        return null;
    }

    @Override
    public @Nullable ItemStack craftItem(@NotNull ItemStack[] items, @NotNull World world, @NotNull Player player) {
        return null;
    }

    @Override
    public @Nullable Recipe getCraftingRecipe(@NotNull ItemStack[] items, @NotNull World world) {
        return null;
    }

    @Override
    public @NotNull World createWorld(@NotNull WorldCreator creator) {
        return null;
    }

    @Override
    public boolean unloadWorld(@NotNull String name, boolean save) {
        return false;
    }

    @Override
    public boolean unloadWorld(@NotNull World world, boolean save) {
        return false;
    }

    @Override
    public @NotNull WorldBorder createWorldBorder() {
        return null;
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public @NotNull ItemStack createExplorerMap(@NotNull World world, @NotNull Location location,
            @NotNull org.bukkit.StructureType structureType) {
        return new ItemStack(org.bukkit.Material.FILLED_MAP);
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public @NotNull ItemStack createExplorerMap(@NotNull World world, @NotNull Location location,
            @NotNull org.bukkit.StructureType structureType, int radius, boolean findUnexplored) {
        return new ItemStack(org.bukkit.Material.FILLED_MAP);
    }

    @Override
    public @NotNull ItemStack createExplorerMap(@NotNull World world, @NotNull Location location,
            @NotNull org.bukkit.generator.structure.StructureType structureType,
            @NotNull org.bukkit.map.MapCursor.Type mapCursorType, int radius, boolean findUnexplored) {
        return null;
    }

    @Override
    public @NotNull ScoreboardManager getScoreboardManager() {
        return (ScoreboardManager) java.lang.reflect.Proxy.newProxyInstance(
            ScoreboardManager.class.getClassLoader(),
            new Class<?>[] { ScoreboardManager.class },
            (p, m, a) -> {
                if (m.getName().equals("getMainScoreboard")) {
                    return java.lang.reflect.Proxy.newProxyInstance(
                        org.bukkit.scoreboard.Scoreboard.class.getClassLoader(),
                        new Class<?>[] { org.bukkit.scoreboard.Scoreboard.class },
                        (sp, sm, sa) -> {
                            if (sm.getName().equals("getTeams")) return Collections.emptySet();
                            if (sm.getName().equals("getObjectives")) return Collections.emptySet();
                            if (sm.getReturnType().equals(Set.class)) return Collections.emptySet();
                            return null;
                        }
                    );
                }
                return null;
            }
        );
    }

    @Override
    public @NotNull org.bukkit.packs.ResourcePack getServerResourcePack() {
        return null;
    }

    @Override
    public void updateResources() {
    }

    public @NotNull FeatureFlagConfig getFeatureFlagConfig() {
        return (FeatureFlagConfig) java.lang.reflect.Proxy.newProxyInstance(
            FeatureFlagConfig.class.getClassLoader(),
            new Class<?>[] { FeatureFlagConfig.class },
            (proxy, method, args) -> {
                if (method.getReturnType().equals(Set.class)) return Collections.emptySet();
                if (method.getReturnType().equals(boolean.class)) return false;
                return null;
            }
        );
    }
}
