package dev.fakeplayers;

import dev.fakeplayers.ai.AIChatHandler;
import dev.fakeplayers.ai.PersonalityManager;
import dev.fakeplayers.config.Config;
import dev.fakeplayers.config.GreetingsManager;
import dev.fakeplayers.manager.NamePool;
import dev.fakeplayers.manager.FakePlayerManager;
import dev.fakeplayers.manager.ActivityScheduler;
import dev.fakeplayers.listener.PlayerConnectionListener;
import dev.fakeplayers.listener.WelcomeHandler;
import dev.fakeplayers.listener.CommandListener;
import dev.fakeplayers.listener.AIChatListener;
import dev.fakeplayers.command.FakePlayersCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class FakePlayersPlugin extends JavaPlugin {

    private static FakePlayersPlugin instance;
    private Config config;
    private GreetingsManager greetingsManager;
    private NamePool namePool;
    private FakePlayerManager fakePlayerManager;
    private ActivityScheduler activityScheduler;
    private PersonalityManager personalityManager;
    private AIChatHandler aiChatHandler;

    @Override
    public void onEnable() {
        instance = this;

        if (!checkFolia()) {
            getLogger().warning("This plugin is designed for Folia. Some features may not work correctly on standard Paper/Spigot.");
        }

        saveDefaultResources();

        this.config = new Config(this);
        this.greetingsManager = new GreetingsManager(this);
        this.namePool = new NamePool(this);
        this.fakePlayerManager = new FakePlayerManager(this);
        this.activityScheduler = new ActivityScheduler(this);
        this.personalityManager = new PersonalityManager(this);
        this.aiChatHandler = new AIChatHandler(this);

        personalityManager.load();
        personalityManager.startGenerationAsync();

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new WelcomeHandler(this), this);
        getServer().getPluginManager().registerEvents(new CommandListener(this), this);
        getServer().getPluginManager().registerEvents(new AIChatListener(this), this);

        getCommand("fakeplayers").setExecutor(new FakePlayersCommand(this));
        getCommand("fakeplayers").setTabCompleter(new FakePlayersCommand(this));

        activityScheduler.start();
        
        aiChatHandler.start();

        setupPlaceholderAPI();

        getLogger().info("FakePlayersFolia has been enabled!");
    }

    private void setupPlaceholderAPI() {
        getLogger().info("[PAPI] Starting PlaceholderAPI integration check...");
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().info("[PAPI] PlaceholderAPI not found, placeholders disabled.");
            return;
        }
        
        getLogger().info("[PAPI] PlaceholderAPI detected, attempting registration...");
        
        try {
            Class<?> expansionClass = Class.forName("dev.fakeplayers.placeholder.FakePlayersExpansion");
            getLogger().info("[PAPI] Expansion class loaded successfully");
            
            Object expansion = expansionClass.getConstructor(FakePlayersPlugin.class).newInstance(this);
            getLogger().info("[PAPI] Expansion instance created");
            
            java.lang.reflect.Method registerMethod = expansionClass.getMethod("register");
            Boolean result = (Boolean) registerMethod.invoke(expansion);
            
            if (result) {
                getLogger().info("[PAPI] SUCCESS! Expansion registered!");
            } else {
                getLogger().warning("[PAPI] Registration returned false.");
            }
        } catch (ClassNotFoundException e) {
            getLogger().warning("[PAPI] Class not found: " + e.getMessage());
        } catch (NoClassDefFoundError e) {
            getLogger().warning("[PAPI] NoClassDefFoundError: " + e.getMessage());
        } catch (Throwable e) {
            getLogger().severe("[PAPI] Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (activityScheduler != null) {
            activityScheduler.stop();
        }
        if (fakePlayerManager != null) {
            fakePlayerManager.removeAll();
        }
        if (aiChatHandler != null) {
            aiChatHandler.stop();
        }
        if (personalityManager != null) {
            personalityManager.shutdown();
        }
        getLogger().info("FakePlayersFolia has been disabled!");
    }

    private boolean checkFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void saveDefaultResources() {
        saveResourceIfNotExists("config.yml");
        saveResourceIfNotExists("name-list.yml");
    }

    private void saveResourceIfNotExists(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            try {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                
                InputStream inputStream = getResource(fileName);
                if (inputStream != null) {
                    Files.copy(inputStream, file.toPath());
                }
            } catch (IOException e) {
                getLogger().severe("Could not save default " + fileName + ": " + e.getMessage());
            }
        }
    }

    public static FakePlayersPlugin getInstance() {
        return instance;
    }

    public Config getPluginConfig() {
        return config;
    }

    public GreetingsManager getGreetingsManager() {
        return greetingsManager;
    }

    public NamePool getNamePool() {
        return namePool;
    }

    public FakePlayerManager getFakePlayerManager() {
        return fakePlayerManager;
    }

    public ActivityScheduler getActivityScheduler() {
        return activityScheduler;
    }

    public PersonalityManager getPersonalityManager() {
        return personalityManager;
    }

    public AIChatHandler getAiChatHandler() {
        return aiChatHandler;
    }

    public void reload() {
        config.reload();
        greetingsManager.load();
        namePool.load();
        personalityManager.load();
        
        if (aiChatHandler != null) {
            aiChatHandler.onReload();
        }
        
        getLogger().info("Configuration and personalities reloaded!");
    }

    public void debug(String message) {
        if (config != null && config.isDebug()) {
            getLogger().info("[DEBUG] " + message);
        }
    }
}
