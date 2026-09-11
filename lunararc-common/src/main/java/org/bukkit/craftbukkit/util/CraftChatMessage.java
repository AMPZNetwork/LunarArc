package org.bukkit.craftbukkit.util;

import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.bukkit.ChatColor;

public final class CraftChatMessage {
    private static final Pattern LINK_PATTERN = Pattern.compile(
            "((?:(?:https?):\\/\\/)?(?:[-\\w_\\.]{2,}\\.[a-z]{2,4}.*?(?=[\\.\\?!,;:]?(?:["
                    + ChatColor.COLOR_CHAR + " \\n]|$))))",
            Pattern.CASE_INSENSITIVE);
    private static final Map<Character, ChatFormatting> FORMAT_MAP = new HashMap<>();

    static {
        for (ChatFormatting format : ChatFormatting.values()) {
            String encoded = format.toString();
            if (encoded.length() >= 2) FORMAT_MAP.put(Character.toLowerCase(encoded.charAt(1)), format);
        }
    }

    private CraftChatMessage() {}

    public static ChatFormatting getColor(ChatColor color) {
        java.util.Objects.requireNonNull(color, "color");
        return FORMAT_MAP.get(Character.toLowerCase(color.getChar()));
    }

    public static ChatColor getColor(ChatFormatting format) {
        java.util.Objects.requireNonNull(format, "format");
        String encoded = format.toString();
        return encoded.length() >= 2 ? ChatColor.getByChar(encoded.charAt(1)) : null;
    }

    private static final class StringMessage {
        private static final Pattern INCREMENTAL_PATTERN = Pattern.compile(
                "(" + ChatColor.COLOR_CHAR + "[0-9a-fk-orx])|((?:(?:https?):\\/\\/)?(?:[-\\w_\\.]{2,}\\.[a-z]{2,4}.*?(?=[\\.\\?!,;:]?(?:["
                        + ChatColor.COLOR_CHAR + " \\n]|$))))|(\\n)", Pattern.CASE_INSENSITIVE);
        private static final Pattern INCREMENTAL_PATTERN_KEEP_NEWLINES = Pattern.compile(
                "(" + ChatColor.COLOR_CHAR + "[0-9a-fk-orx])|((?:(?:https?):\\/\\/)?(?:[-\\w_\\.]{2,}\\.[a-z]{2,4}.*?(?=[\\.\\?!,;:]?(?:["
                        + ChatColor.COLOR_CHAR + " ]|$))))", Pattern.CASE_INSENSITIVE);
        private static final Style EMPTY = Style.EMPTY.withItalic(false);
        private static final Style RESET = Style.EMPTY.withBold(false).withItalic(false).withUnderlined(false)
                .withStrikethrough(false).withObfuscated(false);

        private final List<Component> list = new ArrayList<>();
        private MutableComponent currentChatComponent = Component.empty();
        private Style modifier = Style.EMPTY;
        private final Component[] output;
        private int currentIndex;
        private StringBuilder hex;
        private final String message;

