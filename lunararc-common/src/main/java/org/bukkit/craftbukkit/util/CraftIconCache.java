package org.bukkit.craftbukkit.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.bukkit.util.CachedServerIcon;

public final class CraftIconCache implements CachedServerIcon {
    private final byte[] value;

    public CraftIconCache(byte[] value) {
        this.value = value == null ? null : value.clone();
    }

    public byte[] getValue() {
        return this.value == null ? null : this.value.clone();
    }

    public String getData() {
        if (this.value == null) return null;
        return "data:image/png;base64," + new String(Base64.getEncoder().encode(this.value), StandardCharsets.UTF_8);
    }
}
