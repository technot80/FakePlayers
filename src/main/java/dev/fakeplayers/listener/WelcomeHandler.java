package dev.fakeplayers.listener;

import dev.fakeplayers.FakePlayersPlugin;
import dev.fakeplayers.config.Config;
import dev.fakeplayers.config.GreetingsManager;
import dev.fakeplayers.manager.FakePlayerManager;
import dev.fakeplayers.nms.FakePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class WelcomeHandler implements Listener {

    private final FakePlayersPlugin plugin;
    private final Config config;
    private final GreetingsManager greetingsManager;
    private final FakePlayerManager fakePlayerManager;

    private final Set<String> welcomedPlayers = ConcurrentHashMap.newKeySet();

    public WelcomeHandler(FakePlayersPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
        this.greetingsManager = plugin.getGreetingsManager();
        this.fakePlayerManager = plugin.getFakePlayerManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!config.isWelcomeEnabled()) {
            return;
        }

        Player player = event.getPlayer();

        if (fakePlayerManager.isFakePlayer(player)) {
            return;
        }

        String playerName = player.getName();
        if (welcomedPlayers.contains(playerName)) {
            return;
        }

        welcomedPlayers.add(playerName);

        scheduleCooldownRemoval(playerName);

        scheduleWelcomes(player);
    }

    private void scheduleWelcomes(Player target) {
        Collection<FakePlayer> allFakes = fakePlayerManager.getAllFakePlayers();
        if (allFakes.isEmpty()) {
            return;
        }

        int maxWelcomers = config.getWelcomeMaxConcurrent();
        int totalFakes = allFakes.size();
        
        int welcomerCount = Math.min(
            maxWelcomers,
            Math.max(1, totalFakes / 4 + ThreadLocalRandom.current().nextInt(3))
        );
        welcomerCount = Math.min(welcomerCount, totalFakes);

        List<FakePlayer> available = new ArrayList<>(allFakes);
        Collections.shuffle(available);

        Set<FakePlayer> selectedWelcomers = new HashSet<>();
        for (int i = 0; i < welcomerCount && i < available.size(); i++) {
            selectedWelcomers.add(available.get(i));
        }

        List<String> messages = target.hasPlayedBefore()
            ? greetingsManager.getRejoinMessages()
            : greetingsManager.getFirstJoinMessages();

        if (messages.isEmpty()) {
            return;
        }

        int i = 0;
        for (FakePlayer welcomer : selectedWelcomers) {
            int baseDelay = config.getWelcomeDelayMin() + (i * 2);
            int delay = baseDelay + ThreadLocalRandom.current().nextInt(3);
            delay = Math.min(delay, config.getWelcomeDelayMax());

            final int finalDelay = delay;
            final String message = messages.get(ThreadLocalRandom.current().nextInt(messages.size()))
                .replace("{player}", target.getName());

            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> {
                if (!target.isOnline()) {
                    return;
                }
                if (!welcomer.isValid()) {
                    return;
                }
                fakePlayerManager.chat(welcomer, message);
            }, finalDelay * 20L);

            i++;
        }
    }

    private void scheduleCooldownRemoval(String playerName) {
        Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> {
            welcomedPlayers.remove(playerName);
        }, config.getWelcomeCooldown() * 20L);
    }
}