        private StringMessage(String message, boolean keepNewlines, boolean plain) {
            this.message = message;
            if (message == null) {
                this.output = new Component[] { this.currentChatComponent };
                return;
            }
            this.list.add(this.currentChatComponent);

            Matcher matcher = (keepNewlines ? INCREMENTAL_PATTERN_KEEP_NEWLINES : INCREMENTAL_PATTERN).matcher(message);
            boolean needsAdd = false;
            boolean hasReset = false;
            while (matcher.find()) {
                int groupId = 0;
                String match;
                do {
                    match = matcher.group(++groupId);
                } while (match == null);

                int index = matcher.start(groupId);
                if (index > this.currentIndex) {
                    needsAdd = false;
                    this.appendNewComponent(index);
                }

                switch (groupId) {
                    case 1 -> {
                        char c = match.toLowerCase(Locale.ROOT).charAt(1);
                        ChatFormatting format = FORMAT_MAP.get(c);
                        if (c == 'x') {
                            this.hex = new StringBuilder("#");
                        } else if (this.hex != null) {
                            this.hex.append(c);
                            if (this.hex.length() == 7) {
                                this.modifier = RESET.withColor(TextColor.parseColor(this.hex.toString()).result().orElse(null));
                                this.hex = null;
                            }
                        } else if (format != null && format.isFormat() && format != ChatFormatting.RESET) {
                            this.modifier = switch (format) {
                                case BOLD -> this.modifier.withBold(Boolean.TRUE);
                                case ITALIC -> this.modifier.withItalic(Boolean.TRUE);
                                case STRIKETHROUGH -> this.modifier.withStrikethrough(Boolean.TRUE);
                                case UNDERLINE -> this.modifier.withUnderlined(Boolean.TRUE);
                                case OBFUSCATED -> this.modifier.withObfuscated(Boolean.TRUE);
                                default -> this.modifier;
                            };
                        } else {
                            Style previous = this.modifier;
                            this.modifier = (!hasReset ? RESET : EMPTY).withColor(format);
                            hasReset = true;
                            if (previous.isBold()) this.modifier = this.modifier.withBold(false);
                            if (previous.isItalic()) this.modifier = this.modifier.withItalic(false);
                            if (previous.isObfuscated()) this.modifier = this.modifier.withObfuscated(false);
                            if (previous.isStrikethrough()) this.modifier = this.modifier.withStrikethrough(false);
                            if (previous.isUnderlined()) this.modifier = this.modifier.withUnderlined(false);
                        }
                        needsAdd = true;
                    }
                    case 2 -> {
                        if (plain) {
                            this.appendNewComponent(matcher.end(groupId));
                        } else {
                            String target = match.startsWith("http://") || match.startsWith("https://") ? match : "http://" + match;
                            this.modifier = this.modifier.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, target));
                            this.appendNewComponent(matcher.end(groupId));
                            this.modifier = this.modifier.withClickEvent((ClickEvent) null);
                        }
                    }
                    case 3 -> {
                        if (needsAdd) this.appendNewComponent(index);
                        this.currentChatComponent = null;
                    }
                    default -> throw new IllegalStateException("Unexpected CraftChatMessage matcher group " + groupId);
                }
                this.currentIndex = matcher.end(groupId);
            }

