package io.lunararcdevs.lunararc.common.mixin.core.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.Set;
import java.util.TreeSet;

@Mixin(ItemArgument.class)
public abstract class ItemArgumentMixin {
    @Inject(method = "listSuggestions", at = @At("HEAD"), cancellable = true)
    private <S> void lunararc$skipEmptyRegistrySuggestions(CommandContext<S> context, SuggestionsBuilder builder,
                                                            CallbackInfoReturnable<CompletableFuture<Suggestions>> cir) {
        if (builder.getRemaining().isEmpty()) {
            Set<String> namespaces = new TreeSet<>();
            for (ResourceLocation key : BuiltInRegistries.ITEM.keySet()) {
                namespaces.add(key.getNamespace() + ":");
            }
            for (String namespace : namespaces) {
                builder.suggest(namespace);
            }
            cir.setReturnValue(builder.buildFuture());
        }
    }
}
