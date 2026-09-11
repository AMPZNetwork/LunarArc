package io.lunararcdevs.lunararc.common.server;

import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

public final class LunarArcPlayerTextures implements PlayerTextures {
    private URL skin;
    private URL cape;
    private SkinModel skinModel = SkinModel.CLASSIC;
    private long timestamp;
    private boolean signed;

    @Override
    public boolean isEmpty() {
        return skin == null && cape == null;
    }

    @Override
    public void clear() {
        skin = null;
        cape = null;
        skinModel = SkinModel.CLASSIC;
        timestamp = 0L;
        signed = false;
    }

    @Override
    public @Nullable URL getSkin() {
        return skin;
    }

    @Override
    public void setSkin(@Nullable URL skinUrl) {
        setSkin(skinUrl, SkinModel.CLASSIC);
    }

    @Override
    public void setSkin(@Nullable URL skinUrl, @Nullable SkinModel skinModel) {
        this.skin = skinUrl;
        this.skinModel = skinUrl == null || skinModel == null ? SkinModel.CLASSIC : skinModel;
        this.timestamp = 0L;
        this.signed = false;
    }

    @Override
    public @NotNull SkinModel getSkinModel() {
        return skinModel;
    }

    @Override
    public @Nullable URL getCape() {
        return cape;
    }

    @Override
    public void setCape(@Nullable URL capeUrl) {
        this.cape = capeUrl;
        this.timestamp = 0L;
        this.signed = false;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean isSigned() {
        return signed;
    }
}
