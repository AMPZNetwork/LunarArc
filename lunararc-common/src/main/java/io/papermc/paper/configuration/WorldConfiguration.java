package io.papermc.paper.configuration;

import io.papermc.paper.configuration.type.DurationOrDisabled;

public final class WorldConfiguration {

    public final Lootables lootables = new Lootables();
    public final Entities entities = new Entities();
    public final Anticheat anticheat = new Anticheat();

    public static WorldConfiguration forLevel(net.minecraft.world.level.Level level) {
        return ((io.lunararcdevs.lunararc.common.bridge.LevelBridge) level).lunararc$getPaperConfiguration();
    }

    public static final class Anticheat {
        public final AntiXray antiXray = new AntiXray();

        public static final class AntiXray {
            public boolean enabled;
            public int engineMode = 1;
            public int maxBlockHeight = 64;
            public int updateRadius = 2;
            public boolean lavaObscures;
            public java.util.List<String> hiddenBlocks = java.util.List.of();
        }
    }

    public static final class Entities {
        public final Markers markers = new Markers();

        public static final class Markers {
            public boolean tick = true;
        }

        public static final class Spawning {
            public static final class DuplicateUUID {
                public enum DuplicateUUIDMode { SAFE_REGEN, DELETE, NOTHING, WARN }
            }
        }
    }

    public static final class Misc {
        public enum RedstoneImplementation { VANILLA, EIGENCRAFT, ALTERNATE_CURRENT }
    }

    public static final class Lootables {
        public DurationOrDisabled restrictPlayerRelootTime = new DurationOrDisabled(java.util.Optional.empty());
        public boolean restrictPlayerReloot = true;
        public boolean autoReplenish = false;
        public int maxRefills = -1;
        public io.papermc.paper.configuration.type.Duration refreshMin = io.papermc.paper.configuration.type.Duration.of("12h");
        public io.papermc.paper.configuration.type.Duration refreshMax = io.papermc.paper.configuration.type.Duration.of("2d");
        public boolean resetSeedOnFill = true;
    }
}
