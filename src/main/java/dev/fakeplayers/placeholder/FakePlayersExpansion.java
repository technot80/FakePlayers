package dev.fakeplayers.placeholder;

import dev.fakeplayers.FakePlayersPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FakePlayersExpansion extends PlaceholderExpansion {

    private final FakePlayersPlugin plugin;

    public FakePlayersExpansion(FakePlayersPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "fakeplayers";
    }

    @Override
    public @NotNull String getAuthor() {
        return "FakePlayersFolia";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String getRequiredPlugin() {
        return "FakePlayersFolia";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (plugin.getFakePlayerManager() == null) {
            return "0";
        }

        String param = params.toLowerCase();
        
        switch (param) {
            case "count":
                return String.valueOf(plugin.getFakePlayerManager().getFakePlayerCount());
                
            case "total_online":
            case "online":
                int realTotal = 0;
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (!plugin.getFakePlayerManager().isFakePlayer(p)) {
                        realTotal++;
                    }
                }
                return String.valueOf(realTotal + plugin.getFakePlayerManager().getFakePlayerCount());
                
            case "real_online":
            case "real":
                int realOnly = 0;
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (!plugin.getFakePlayerManager().isFakePlayer(p)) {
                        realOnly++;
                    }
                }
                return String.valueOf(realOnly);
                
            case "max":
                return String.valueOf(plugin.getPluginConfig().getMaxFakePlayers());
                
            case "min_real":
                return String.valueOf(plugin.getPluginConfig().getMinRealPlayers());
                
            default:
                return null;
        }
    }
}
