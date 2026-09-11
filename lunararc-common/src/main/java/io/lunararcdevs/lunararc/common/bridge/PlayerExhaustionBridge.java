package io.lunararcdevs.lunararc.common.bridge;

import org.bukkit.event.entity.EntityExhaustionEvent;

/** One-shot Bukkit exhaustion cause on the real NMS Player. */
public interface PlayerExhaustionBridge {
    void lunararc$pushExhaustionReason(EntityExhaustionEvent.ExhaustionReason reason);
}
