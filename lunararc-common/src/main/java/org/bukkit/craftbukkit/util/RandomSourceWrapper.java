package org.bukkit.craftbukkit.util;

import java.util.Objects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public final class RandomSourceWrapper implements RandomSource {
    private final java.util.Random random;

    public RandomSourceWrapper(java.util.Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override public RandomSource fork() { return new RandomSourceWrapper(new java.util.Random(this.random.nextLong())); }
    @Override public PositionalRandomFactory forkPositional() { return RandomSource.create(this.random.nextLong()).forkPositional(); }
    @Override public void setSeed(long seed) { this.random.setSeed(seed); }
    @Override public int nextInt() { return this.random.nextInt(); }
    @Override public int nextInt(int bound) { return this.random.nextInt(bound); }
    @Override public long nextLong() { return this.random.nextLong(); }
    @Override public boolean nextBoolean() { return this.random.nextBoolean(); }
    @Override public float nextFloat() { return this.random.nextFloat(); }
    @Override public double nextDouble() { return this.random.nextDouble(); }
    @Override public double nextGaussian() { return this.random.nextGaussian(); }


    public static final class RandomWrapper extends java.util.Random {
        private final RandomSource source;
        public RandomWrapper(RandomSource source) { this.source = Objects.requireNonNull(source, "source"); }
        @Override public void setSeed(long seed) { if (this.source != null) this.source.setSeed(seed); }
        @Override public int nextInt() { return this.source.nextInt(); }
        @Override public int nextInt(int bound) { return this.source.nextInt(bound); }
        @Override public long nextLong() { return this.source.nextLong(); }
        @Override public boolean nextBoolean() { return this.source.nextBoolean(); }
        @Override public float nextFloat() { return this.source.nextFloat(); }
        @Override public double nextDouble() { return this.source.nextDouble(); }
        @Override public double nextGaussian() { return this.source.nextGaussian(); }
        @Override public int nextInt(int origin, int bound) { return this.source.nextInt(origin, bound); }
    }
}
