package io.lunararcdevs.lunararc;

import io.lunararcdevs.lunararc.common.server.LunarArcProfileCacheWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import net.minecraft.nbt.CompoundTag;

public final class PaperPerformanceRegressionTest {
    public static void run() throws Exception {
        net.minecraft.SharedConstants.tryDetectVersion();
        var lookupThread = io.lunararcdevs.lunararc.common.server.LunarArcProfileLookupExecutor.executor()
                .submit(Thread::currentThread).get(2, java.util.concurrent.TimeUnit.SECONDS);
        check(lookupThread.isDaemon() && lookupThread.getName().startsWith("LunarArc Profile Lookup-"),
                "profile lookup did not use its dedicated daemon executor");
        var channel = new io.netty.channel.embedded.EmbeddedChannel(new io.netty.channel.ChannelInboundHandlerAdapter());
        var context = channel.pipeline().firstContext();
        var input = io.netty.buffer.Unpooled.buffer().writeByte(3).writeByte(1);
        var decoder = new io.lunararcdevs.lunararc.common.mixin.core.network.Varint21FrameDecoderMixin() {};
        var discard = decoder.getClass().getSuperclass().getDeclaredMethod("lunararc$discardDisconnectedInput",
                io.netty.channel.ChannelHandlerContext.class, io.netty.buffer.ByteBuf.class, java.util.List.class,
                org.spongepowered.asm.mixin.injection.callback.CallbackInfo.class);
        discard.setAccessible(true);
        try {
            var active = new org.spongepowered.asm.mixin.injection.callback.CallbackInfo("decode", true);
            discard.invoke(decoder, context, input, new java.util.ArrayList<>(), active);
            check(!active.isCancelled() && input.readableBytes() == 2, "active channel input was discarded");
            channel.close().syncUninterruptibly();
            var inactive = new org.spongepowered.asm.mixin.injection.callback.CallbackInfo("decode", true);
            discard.invoke(decoder, context, input, new java.util.ArrayList<>(), inactive);
            check(inactive.isCancelled() && !input.isReadable(), "inactive channel input was not discarded");
        } finally {
            input.release();
            channel.finishAndReleaseAll();
        }
        var directory = Files.createTempDirectory("lunararc-profile-test");
        var file = directory.resolve("usercache.json");
        try {
            for (int i = 0; i < 1000; i++) {
                try (var writer = LunarArcProfileCacheWriter.open(file.toFile(), StandardCharsets.UTF_8)) {
                    writer.write("[{\"name\":\"player-" + i + "\"}]");
                }
            }
            LunarArcProfileCacheWriter.flush();
            check(Files.readString(file).equals("[{\"name\":\"player-999\"}]"), "older profile snapshot overwrote latest save");
            Thread.currentThread().interrupt();
            LunarArcProfileCacheWriter.flush();
            check(Thread.interrupted(), "flush discarded interrupt status");
            try (var files = Files.list(directory)) {
                check(files.count() == 1, "profile writer leaked temporary files");
            }
        } finally {
            LunarArcProfileCacheWriter.flush();
            Files.deleteIfExists(file);
            Files.deleteIfExists(directory);
        }

        check(!ca.spottedleaf.dataconverter.minecraft.MCDataConverter.class.getProtectionDomain()
                .getCodeSource().getLocation().toString().contains("paper-server-"), "data converter missing from runtime surface");
        CompoundTag legacy = new CompoundTag();
        legacy.putString("id", "minecraft:stone");
        legacy.putByte("Count", (byte) 3);
        CompoundTag custom = new CompoundTag();
        custom.putString("example:payload", "preserved");
        legacy.put("tag", custom);
        CompoundTag converted = ca.spottedleaf.dataconverter.minecraft.MCDataConverter.convertTag(
                ca.spottedleaf.dataconverter.minecraft.datatypes.MCTypeRegistry.ITEM_STACK, legacy, 3700, 3955);
        check(converted.getInt("count") == 3, "item count was not migrated to components format");
        check(converted.getCompound("components").getCompound("minecraft:custom_data")
                .getString("example:payload").equals("preserved"), "custom item data lost during conversion");
        CompoundTag oldItem = new CompoundTag();
        oldItem.putShort("id", (short) 1);
        oldItem.putByte("Count", (byte) 2);
        oldItem.putShort("Damage", (short) 0);
        var flattened = ca.spottedleaf.dataconverter.minecraft.MCDataConverter.convertTag(
                ca.spottedleaf.dataconverter.minecraft.datatypes.MCTypeRegistry.ITEM_STACK, oldItem, 0, 3955);
        check(flattened.getString("id").equals("minecraft:stone") && flattened.getInt("count") == 2,
                "legacy numeric item id failed to migrate");
        CompoundTag entity = new CompoundTag();
        entity.putString("id", "example:custom_entity");
        entity.putString("example:data", "preserved");
        var upgradedEntity = ca.spottedleaf.dataconverter.minecraft.MCDataConverter.convertTag(
                ca.spottedleaf.dataconverter.minecraft.datatypes.MCTypeRegistry.ENTITY, entity, 3700, 3955);
        check(upgradedEntity.getString("id").equals("example:custom_entity")
                && upgradedEntity.getString("example:data").equals("preserved"), "mod entity data was lost");
        var json = com.google.gson.JsonParser.parseString("{\"id\":\"minecraft:stone\",\"Count\":3,\"tag\":{\"example:payload\":\"preserved\"}}").getAsJsonObject();
        var convertedJson = net.minecraft.util.datafix.DataFixers.getDataFixer().update(
                net.minecraft.util.datafix.fixes.References.ITEM_STACK,
                new com.mojang.serialization.Dynamic<>(com.mojang.serialization.JsonOps.INSTANCE, json),
                3700, 3955).getValue().getAsJsonObject();
        check(convertedJson.has("count") && convertedJson.get("count").getAsInt() == 3, "JSON item count was not migrated: " + convertedJson);
        var inventory = (org.bukkit.inventory.Inventory) java.lang.reflect.Proxy.newProxyInstance(
                PaperPerformanceRegressionTest.class.getClassLoader(), new Class<?>[]{org.bukkit.inventory.Inventory.class},
                (proxy, method, arguments) -> null);
        var item = new org.bukkit.inventory.ItemStack() {
            @Override
            public org.bukkit.inventory.ItemStack clone() {
                return this;
            }
        };
        var event = new io.papermc.paper.event.inventory.PaperInventoryMoveItemEvent(inventory, item, inventory, true);
        check(!event.calledGetItem && !event.calledSetItem, "event item flags were set before plugin access");
        event.getItem();
        check(event.calledGetItem && !event.calledSetItem, "event getItem tracking failed");
        event.setItem(item);
        check(event.calledSetItem, "event setItem tracking failed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
