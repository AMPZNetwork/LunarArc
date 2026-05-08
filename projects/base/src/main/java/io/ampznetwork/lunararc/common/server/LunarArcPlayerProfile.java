package io.ampznetwork.lunararc.common.server;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class LunarArcPlayerProfile implements PlayerProfile {
    private UUID uuid;
    private String name;
    private final Set<ProfileProperty> properties = new HashSet<>();
    private PlayerTextures textures;

    public LunarArcPlayerProfile(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    // org.bukkit.profile.PlayerProfile methods
    @Override public @Nullable UUID getUniqueId() { return uuid; }
    
    // com.destroystokyo.paper.profile.PlayerProfile methods
    @Override public @Nullable String getName() { return name; }
    @Override public @Nullable String setName(@Nullable String name) { String old = this.name; this.name = name; return old; }
    @Override public @Nullable UUID getId() { return uuid; }
    @Override public @Nullable UUID setId(@Nullable UUID uuid) { UUID old = this.uuid; this.uuid = uuid; return old; }
    @Override public @NotNull Set<ProfileProperty> getProperties() { return properties; }
    @Override public void setProperties(@NotNull Collection<ProfileProperty> properties) { this.properties.clear(); this.properties.addAll(properties); }
    @Override public void setProperty(@NotNull ProfileProperty property) { properties.removeIf(p -> p.getName().equals(property.getName())); properties.add(property); }
    @Override public void clearProperties() { properties.clear(); }
    @Override public boolean removeProperty(@NotNull String name) { return properties.removeIf(p -> p.getName().equals(name)); }
    @Override public boolean hasProperty(@NotNull String name) { return properties.stream().anyMatch(p -> p.getName().equals(name)); }
    
    @Override 
    public @NotNull PlayerTextures getTextures() { 
        return textures != null ? textures : (textures = (PlayerTextures) java.lang.reflect.Proxy.newProxyInstance(PlayerTextures.class.getClassLoader(), new Class<?>[] { PlayerTextures.class }, (p, m, a) -> null)); 
    }
    
    @Override 
    public void setTextures(@Nullable PlayerTextures textures) { 
        this.textures = textures; 
    }
    
    @Override public @NotNull CompletableFuture<PlayerProfile> update() { return CompletableFuture.completedFuture(this); }
    
    @Override public boolean isComplete() { return uuid != null && name != null; }
    @Override public boolean completeFromCache() { return isComplete(); }
    @Override public boolean completeFromCache(boolean onlineMode) { return isComplete(); }
    @Override public boolean completeFromCache(boolean lookupUuid, boolean onlineMode) { return isComplete(); }
    @Override public boolean complete(boolean textures) { return isComplete(); }
    @Override public boolean complete(boolean textures, boolean onlineMode) { return isComplete(); }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        if (uuid != null) result.put("uniqueId", uuid.toString());
        if (name != null) result.put("name", name);
        return result;
    }

    @Override
    public @NotNull LunarArcPlayerProfile clone() {
        LunarArcPlayerProfile clone = new LunarArcPlayerProfile(uuid, name);
        clone.properties.addAll(this.properties);
        clone.textures = this.textures;
        return clone;
    }
}
