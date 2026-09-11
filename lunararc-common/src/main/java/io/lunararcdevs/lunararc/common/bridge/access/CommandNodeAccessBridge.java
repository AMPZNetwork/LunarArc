package io.lunararcdevs.lunararc.common.bridge.access;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.Map;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface CommandNodeAccessBridge<S> {
    Map<String, CommandNode<S>> lunararc$getChildren();
    Map<String, LiteralCommandNode<S>> lunararc$getLiterals();
    Map<String, ArgumentCommandNode<S, ?>> lunararc$getArguments();
}