            if (this.currentIndex < message.length() || needsAdd) this.appendNewComponent(message.length());
            this.output = this.list.toArray(Component[]::new);
        }

        private void appendNewComponent(int index) {
            MutableComponent addition = Component.literal(this.message.substring(this.currentIndex, index)).setStyle(this.modifier);
            this.currentIndex = index;
            if (this.currentChatComponent == null) {
                this.currentChatComponent = Component.empty();
                this.list.add(this.currentChatComponent);
            }
            this.currentChatComponent.append(addition);
        }
    }

    public static Optional<Component> fromStringOrOptional(String message) {
        return Optional.ofNullable(fromStringOrNull(message));
    }

    public static Optional<Component> fromStringOrOptional(String message, boolean keepNewlines) {
        return Optional.ofNullable(fromStringOrNull(message, keepNewlines));
    }

    public static Component fromStringOrNull(String message) {
        return fromStringOrNull(message, false);
    }

    public static Component fromStringOrNull(String message, boolean keepNewlines) {
        return message == null || message.isEmpty() ? null : fromString(message, keepNewlines)[0];
    }

    public static Component fromStringOrEmpty(String message) {
        return fromStringOrEmpty(message, false);
    }

    public static Component fromStringOrEmpty(String message, boolean keepNewlines) {
        return fromString(message, keepNewlines)[0];
    }

    public static Component[] fromString(String message) {
        return fromString(message, false);
    }

    public static Component[] fromString(String message, boolean keepNewlines) {
        return fromString(message, keepNewlines, false);
    }

    public static Component[] fromString(String message, boolean keepNewlines, boolean plain) {
        return new StringMessage(message, keepNewlines, plain).output;
    }

    private static net.minecraft.core.HolderLookup.Provider lookupProvider() {
        net.minecraft.server.MinecraftServer server = io.lunararcdevs.lunararc.common.LunarArcServerAccess.getMinecraftServer();
        return server.registryAccess();
    }

    public static String toJSON(Component component) {
        return Component.Serializer.toJson(component, lookupProvider());
    }

    public static String toJSONOrNull(Component component) {
        return component == null ? null : toJSON(component);
    }

    public static MutableComponent fromJSON(String jsonMessage) throws JsonParseException {
        return Component.Serializer.fromJson(jsonMessage, lookupProvider());
    }

    public static MutableComponent fromJSONOrNull(String jsonMessage) {
        if (jsonMessage == null) return null;
        try {
            return fromJSON(jsonMessage);
        } catch (JsonParseException | IllegalArgumentException ex) {
            return null;
        }
    }

    public static Component fromJSONOrString(String message) {
        return fromJSONOrString(message, false);
    }

    public static Component fromJSONOrString(String message, boolean keepNewlines) {
        return fromJSONOrString(message, false, keepNewlines);
    }

    public static Component fromJSONOrString(String message, boolean nullable, boolean keepNewlines) {
        return fromJSONOrString(message, nullable, keepNewlines, Integer.MAX_VALUE, false);
    }

    public static Component fromJSONOrString(String message, boolean nullable, boolean keepNewlines, int maxLength,
            boolean checkJsonContentLength) {
        if (message == null) message = "";
        if (nullable && message.isEmpty()) return null;
        Component component = fromJSONOrNull(message);
        if (component != null) {
            if (checkJsonContentLength) {
                String content = fromComponent(component);
                String trimmed = trimMessage(content, maxLength);
                if (content != trimmed) return fromString(trimmed, keepNewlines)[0];
            }
            return component;
        }
        return fromString(trimMessage(message, maxLength), keepNewlines)[0];
    }

    public static String trimMessage(String message, int maxLength) {
        return message != null && message.length() > maxLength ? message.substring(0, maxLength) : message;
    }

    public static String fromComponent(Component component) {
        if (component == null) return "";
        return io.lunararcdevs.lunararc.common.messaging.LunarArcComponentPipeline.toLegacy(
                io.lunararcdevs.lunararc.common.messaging.LunarArcComponentPipeline.toAdventure(component));
    }

    public static Component fixComponent(MutableComponent component) {
        return fixComponent(component, LINK_PATTERN.matcher(""));
    }

    private static Component fixComponent(MutableComponent component, Matcher matcher) {
        if (component.getContents() instanceof PlainTextContents text) {
            String msg = text.text();
            if (matcher.reset(msg).find()) {
                matcher.reset();
                Style modifier = component.getStyle();
                List<Component> extras = new ArrayList<>();
                List<Component> extrasOld = new ArrayList<>(component.getSiblings());
                component = Component.empty();
                int pos = 0;
                while (matcher.find()) {
                    String target = matcher.group();
                    if (!(target.startsWith("http://") || target.startsWith("https://"))) target = "http://" + target;
                    extras.add(Component.literal(msg.substring(pos, matcher.start())).setStyle(modifier));
                    extras.add(Component.literal(matcher.group()).setStyle(
                            modifier.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, target))));
                    pos = matcher.end();
                }
                extras.add(Component.literal(msg.substring(pos)).setStyle(modifier));
                extras.addAll(extrasOld);
                for (Component extra : extras) component.append(extra);
            }
        }

        List<Component> siblings = component.getSiblings();
        for (int i = 0; i < siblings.size(); i++) {
            Component child = siblings.get(i);
            if (child.getStyle().getClickEvent() == null) siblings.set(i, fixComponent(child.copy(), matcher));
        }

        if (component.getContents() instanceof TranslatableContents translatable) {
            Object[] args = translatable.getArgs();
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof Component child && child.getStyle().getClickEvent() == null) {
                    args[i] = fixComponent(child.copy(), matcher);
                } else if (arg instanceof String string && matcher.reset(string).find()) {
                    args[i] = fixComponent(Component.literal(string), matcher);
                }
            }
        }
        return component;
    }

    public static final class ChatSerializer {
        private ChatSerializer() {}
        public static Component fromJSON(String json) { return CraftChatMessage.fromJSON(json); }
        public static String toJSON(Component component) { return CraftChatMessage.toJSON(component); }
    }
}
