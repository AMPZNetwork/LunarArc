package io.lunararcdevs.lunararc.common.mod.server;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Time-weighted rolling average used for Paper-compatible 1m/5m/15m TPS reporting.
 * This is ordinary common code; the owning samples live directly on the loader-owned
 * MinecraftServer through MinecraftServerMixin.
 */
public final class LunarArcRollingAverage {
    private static final long SECOND_NANOS = 1_000_000_000L;

    private final int size;
    private final BigDecimal[] samples;
    private final long[] times;
    private long time;
    private BigDecimal total;
    private int index;

    public LunarArcRollingAverage(int size) {
        if (size <= 0) throw new IllegalArgumentException("size must be positive");
        this.size = size;
        this.samples = new BigDecimal[size];
        this.times = new long[size];
        this.time = (long) size * SECOND_NANOS;
        this.total = BigDecimal.valueOf(20L)
                .multiply(BigDecimal.valueOf(SECOND_NANOS))
                .multiply(BigDecimal.valueOf(size));
        Arrays.fill(this.samples, BigDecimal.valueOf(20L));
        Arrays.fill(this.times, SECOND_NANOS);
    }

    public void add(BigDecimal sample, long elapsedNanos) {
        this.time -= this.times[this.index];
        this.total = this.total.subtract(this.samples[this.index].multiply(BigDecimal.valueOf(this.times[this.index])));
        this.samples[this.index] = sample;
        this.times[this.index] = elapsedNanos;
        this.time += elapsedNanos;
        this.total = this.total.add(sample.multiply(BigDecimal.valueOf(elapsedNanos)));
        if (++this.index == this.size) this.index = 0;
    }

    public double getAverage() {
        return this.total.divide(BigDecimal.valueOf(Math.max(1L, this.time)), 30, RoundingMode.HALF_UP).doubleValue();
    }
}
