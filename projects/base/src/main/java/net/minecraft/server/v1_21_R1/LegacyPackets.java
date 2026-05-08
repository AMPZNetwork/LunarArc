
package net.minecraft.server.v1_21_R1;

/**
 * This file contains stub classes for legacy NMS packets to maintain compatibility 
 * with plugins that use reflection to access these classes in the v1_21_R1 package.
 * 
 * Note: These are NOT public to allow them to coexist in a single source file.
 * Java allows multiple top-level non-public classes in one file.
 */

public class LegacyPackets {
    // Utility class to hold version info if needed
    public static final String VERSION = "v1_21_R1";
}

// Scoreboard Packets
class PacketPlayOutScoreboardTeam {}
class PacketPlayOutScoreboardObjective {}
class PacketPlayOutScoreboardDisplayObjective {}
class PacketPlayOutScoreboardScore {}

// Entity Packets
class PacketPlayOutNamedEntitySpawn {}
class PacketPlayOutEntityMetadata {}
class PacketPlayOutEntityTeleport {}
class PacketPlayOutRelEntityMove {}
class PacketPlayOutRelEntityMoveLook {}
class PacketPlayOutEntityLook {}
class PacketPlayOutEntityDestroy {}
class PacketPlayOutEntityHeadRotation {}

// Other Packets
class PacketPlayOutSpawnEntity {}
class PacketPlayOutSpawnEntityExperienceOrb {}
class PacketPlayOutSpawnEntityLiving {}
class PacketPlayOutSpawnEntityPainting {}
class PacketPlayOutChat {}
class PacketPlayOutPlayerInfo {}
class PacketPlayOutPlayerListHeaderFooter {}
class PacketPlayOutTitle {}
class PacketPlayOutWorldEvent {}
class PacketPlayOutWorldParticles {}
