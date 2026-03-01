package dev.fakeplayers.ai;

import dev.agentchat.api.ChatAPI;
import dev.agentchat.api.ChatResponse;
import dev.agentchat.api.ChatSession;
import dev.fakeplayers.FakePlayersPlugin;
import dev.fakeplayers.config.Config;
import dev.fakeplayers.manager.NamePool;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PersonalityManager {

    private final FakePlayersPlugin plugin;
    private final Config config;
    private final NamePool namePool;
    private final Map<String, String> personalities;
    private final File personalityFolder;
    private String globalSystemPrompt;
    private final ExecutorService executor;
    private volatile boolean generationComplete = false;

    private static final String GENERATOR_PROMPT = """
        You are a personality generator for Minecraft fake players. 
        Create a unique, distinct personality for a Minecraft player.
        
        Requirements:
        - Maximum 10 sentences
        - Include their name naturally: "You are [name],..."
        - Describe their playstyle, interests, and chat style
        - Make them friendly and positive
        - Never mention being AI or a bot
        - No swear words or offensive content
        - Be creative and varied - each personality should be unique
        
        Output ONLY the personality description, nothing else.
        """;

    public PersonalityManager(FakePlayersPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
        this.namePool = plugin.getNamePool();
        this.personalities = new HashMap<>();
        this.personalityFolder = new File(plugin.getDataFolder(), "personalities");
        this.executor = Executors.newSingleThreadExecutor();
        
        if (!personalityFolder.exists()) {
            personalityFolder.mkdirs();
        }
    }

    public void load() {
        personalities.clear();
        
        globalSystemPrompt = loadSystemPrompt();
        
        if (!config.isAiEnabled()) {
            plugin.getLogger().info("AI chat is disabled. Skipping personality loading.");
            return;
        }
        
        loadPersonalities();
    }

    public void startGenerationAsync() {
        if (!config.isAiEnabled()) {
            return;
        }
        
        if (!isApiAvailable()) {
            plugin.getLogger().warning("AgentChatAPI not available. Cannot generate personalities.");
            return;
        }
        
        executor.submit(() -> {
            generateMissingPersonalities();
            generationComplete = true;
        });
    }

    private String loadSystemPrompt() {
        File promptFile = new File(plugin.getDataFolder(), "system-prompt.txt");
        
        if (promptFile.exists()) {
            try {
                String prompt = Files.readString(promptFile.toPath()).trim();
                if (!prompt.isEmpty()) {
                    plugin.getLogger().info("Loaded custom system prompt");
                    return prompt;
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to load system prompt: " + e.getMessage());
            }
        }
        
        String defaultPrompt = getDefaultSystemPrompt();
        
        try {
            Files.writeString(promptFile.toPath(), defaultPrompt);
            plugin.getLogger().info("Created default system prompt file");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save default system prompt: " + e.getMessage());
        }
        
        return defaultPrompt;
    }

    private String getDefaultSystemPrompt() {
        return """
            You are playing Minecraft and chatting with other players. You are a REAL human player, not an AI.
            Never reveal that you are an AI or a bot. Never mention being artificial or programmed.
            Never write code, solve math problems, or discuss politics, religion, or controversial topics.
            Keep conversations light and fun - talk about gaming, building, mining, exploring, or everyday topics.
            Stay friendly and positive. Never use swear words, offensive language, or be mean to anyone.
            Keep your responses short and natural - like a casual Minecraft player chatting.
            Always stay in character with your assigned personality below.
            """;
    }

    private void loadPersonalities() {
        List<String> names = namePool.getNames();
        
        for (String name : names) {
            File personalityFile = new File(personalityFolder, name.toLowerCase() + ".txt");
            
            if (personalityFile.exists()) {
                try {
                    String personality = Files.readString(personalityFile.toPath()).trim();
                    if (!personality.isEmpty()) {
                        personalities.put(name, personality);
                        plugin.debug("Loaded personality for: " + name);
                    } else {
                        plugin.getLogger().warning("Empty personality file for: " + name);
                    }
                } catch (IOException e) {
                    plugin.getLogger().warning("Failed to load personality for " + name + ": " + e.getMessage());
                }
            }
        }
        
        plugin.getLogger().info("Loaded " + personalities.size() + " personalities");
    }

    private void generateMissingPersonalities() {
        if (!config.isAiEnabled()) {
            return;
        }
        
        if (!isApiAvailable()) {
            return;
        }
        
        List<String> names = namePool.getNames();
        
        for (String name : names) {
            if (personalities.containsKey(name)) {
                continue;
            }
            
            File personalityFile = new File(personalityFolder, name.toLowerCase() + ".txt");
            
            if (personalityFile.exists()) {
                continue;
            }
            
            String personality = generatePersonality(name);
            
            if (personality != null) {
                try {
                    Files.writeString(personalityFile.toPath(), personality);
                    synchronized (personalities) {
                        personalities.put(name, personality);
                    }
                    plugin.getLogger().info("Generated personality for: " + name);
                } catch (IOException e) {
                    plugin.getLogger().severe("Failed to save personality for " + name + ": " + e.getMessage());
                }
            }
        }
        
        plugin.getLogger().info("Personality generation complete!");
    }

    private String generatePersonality(String name) {
        try {
            String sessionName = "personality_gen_" + System.currentTimeMillis();
            
            ChatSession session = ChatAPI.get().createSession(sessionName, GENERATOR_PROMPT);
            
            String prompt = "Create a unique personality for a Minecraft player named " + name;
            
            ChatResponse response = session.sendMessage(prompt).join();
            
            ChatAPI.get().endSession(sessionName);
            
            if (response.isSuccess() && response.getContent() != null) {
                return cleanPersonality(response.getContent());
            } else {
                plugin.getLogger().severe("Failed to generate personality for " + name + ": " + response.getErrorMessage());
                return getFallbackPersonality(name);
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error generating personality for " + name + ": " + e.getMessage());
            return getFallbackPersonality(name);
        }
    }

    private String cleanPersonality(String content) {
        String cleaned = content.trim();
        
        cleaned = cleaned.replaceAll("^[\"']|[\"']$", "");
        
        String[] sentences = cleaned.split("[.!?]+");
        if (sentences.length > 10) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                sb.append(sentences[i].trim());
                if (i < 9) sb.append(". ");
            }
            cleaned = sb.toString().trim();
            if (!cleaned.endsWith(".")) {
                cleaned += ".";
            }
        }
        
        return cleaned;
    }

    private String getFallbackPersonality(String name) {
        return "You are " + name + ", a friendly Minecraft player who loves exploring and building. You enjoy chatting with other players and always try to help out.";
    }

    private boolean isApiAvailable() {
        try {
            ChatAPI.get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getPersonality(String name) {
        return personalities.get(name);
    }

    public boolean hasPersonality(String name) {
        return personalities.containsKey(name);
    }

    public String buildFullPrompt(String name) {
        String personality = getPersonality(name);
        if (personality == null) {
            personality = "You are a friendly Minecraft player having a casual conversation.";
        }
        
        return globalSystemPrompt + "\n\nYour personality: " + personality;
    }

    public int getPersonalityCount() {
        return personalities.size();
    }

    public String getGlobalSystemPrompt() {
        return globalSystemPrompt;
    }

    public void shutdown() {
        executor.shutdown();
    }
}
