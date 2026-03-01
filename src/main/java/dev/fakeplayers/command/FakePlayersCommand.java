package dev.fakeplayers.command;

import dev.fakeplayers.FakePlayersPlugin;
import dev.fakeplayers.manager.FakePlayerManager;
import dev.fakeplayers.nms.FakePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class FakePlayersCommand implements CommandExecutor, TabCompleter {

    private final FakePlayersPlugin plugin;
    private static final List<String> SUBCOMMANDS = Arrays.asList("add", "remove", "list", "reload", "status");

    public FakePlayersCommand(FakePlayersPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "add":
                if (!sender.hasPermission("fakeplayers.add")) {
                    sender.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
                    return true;
                }
                return handleAdd(sender, args);
            case "remove":
                if (!sender.hasPermission("fakeplayers.remove")) {
                    sender.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
                    return true;
                }
                return handleRemove(sender, args);
            case "list":
                if (!sender.hasPermission("fakeplayers.list")) {
                    sender.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
                    return true;
                }
                return handleList(sender);
            case "reload":
                if (!sender.hasPermission("fakeplayers.reload")) {
                    sender.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
                    return true;
                }
                return handleReload(sender);
            case "status":
                if (!sender.hasPermission("fakeplayers.status")) {
                    sender.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
                    return true;
                }
                return handleStatus(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (String sub : SUBCOMMANDS) {
                if (sub.startsWith(args[0].toLowerCase()) && sender.hasPermission("fakeplayers." + sub)) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("remove") && sender.hasPermission("fakeplayers.remove")) {
                if ("all".startsWith(args[1].toLowerCase())) {
                    completions.add("all");
                }
                for (String name : plugin.getFakePlayerManager().getFakePlayerNames()) {
                    if (name.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(name);
                    }
                }
            }
        }

        return completions;
    }

    private boolean handleAdd(CommandSender sender, String[] args) {
        FakePlayerManager manager = plugin.getFakePlayerManager();

        if (args.length >= 2) {
            String name = args[1];
            FakePlayer fake = manager.addPlayer(name);
            if (fake != null) {
                sender.sendMessage(Component.text("Added fake player: " + name, NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("Could not add fake player: " + name + " (already exists or limit reached)", NamedTextColor.RED));
            }
        } else {
            String name = plugin.getNamePool().getRandomAvailableName();
            FakePlayer fake = manager.addPlayer(name);
            if (fake != null) {
                sender.sendMessage(Component.text("Added fake player: " + name, NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("Could not add fake player (limit reached)", NamedTextColor.RED));
            }
        }
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        FakePlayerManager manager = plugin.getFakePlayerManager();

        if (args.length >= 2) {
            String name = args[1];
            if (name.equalsIgnoreCase("all")) {
                int count = manager.getFakePlayerCount();
                manager.removeAll();
                sender.sendMessage(Component.text("Removed " + count + " fake players", NamedTextColor.GREEN));
            } else {
                FakePlayer fake = manager.getFakePlayer(name);
                if (fake != null) {
                    manager.removePlayer(fake);
                    sender.sendMessage(Component.text("Removed fake player: " + name, NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("Fake player not found: " + name, NamedTextColor.RED));
                }
            }
        } else {
            sender.sendMessage(Component.text("Usage: /fp remove <name|all>", NamedTextColor.RED));
        }
        return true;
    }

    private boolean handleList(CommandSender sender) {
        Collection<FakePlayer> fakes = plugin.getFakePlayerManager().getAllFakePlayers();

        if (fakes.isEmpty()) {
            sender.sendMessage(Component.text("No fake players online", NamedTextColor.YELLOW));
        } else {
            sender.sendMessage(Component.text("Fake players (" + fakes.size() + "):", NamedTextColor.GREEN));
            for (FakePlayer fake : fakes) {
                sender.sendMessage(Component.text(" - " + fake.getName(), NamedTextColor.GRAY));
            }
        }
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(Component.text("Configuration and personalities reloaded!", NamedTextColor.GREEN));
        return true;
    }

    private boolean handleStatus(CommandSender sender) {
        int realPlayers = 0;
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (!plugin.getFakePlayerManager().isFakePlayer(p)) {
                realPlayers++;
            }
        }

        int fakePlayers = plugin.getFakePlayerManager().getFakePlayerCount();
        int totalPlayers = realPlayers + fakePlayers;

        sender.sendMessage(Component.text("=== FakePlayers Status ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Real players: " + realPlayers, NamedTextColor.GREEN));
        sender.sendMessage(Component.text("Fake players: " + fakePlayers, NamedTextColor.AQUA));
        sender.sendMessage(Component.text("Total shown: " + totalPlayers, NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Max fake players: " + plugin.getPluginConfig().getMaxFakePlayers(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Min real players: " + plugin.getPluginConfig().getMinRealPlayers(), NamedTextColor.GRAY));

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== FakePlayers Commands ===", NamedTextColor.GOLD));
        if (sender.hasPermission("fakeplayers.add")) {
            sender.sendMessage(Component.text("/fp add [name]", NamedTextColor.GREEN).append(Component.text(" - Add a fake player", NamedTextColor.GRAY)));
        }
        if (sender.hasPermission("fakeplayers.remove")) {
            sender.sendMessage(Component.text("/fp remove <name|all>", NamedTextColor.GREEN).append(Component.text(" - Remove fake player(s)", NamedTextColor.GRAY)));
        }
        if (sender.hasPermission("fakeplayers.list")) {
            sender.sendMessage(Component.text("/fp list", NamedTextColor.GREEN).append(Component.text(" - List all fake players", NamedTextColor.GRAY)));
        }
        if (sender.hasPermission("fakeplayers.status")) {
            sender.sendMessage(Component.text("/fp status", NamedTextColor.GREEN).append(Component.text(" - Show status", NamedTextColor.GRAY)));
        }
        if (sender.hasPermission("fakeplayers.reload")) {
            sender.sendMessage(Component.text("/fp reload", NamedTextColor.GREEN).append(Component.text(" - Reload config", NamedTextColor.GRAY)));
        }
    }
}
