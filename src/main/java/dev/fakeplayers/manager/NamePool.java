package dev.fakeplayers.manager;

import dev.fakeplayers.FakePlayersPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NamePool {

    private final FakePlayersPlugin plugin;
    private final File file;
    private List<String> names = new ArrayList<>();

    public NamePool(FakePlayersPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "name-list.yml");
        load();
    }

    public void load() {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        List<String> loadedNames = yaml.getStringList("names");

        if (loadedNames.isEmpty()) {
            plugin.getLogger().warning("No names found in name-list.yml! Using default names.");
            names = getDefaultNames();
        } else {
            names = new ArrayList<>(loadedNames);
        }

        plugin.debug("Loaded " + names.size() + " names from pool");
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("names", names);
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save name-list.yml: " + e.getMessage());
        }
    }

    public String getRandomName() {
        if (names.isEmpty()) {
            return "Player" + ThreadLocalRandom.current().nextInt(1000, 9999);
        }
        return names.get(ThreadLocalRandom.current().nextInt(names.size()));
    }

    public String getRandomAvailableName() {
        for (int i = 0; i < 10; i++) {
            String name = getRandomName();
            if (plugin.getFakePlayerManager().getFakePlayer(name) == null) {
                return name;
            }
        }
        return "Player" + ThreadLocalRandom.current().nextInt(10000, 99999);
    }

    public List<String> getNames() {
        return Collections.unmodifiableList(names);
    }

    public void addName(String name) {
        if (!names.contains(name)) {
            names.add(name);
            save();
        }
    }

    public void removeName(String name) {
        if (names.remove(name)) {
            save();
        }
    }

    private List<String> getDefaultNames() {
        List<String> defaults = new ArrayList<>();
        defaults.add("Steve");
        defaults.add("Alex");
        defaults.add("DragonSlayer99");
        defaults.add("PixelMaster");
        defaults.add("CraftKing");
        defaults.add("BlockBuilder");
        defaults.add("NetherWalker");
        defaults.add("EndSeeker");
        defaults.add("RedstoneWiz");
        defaults.add("EmeraldMiner");
        return defaults;
    }
}
