package dev.fakeplayers.listener;

import dev.fakeplayers.FakePlayersPlugin;
import dev.fakeplayers.config.GreetingsManager;
import dev.fakeplayers.manager.FakePlayerManager;
import dev.fakeplayers.nms.FakePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CommandListener implements Listener {

    private final FakePlayersPlugin plugin;
    private final GreetingsManager greetingsManager;
    private final FakePlayerManager fakePlayerManager;

    private static final List<String> MSG_COMMANDS = Arrays.asList(
        "/msg", "/tell", "/w", "/whisper", "/m", "/t", "/pm", "/message"
    );

    public CommandListener(FakePlayersPlugin plugin) {
        this.plugin = plugin;
        this.greetingsManager = plugin.getGreetingsManager();
        this.fakePlayerManager = plugin.getFakePlayerManager();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String rawMessage = event.getMessage();
        String[] parts = rawMessage.split(" ", 3);

        if (parts.length < 2) {
            return;
        }

        String command = parts[0].toLowerCase();
        if (!isMsgCommand(command)) {
            return;
        }

        String targetName = parts[1];
        FakePlayer fakePlayer = fakePlayerManager.getFakePlayer(targetName);

        if (fakePlayer == null) {
            return;
        }

        Player sender = event.getPlayer();

        String message = parts.length > 2 ? parts[2] : "";

        sender.sendMessage(Component.text()
            .append(Component.text("You whisper to " + fakePlayer.getName() + ": " + message, NamedTextColor.GRAY))
            .build());

        if (message.length() > 0 && ThreadLocalRandom.current().nextInt(100) < 30) {
            final String response = greetingsManager.getRandomMsgResponse();
            int delay = 2 + ThreadLocalRandom.current().nextInt(5);

            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> {
                if (!sender.isOnline()) return;

                sender.sendMessage(Component.text()
                    .append(Component.text(fakePlayer.getName() + " whispers to you: " + response, NamedTextColor.GRAY))
                    .build());
            }, delay * 20L);
        }

        event.setCancelled(true);
    }

    private boolean isMsgCommand(String command) {
        for (String msgCmd : MSG_COMMANDS) {
            if (command.equals(msgCmd) || command.startsWith(msgCmd + ":")) {
                return true;
            }
        }
        return false;
    }
}
