package dev.fakeplayers.manager;

import dev.fakeplayers.FakePlayersPlugin;
import dev.fakeplayers.config.Config;
import dev.fakeplayers.nms.FakePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class FakePlayerManager {

    private final FakePlayersPlugin plugin;
    private final Config config;
    private final Map<UUID, FakePlayer> fakePlayers = new ConcurrentHashMap<>();
    private final Map<String, FakePlayer> fakePlayersByName = new ConcurrentHashMap<>();

    public FakePlayerManager(FakePlayersPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
    }

    public FakePlayer addPlayer(String name) {
        if (fakePlayersByName.containsKey(name)) {
            return null;
        }

        if (fakePlayers.size() >= config.getMaxFakePlayers()) {
            return null;
        }

        FakePlayer fakePlayer = new FakePlayer(name);

        fakePlayers.put(fakePlayer.getUuid(), fakePlayer);
        fakePlayersByName.put(name, fakePlayer);

        FakePlayer.broadcastAddToAll(Collections.singleton(fakePlayer));

        broadcastJoin(fakePlayer);

        plugin.debug("Added fake player: " + name);

        return fakePlayer;
    }

    public void removePlayer(FakePlayer fakePlayer) {
        if (fakePlayer == null || !fakePlayer.isValid()) {
            return;
        }

        fakePlayer.invalidate();

        broadcastQuit(fakePlayer);

        FakePlayer.broadcastRemoveFromAll(Collections.singleton(fakePlayer));

        fakePlayers.remove(fakePlayer.getUuid());
        fakePlayersByName.remove(fakePlayer.getName());
        
        if (plugin.getAiChatHandler() != null) {
            plugin.getAiChatHandler().onFakePlayerRemoved(fakePlayer.getName());
        }

        plugin.debug("Removed fake player: " + fakePlayer.getName());
    }

    public void removePlayer(String name) {
        FakePlayer fakePlayer = fakePlayersByName.get(name);
        if (fakePlayer != null) {
            removePlayer(fakePlayer);
        }
    }

    public void removePlayer(UUID uuid) {
        FakePlayer fakePlayer = fakePlayers.get(uuid);
        if (fakePlayer != null) {
            removePlayer(fakePlayer);
        }
    }

    public void removeAll() {
        List<FakePlayer> toRemove = new ArrayList<>(fakePlayers.values());
        for (FakePlayer fakePlayer : toRemove) {
            removePlayer(fakePlayer);
        }
    }

    public FakePlayer getRandomFakePlayer() {
        if (fakePlayers.isEmpty()) {
            return null;
        }

        List<FakePlayer> list = new ArrayList<>(fakePlayers.values());
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    public FakePlayer getRandomFakePlayerExcluding(Set<FakePlayer> exclude) {
        List<FakePlayer> available = new ArrayList<>();

        for (FakePlayer fake : fakePlayers.values()) {
            if (!exclude.contains(fake) && fake.isValid()) {
                available.add(fake);
            }
        }

        if (available.isEmpty()) {
            return null;
        }

        return available.get(ThreadLocalRandom.current().nextInt(available.size()));
    }

    public FakePlayer getFakePlayer(String name) {
        return fakePlayersByName.get(name);
    }

    public FakePlayer getFakePlayer(UUID uuid) {
        return fakePlayers.get(uuid);
    }

    public boolean isFakePlayer(Player player) {
        return fakePlayers.containsKey(player.getUniqueId());
    }

    public boolean isFakePlayer(UUID uuid) {
        return fakePlayers.containsKey(uuid);
    }

    public int getFakePlayerCount() {
        return fakePlayers.size();
    }

    public Collection<FakePlayer> getAllFakePlayers() {
        return Collections.unmodifiableCollection(fakePlayers.values());
    }

    public List<String> getFakePlayerNames() {
        return new ArrayList<>(fakePlayersByName.keySet());
    }

    public void broadcastJoin(FakePlayer fakePlayer) {
        String name = fakePlayer.getName();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(net.kyori.adventure.text.Component.text()
                .append(net.kyori.adventure.text.Component.text(name, NamedTextColor.YELLOW))
                .append(net.kyori.adventure.text.Component.text(" joined the game", NamedTextColor.YELLOW)));
        }
    }

    public void broadcastQuit(FakePlayer fakePlayer) {
        String name = fakePlayer.getName();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(net.kyori.adventure.text.Component.text()
                .append(net.kyori.adventure.text.Component.text(name, NamedTextColor.YELLOW))
                .append(net.kyori.adventure.text.Component.text(" left the game", NamedTextColor.YELLOW)));
        }
    }

    public void chat(FakePlayer fakePlayer, String message) {
        String formattedMessage = String.format("<%s> %s", fakePlayer.getName(), message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(net.kyori.adventure.text.Component.text(formattedMessage));
        }
    }
}
