package io.lunararcdevs.lunararc.common.bridge.world.raid;

import net.minecraft.world.entity.raid.Raid;

import java.util.Map;

/** Narrow registry access mixed directly into the loader-owned Raids manager. */
public interface RaidsBridge {
    Map<Integer, Raid> lunararc$raids();
}
