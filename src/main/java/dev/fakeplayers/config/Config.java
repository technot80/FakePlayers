package dev.fakeplayers.config;

import dev.fakeplayers.FakePlayersPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class Config {

    private static final int CURRENT_CONFIG_VERSION = 2;

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
        checkAndUpgradeConfig();
    }

    private void checkAndUpgradeConfig() {
        int configVersion = yaml.getInt("config-version", 1);
        
        if (configVersion < CURRENT_CONFIG_VERSION) {
            plugin.getLogger().info("Upgrading config from version " + configVersion + " to " + CURRENT_CONFIG_VERSION);
            upgradeConfig(configVersion);
            configVersion = CURRENT_CONFIG_VERSION;
        }
        
        yaml.set("config-version", CURRENT_CONFIG_VERSION);
        save();
    }

    private void upgradeConfig(int fromVersion) {
        if (fromVersion < 2) {
            upgradeToV2();
        }
    }

    private void upgradeToV2() {
        plugin.getLogger().info("Applying upgrade to v2 (AI chat settings)...");
        
        yaml.set("ai.enabled", yaml.getBoolean("ai.enabled", false));
        yaml.set("ai.enabled-ai-count", yaml.getInt("ai.enabled-ai-count", 5));
        yaml.set("ai.selection-chance", yaml.getInt("ai.selection-chance", 50));
        yaml.set("ai.min-real-players", yaml.getInt("ai.min-real-players", 1));
        yaml.set("ai.chat-delay-min", yaml.getInt("ai.chat-delay-min", 100));
        yaml.set("ai.chat-delay-max", yaml.getInt("ai.chat-delay-max", 300));
        yaml.set("ai.response-chance", yaml.getInt("ai.response-chance", 30));
        yaml.set("ai.self-chat-chance", yaml.getInt("ai.self-chat-chance", 20));
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

    public int getAiEnabledCount() {
        return yaml.getInt("ai.enabled-ai-count", 5);
    }

    public int getAiSelectionChance() {
        return yaml.getInt("ai.selection-chance", 50);
    }

    public int getAiMinRealPlayers() {
        return yaml.getInt("ai.min-real-players", 1);
    }

    public int getAiChatDelayMin() {
        return yaml.getInt("ai.chat-delay-min", 100);
    }

    public int getAiChatDelayMax() {
        return yaml.getInt("ai.chat-delay-max", 300);
    }

    public int getAiResponseChance() {
        return yaml.getInt("ai.response-chance", 30);
    }

    public int getAiSelfChatChance() {
        return yaml.getInt("ai.self-chat-chance", 20);
    }

    public boolean isAiEnabled() {
        return yaml.getBoolean("ai.enabled", false) && getAiEnabledCount() > 0;
    }

    public boolean isAiGloballyEnabled() {
        return yaml.getBoolean("ai.enabled", false);
    }

    public int getConfigVersion() {
        return yaml.getInt("config-version", 1);
    }
}
