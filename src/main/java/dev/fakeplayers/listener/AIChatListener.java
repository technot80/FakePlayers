package dev.fakeplayers.listener;

import dev.fakeplayers.FakePlayersPlugin;
import dev.fakeplayers.manager.FakePlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AIChatListener implements Listener {

    private final FakePlayersPlugin plugin;

    public AIChatListener(FakePlayersPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        FakePlayerManager manager = plugin.getFakePlayerManager();

        if (manager.isFakePlayer(player)) {
            return;
        }

        String message = event.getMessage();
        
        plugin.getAiChatHandler().onRealPlayerChat(player, message);
    }
}
