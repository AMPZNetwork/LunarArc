package io.ampznetwork.lunararc.common.events;

import io.ampznetwork.lunararc.i18n.TranslationManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Listener to notify OPs about updates in-game.
 */
public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) {
            checkAndNotify(player);
        }
    }

    private void checkAndNotify(Player player) {
        Path configPath = Paths.get("lunararc.conf");
        if (!Files.exists(configPath))
            return;

        try (InputStream in = Files.newInputStream(configPath)) {
            Properties props = new Properties();
            props.load(in);

            String current = props.getProperty("update.current");
            String latest = props.getProperty("update.latest");
            String url = props.getProperty("update.url");

            if (current != null && latest != null && !current.equals(latest)) {
                player.sendMessage(Component.text("----------------------------------------", NamedTextColor.GRAY));
                player.sendMessage(
                        Component.text("LunarArc Update Available!", NamedTextColor.AQUA, TextDecoration.BOLD));
                player.sendMessage(Component.text("Current: ", NamedTextColor.WHITE)
                        .append(Component.text(current, NamedTextColor.YELLOW)));
                player.sendMessage(Component.text("Latest:  ", NamedTextColor.WHITE)
                        .append(Component.text(latest, NamedTextColor.GREEN)));

                if (url != null && !url.isEmpty()) {
                    player.sendMessage(Component
                            .text("Click here to download the update", NamedTextColor.GOLD, TextDecoration.UNDERLINED)
                            .clickEvent(ClickEvent.openUrl(url)));
                }
                player.sendMessage(Component.text("----------------------------------------", NamedTextColor.GRAY));
            }
        } catch (Exception ignored) {
        }
    }
}
