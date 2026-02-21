package dev.fakeplayers.listener;

import dev.fakeplayers.FakePlayersPlugin;
import dev.fakeplayers.nms.FakePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collection;

public class PlayerConnectionListener implements Listener {

    private final FakePlayersPlugin plugin;

    public PlayerConnectionListener(FakePlayersPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (plugin.getFakePlayerManager().isFakePlayer(player)) {
            return;
        }

        Collection<FakePlayer> fakePlayers = plugin.getFakePlayerManager().getAllFakePlayers();
        if (!fakePlayers.isEmpty()) {
            FakePlayer.sendAddToPlayer(player, fakePlayers);
        }
    }
}
