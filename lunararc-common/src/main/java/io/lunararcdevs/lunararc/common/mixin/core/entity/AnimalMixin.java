package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.AnimalBridge;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Bukkit love-mode semantics around the real Animal#setInLove transition. */
@Mixin(Animal.class)
public abstract class AnimalMixin implements AnimalBridge {
    @Unique private int lunararc$loveTicks = 600;
    @Unique private ItemStack lunararc$breedItem = ItemStack.EMPTY;
    @Unique private int lunararc$breedExperience = -1;

    @Override public ItemStack lunararc$getBreedItem() { return this.lunararc$breedItem; }
    @Override public void lunararc$setBreedItem(ItemStack item) { this.lunararc$breedItem = item == null ? ItemStack.EMPTY : item.copy(); }

    @Inject(method = "setInLove", at = @At("HEAD"), cancellable = true, require = 0)
    private void lunararc$enterLoveMode(Player player, CallbackInfo ci) {
        Animal animal = (Animal) (Object) this;
        if (animal.level().isClientSide) return;
        Object bukkitAnimal = ((EntityBridge) animal).lunararc$getBukkitEntity();
        if (!(bukkitAnimal instanceof org.bukkit.entity.Animals animals)) return;
        org.bukkit.entity.HumanEntity human = null;
        if (player != null) {
            Object bukkitPlayer = ((EntityBridge) player).lunararc$getBukkitEntity();
            if (bukkitPlayer instanceof org.bukkit.entity.HumanEntity value) human = value;
        }
        var event = new org.bukkit.event.entity.EntityEnterLoveModeEvent(animals, human, 600);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
            return;
        }
        this.lunararc$loveTicks = Math.max(0, event.getTicksInLove());
        this.lunararc$breedItem = player == null ? ItemStack.EMPTY : player.getInventory().getSelected().copy();
    }

    @ModifyConstant(method = "setInLove", constant = @Constant(intValue = 600), require = 0)
    private int lunararc$pluginLoveTicks(int vanilla) {
        return this.lunararc$loveTicks;
    }

    @Inject(method = "setInLove", at = @At("RETURN"), require = 0)
    private void lunararc$clearLoveTicks(Player player, CallbackInfo ci) {
        this.lunararc$loveTicks = 600;
    }

    @Inject(
            method = "spawnChildFromBreeding",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Animal;finalizeSpawnChildFromBreeding(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/Animal;Lnet/minecraft/world/entity/AgeableMob;)V"),
            cancellable = true,
            require = 0)
    private void lunararc$breedEvent(ServerLevel level, Animal otherParent, CallbackInfo ci, @Local AgeableMob child) {
        Animal self = (Animal) (Object) this;
        if (child == null || level.isClientSide) return;
        net.minecraft.server.level.ServerPlayer breeder = self.getLoveCause();
        if (breeder == null) breeder = otherParent.getLoveCause();
        int experience = self.getRandom().nextInt(7) + 1;
        ItemStack bredWith = this.lunararc$breedItem.isEmpty()
                ? ((AnimalBridge) otherParent).lunararc$getBreedItem()
                : this.lunararc$breedItem;
        var event = org.bukkit.craftbukkit.event.CraftEventFactory.callEntityBreedEvent(
                child, self, otherParent, breeder, bredWith, experience);
        if (event.isCancelled()) {
            this.lunararc$breedExperience = -1;
            this.lunararc$breedItem = ItemStack.EMPTY;
            ci.cancel();
            return;
        }
        this.lunararc$breedExperience = Math.max(0, event.getExperience());
        ((EntityBridge) child).lunararc$setSpawnReason(org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.BREEDING);
    }

    @org.spongepowered.asm.mixin.injection.ModifyArg(
            method = "finalizeSpawnChildFromBreeding",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;<init>(Lnet/minecraft/world/level/Level;DDDI)V"),
            index = 4,
            require = 0)
    private int lunararc$breedExperience(int vanillaExperience) {
        return this.lunararc$breedExperience < 0 ? vanillaExperience : this.lunararc$breedExperience;
    }

    @Inject(method = "spawnChildFromBreeding", at = @At("RETURN"), require = 0)
    private void lunararc$clearBreedContext(ServerLevel level, Animal otherParent, CallbackInfo ci) {
        this.lunararc$breedExperience = -1;
        this.lunararc$breedItem = ItemStack.EMPTY;
    }
}
