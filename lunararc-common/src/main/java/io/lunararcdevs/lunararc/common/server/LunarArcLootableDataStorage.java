package io.lunararcdevs.lunararc.common.server;

import com.destroystokyo.paper.loottable.PaperLootableInventoryData;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public final class LunarArcLootableDataStorage {

    private static final Map<Object, PaperLootableInventoryData> DATA =
            Collections.synchronizedMap(new WeakHashMap<>());

    private LunarArcLootableDataStorage() {}

    public static PaperLootableInventoryData get(Object holder, Supplier<PaperLootableInventoryData> factory) {
        return DATA.computeIfAbsent(holder, ignored -> factory.get());
    }
}
