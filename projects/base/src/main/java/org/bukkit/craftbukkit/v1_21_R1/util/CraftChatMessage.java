package org.bukkit.craftbukkit.v1_21_R1.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class CraftChatMessage {
    private CraftChatMessage() {}

    public static Component fromStringOrNull(String message) {
        if (message == null || message.isEmpty()) return null;
        return Component.literal(message);
    }

    public static Component[] fromString(String message) {
        return new Component[] { fromStringOrNull(message) };
    }

    public static String fromComponent(Component component) {
        if (component == null) return "";
        return component.getString();
    }
    
    public static MutableComponent fromJSON(String json) {
        return Component.Serializer.fromJson(json, net.minecraft.core.HolderLookup.Provider.create(java.util.stream.Stream.empty()));
    }
    
    public static String toJSON(Component component) {
        return Component.Serializer.toJson(component, net.minecraft.core.HolderLookup.Provider.create(java.util.stream.Stream.empty()));
    }
    
    public static final class ChatSerializer {
        public static Component fromJSON(String json) {
            return CraftChatMessage.fromJSON(json);
        }
        
        public static String toJSON(Component component) {
            return CraftChatMessage.toJSON(component);
        }
    }
}
