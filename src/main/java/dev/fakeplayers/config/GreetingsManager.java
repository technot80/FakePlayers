package dev.fakeplayers.config;

import dev.fakeplayers.FakePlayersPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class GreetingsManager {

    private final FakePlayersPlugin plugin;
    private final File file;
    private List<String> firstJoinMessages = new ArrayList<>();
    private List<String> rejoinMessages = new ArrayList<>();
    private List<String> msgResponses = new ArrayList<>();

    public GreetingsManager(FakePlayersPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "greetings.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            saveDefault();
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        
        firstJoinMessages = yaml.getStringList("first-join");
        rejoinMessages = yaml.getStringList("rejoin");
        msgResponses = yaml.getStringList("msg-responses");

        if (firstJoinMessages.isEmpty()) {
            firstJoinMessages = getDefaultFirstJoin();
        }
        if (rejoinMessages.isEmpty()) {
            rejoinMessages = getDefaultRejoin();
        }
        if (msgResponses.isEmpty()) {
            msgResponses = getDefaultMsgResponses();
        }

        plugin.debug("Loaded " + firstJoinMessages.size() + " first-join messages");
        plugin.debug("Loaded " + rejoinMessages.size() + " rejoin messages");
        plugin.debug("Loaded " + msgResponses.size() + " msg responses");
    }

    private void saveDefault() {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            
            InputStream inputStream = plugin.getResource("greetings.yml");
            if (inputStream != null) {
                Files.copy(inputStream, file.toPath());
            } else {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.set("first-join", getDefaultFirstJoin());
                yaml.set("rejoin", getDefaultRejoin());
                yaml.set("msg-responses", getDefaultMsgResponses());
                yaml.save(file);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save default greetings.yml", e);
        }
    }

    public List<String> getFirstJoinMessages() {
        return Collections.unmodifiableList(firstJoinMessages);
    }

    public List<String> getRejoinMessages() {
        return Collections.unmodifiableList(rejoinMessages);
    }

    public List<String> getMsgResponses() {
        return Collections.unmodifiableList(msgResponses);
    }

    public String getRandomFirstJoin(String playerName) {
        if (firstJoinMessages.isEmpty()) {
            return "Welcome " + playerName + "!";
        }
        return firstJoinMessages.get((int) (Math.random() * firstJoinMessages.size()))
            .replace("{player}", playerName);
    }

    public String getRandomRejoin(String playerName) {
        if (rejoinMessages.isEmpty()) {
            return "Welcome back " + playerName + "!";
        }
        return rejoinMessages.get((int) (Math.random() * rejoinMessages.size()))
            .replace("{player}", playerName);
    }

    public String getRandomMsgResponse() {
        if (msgResponses.isEmpty()) {
            return "...";
        }
        return msgResponses.get((int) (Math.random() * msgResponses.size()));
    }

    private List<String> getDefaultFirstJoin() {
        List<String> list = new ArrayList<>();
        list.add("Hey {player}! Welcome to the server!");
        list.add("Welcome {player}! Nice to see a new face!");
        list.add("Hi {player}, welcome aboard!");
        list.add("Welcome {player}, enjoy your stay!");
        list.add("Hey {player}! Glad you joined!");
        return list;
    }

    private List<String> getDefaultRejoin() {
        List<String> list = new ArrayList<>();
        list.add("Welcome back {player}!");
        list.add("Hey {player}, good to see you again!");
        list.add("{player} is back! Welcome!");
        list.add("wb {player}");
        list.add("Hey {player}! Long time no see!");
        return list;
    }

    private List<String> getDefaultMsgResponses() {
        List<String> list = new ArrayList<>();
        list.add("I'm busy right now, sorry!");
        list.add("afk");
        list.add("Can't talk right now");
        list.add("...");
        list.add("Not now");
        list.add("brb");
        list.add("busy");
        list.add("ttyl");
        return list;
    }
}
