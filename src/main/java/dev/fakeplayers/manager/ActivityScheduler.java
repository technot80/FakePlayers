package dev.fakeplayers.manager;

import dev.fakeplayers.FakePlayersPlugin;
import dev.fakeplayers.config.Config;
import dev.fakeplayers.nms.FakePlayer;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class ActivityScheduler {

    private final FakePlayersPlugin plugin;
    private final Config config;
    private final FakePlayerManager fakePlayerManager;

    private boolean running = false;
    private ScheduledTask joinTask = null;
    private ScheduledTask quitTask = null;

    public ActivityScheduler(FakePlayersPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
        this.fakePlayerManager = plugin.getFakePlayerManager();
    }

    public void start() {
        running = true;
        scheduleNextJoin();
        scheduleNextQuit();
    }

    public void stop() {
        running = false;
        if (joinTask != null) {
            joinTask.cancel();
        }
        if (quitTask != null) {
            quitTask.cancel();
        }
    }

    private void scheduleNextJoin() {
        if (!running) return;

        long delay = ThreadLocalRandom.current().nextLong(
            config.getJoinDelayMin(),
            config.getJoinDelayMax() + 1
        );

        joinTask = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> {
            if (!running) return;

            if (shouldAddPlayer()) {
                String name = plugin.getNamePool().getRandomName();
                if (name != null) {
                    fakePlayerManager.addPlayer(name);
                }
            }

            scheduleNextJoin();
        }, delay * 20L);
    }

    private void scheduleNextQuit() {
        if (!running || !config.isQuitEnabled()) return;

        long delay = ThreadLocalRandom.current().nextLong(
            config.getQuitDelayMin(),
            config.getQuitDelayMax() + 1
        );

        quitTask = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> {
            if (!running) return;

            if (shouldRemovePlayer()) {
                FakePlayer fake = fakePlayerManager.getRandomFakePlayer();
                if (fake != null) {
                    fakePlayerManager.removePlayer(fake);
                }
            }

            scheduleNextQuit();
        }, delay * 20L);
    }

    private boolean shouldAddPlayer() {
        int realPlayers = getRealPlayerCount();
        int fakePlayers = fakePlayerManager.getFakePlayerCount();

        if (!config.isAlwaysActive() && realPlayers < config.getMinRealPlayers()) {
            return false;
        }

        if (fakePlayers >= config.getMaxFakePlayers()) {
            return false;
        }

        return ThreadLocalRandom.current().nextInt(100) < config.getJoinChance();
    }

    private boolean shouldRemovePlayer() {
        int fakePlayers = fakePlayerManager.getFakePlayerCount();
        return fakePlayers > 0;
    }

    private int getRealPlayerCount() {
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!fakePlayerManager.isFakePlayer(player)) {
                count++;
            }
        }
        return count;
    }
}
