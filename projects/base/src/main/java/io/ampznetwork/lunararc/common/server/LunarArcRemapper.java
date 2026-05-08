package io.ampznetwork.lunararc.common.server;

import io.ampznetwork.lunararc.libs.asm.ClassReader;
import io.ampznetwork.lunararc.libs.asm.ClassWriter;
import io.ampznetwork.lunararc.libs.asm.commons.ClassRemapper;
import io.ampznetwork.lunararc.libs.asm.commons.Remapper;

import java.util.HashMap;
import java.util.Map;

public class LunarArcRemapper extends io.ampznetwork.lunararc.libs.asm.commons.Remapper {
    private static final Map<String, String> CLASS_MAP = new HashMap<>();

    static {
        // Relocate old CraftBukkit versions to current
        for (int i = 17; i <= 21; i++) {
            for (int r = 1; r <= 10; r++) {
                String oldBase = "org/bukkit/craftbukkit/v1_" + i + "_R" + r;
                CLASS_MAP.put(oldBase, "org/bukkit/craftbukkit/v1_21_R1");
            }
        }
        
        // Common NMS Relocations (Spigot -> Mojang/Modern)
        CLASS_MAP.put("net/minecraft/server/v1_21_R1", "net/minecraft/server");
        CLASS_MAP.put("net/minecraft/network/NetworkManager", "net/minecraft/network/Connection");
        CLASS_MAP.put("net/minecraft/network/protocol/game/PacketPlayOutScoreboardTeam", "net/minecraft/network/protocol/game/ClientboundSetPlayerTeamPacket");
        CLASS_MAP.put("net/minecraft/network/protocol/game/PacketPlayOutChat", "net/minecraft/network/protocol/game/ClientboundSystemChatPacket");
        CLASS_MAP.put("net/minecraft/network/chat/IChatBaseComponent$ChatSerializer", "net/minecraft/network/chat/Component$Serializer");
        CLASS_MAP.put("net/minecraft/network/chat/IChatBaseComponent", "net/minecraft/network/chat/Component");
        CLASS_MAP.put("org/json/simple", "io/ampznetwork/lunararc/libs/json/simple");
        
        // Legacy Support for some plugins looking for very old names
        CLASS_MAP.put("org/bukkit/craftbukkit/CraftServer", "org/bukkit/craftbukkit/v1_21_R1/CraftServer");
        CLASS_MAP.put("org/bukkit/craftbukkit/entity/CraftPlayer", "org/bukkit/craftbukkit/v1_21_R1/entity/CraftPlayer");
    }

    @Override
    public String map(String internalName) {
        if (internalName == null) return null;
        
        // Fast path for already correctly versioned CraftBukkit
        if (internalName.startsWith("org/bukkit/craftbukkit/v1_21_R1/")) {
            return internalName;
        }

        // Fast path for CraftBukkit remapping
        if (internalName.startsWith("org/bukkit/craftbukkit/")) {
            // Check versioned old ones
            if (internalName.startsWith("org/bukkit/craftbukkit/v1_")) {
                for (Map.Entry<String, String> entry : CLASS_MAP.entrySet()) {
                    if (entry.getKey().startsWith("org/bukkit/craftbukkit/") && internalName.startsWith(entry.getKey())) {
                        return entry.getValue() + internalName.substring(entry.getKey().length());
                    }
                }
            }
            // Fallback for unversioned
            return "org/bukkit/craftbukkit/v1_21_R1/" + internalName.substring("org/bukkit/craftbukkit/".length());
        }

        // Common NMS / Library relocations
        for (Map.Entry<String, String> entry : CLASS_MAP.entrySet()) {
            if (internalName.startsWith(entry.getKey())) {
                String mapped = entry.getValue() + internalName.substring(entry.getKey().length());
                if (mapped.equals(internalName)) continue;
                return mapped;
            }
        }
        
        return internalName;
    }

    public byte[] transform(byte[] bytecode) {
        try {
            if (bytecode.length < 8) return bytecode;
            
            // Check major version (bytes 6-7)
            int major = ((bytecode[6] & 0xFF) << 8) | (bytecode[7] & 0xFF);
            if (major > 65) {
                // Compatibility patch for plugins compiled on newer JVMs
                bytecode[6] = 0;
                bytecode[7] = 65;
            }

            ClassReader reader = new ClassReader(bytecode);
            String className = reader.getClassName();
            
            ClassWriter writer = new ClassWriter(0);
            ClassRemapper remapper = new ClassRemapper(writer, this);
            reader.accept(remapper, 0);
            return writer.toByteArray();
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("69")) {
                // This is the infamous version 69 error. 
                // We return original bytecode and hope for the best, 
                // rather than crashing the whole plugin loading.
                return bytecode;
            }
            throw e;
        } catch (Exception e) {
            return bytecode;
        }
    }
}
