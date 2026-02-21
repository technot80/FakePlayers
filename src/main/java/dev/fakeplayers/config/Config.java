package dev.fakeplayers.config;

import dev.fakeplayers.FakePlayersPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Config {

    private final FakePlayersPlugin plugin;
    private final File file;
    private YamlConfiguration yaml;

    public Config(FakePlayersPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "config.yml");
        reload();
    }

    public void reload() {
        yaml = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml: " + e.getMessage());
        }
    }

    public String getServerHost() {
        return yaml.getString("server.host", "localhost");
    }

    public int getServerPort() {
        return yaml.getInt("server.port", 25565);
    }

    public int getMinRealPlayers() {
        return yaml.getInt("activity.min-real-players", 1);
    }

    public boolean isAlwaysActive() {
        return yaml.getBoolean("activity.always-active", false);
    }

    public int getMaxFakePlayers() {
        return yaml.getInt("activity.max-fake-players", 20);
    }

    public int getJoinDelayMin() {
        return yaml.getInt("join.delay-min", 30);
    }

    public int getJoinDelayMax() {
        return yaml.getInt("join.delay-max", 120);
    }

    public int getJoinChance() {
        return yaml.getInt("join.chance", 100);
    }

    public boolean isQuitEnabled() {
        return yaml.getBoolean("quit.enabled", true);
    }

    public int getQuitDelayMin() {
        return yaml.getInt("quit.delay-min", 300);
    }

    public int getQuitDelayMax() {
        return yaml.getInt("quit.delay-max", 900);
    }

    public boolean isWelcomeEnabled() {
        return yaml.getBoolean("welcome.enabled", true);
    }

    public int getWelcomeChance() {
        return yaml.getInt("welcome.chance", 50);
    }

    public int getWelcomeDelayMin() {
        return yaml.getInt("welcome.delay-min", 2);
    }

    public int getWelcomeDelayMax() {
        return yaml.getInt("welcome.delay-max", 8);
    }

    public int getWelcomeMaxConcurrent() {
        return yaml.getInt("welcome.max-concurrent", 3);
    }

    public int getWelcomeCooldown() {
        return yaml.getInt("welcome.cooldown", 60);
    }

    public List<String> getFirstJoinMessages() {
        return yaml.getStringList("welcome.first-join-messages");
    }

    public List<String> getRejoinMessages() {
        return yaml.getStringList("welcome.rejoin-messages");
    }

    public boolean isBotInvisible() {
        return yaml.getBoolean("bot.invisible", true);
    }

    public boolean isBotInvulnerable() {
        return yaml.getBoolean("bot.invulnerable", true);
    }

    public boolean isBotNoGravity() {
        return yaml.getBoolean("bot.no-gravity", true);
    }

    public boolean isBotNoCollision() {
        return yaml.getBoolean("bot.no-collision", true);
    }

    public boolean isBotHiddenFromPlayers() {
        return yaml.getBoolean("bot.hide-from-players", true);
    }

    public boolean isDebug() {
        return yaml.getBoolean("debug", false);
    }
}
