package io.ampznetwork.lunararc;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

public final class PaperHelperRegressionTest {
    public static void run() throws Exception {
        for (String name : new String[]{
                "io.papermc.paper.block.fluid.PaperFluidData",
                "io.papermc.paper.entity.PaperBucketable",
                "io.papermc.paper.entity.PaperShearable",
                "io.papermc.paper.entity.PaperLeashable",
                "io.papermc.paper.entity.PaperSchoolableFish",
                "io.papermc.paper.inventory.BrewingSimpleContainerData",
                "io.papermc.paper.inventory.PaperInventoryCustomHolderContainer",
                "io.papermc.paper.persistence.PaperPersistentDataContainerView",
                "io.papermc.paper.util.ObfHelper",
                "io.papermc.paper.util.MCUtil",
                "io.papermc.paper.world.flag.PaperFeatureFlagProviderImpl",
                "io.papermc.paper.console.BrigadierCommandCompleter",
                "io.papermc.paper.commands.FeedbackForwardingSender",
                "org.bukkit.craftbukkit.command.CraftCommandMap",
                "org.bukkit.craftbukkit.Main"}) {
            Class<?> type = Class.forName(name, false, PaperHelperRegressionTest.class.getClassLoader());
            check(!type.getProtectionDomain().getCodeSource().getLocation().toString().contains("paper-server-"),
                    "helper resolved only from compile reference: " + name);
        }
        var block = io.papermc.paper.util.MCUtil.toBlockPosition(new org.bukkit.Location(null, -0.25, 64.9, 2.1));
        check(block.getX() == -1 && block.getY() == 64 && block.getZ() == 2, "block position must floor negative coordinates");
        var brewing = new io.papermc.paper.inventory.BrewingSimpleContainerData();
        check(brewing.getCount() == 3 && brewing.get(2) == 400, "brewing duration missing");
        CompoundTag data = new CompoundTag();
        data.putInt("test:value", 42);
        var view = new io.papermc.paper.persistence.PaperPersistentDataContainerView(
                new org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry()) {
            @Override public net.minecraft.nbt.Tag getTag(String key) { return data.get(key); }
            @Override public CompoundTag toTagCompound() { return data; }
        };
        NamespacedKey key = new NamespacedKey("test", "value");
        check(view.get(key, PersistentDataType.INTEGER) == 42, "persistent integer lost");
        check(!view.has(key, PersistentDataType.STRING), "persistent data type mismatch accepted");
        var target = new org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer();
        view.copyTo(target, true);
        check(target.get(key, PersistentDataType.INTEGER) == 42, "persistent data copy failed");
        check(view.serializeToBytes().length > 0, "persistent data serialization empty");
        System.out.println("Paper helper regressions passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
