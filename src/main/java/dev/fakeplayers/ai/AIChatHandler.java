package dev.fakeplayers.ai;

import dev.agentchat.api.ChatAPI;
import dev.agentchat.api.ChatResponse;
import dev.agentchat.api.ChatSession;
import dev.fakeplayers.FakePlayersPlugin;
import dev.fakeplayers.config.Config;
import dev.fakeplayers.manager.FakePlayerManager;
import dev.fakeplayers.nms.FakePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class AIChatHandler {

    private final FakePlayersPlugin plugin;
    private final Config config;
    private final FakePlayerManager fakePlayerManager;
    private final PersonalityManager personalityManager;
    private final FakePlayerAI aiTracker;
    private final Map<String, ChatSession> sessions;
    private volatile boolean chatTaskRunning = false;
    private BukkitRunnable chatTask;

    public AIChatHandler(FakePlayersPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
        this.fakePlayerManager = plugin.getFakePlayerManager();
        this.personalityManager = plugin.getPersonalityManager();
        this.aiTracker = new FakePlayerAI();
        this.sessions = new HashMap<>();
    }

    public void start() {
        if (!config.isAiGloballyEnabled()) {
            plugin.getLogger().info("AI chat is disabled in config. Set ai.enabled: true to enable.");
            return;
        }
        
        if (config.getAiEnabledCount() <= 0) {
            plugin.getLogger().info("AI chat is disabled (enabled-ai-count is 0)");
            return;
        }

        if (!isApiAvailable()) {
            plugin.getLogger().warning("AgentChatAPI not found. AI chat features disabled.");
            return;
        }

        plugin.getLogger().info("AI Chat Handler started");
        startChatTask();
    }

    public void stop() {
        if (chatTask != null) {
            chatTask.cancel();
            chatTask = null;
        }

        for (ChatSession session : sessions.values()) {
            if (session != null) {
                ChatAPI.get().endSession(getSessionName(session.getName()));
            }
        }
        sessions.clear();
        aiTracker.clear();
    }

    private boolean isApiAvailable() {
        try {
            ChatAPI.get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void startChatTask() {
        chatTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!shouldChat()) {
                    return;
                }
                runAiChatCycle();
            }
        };
        chatTask.runTaskTimerAsynchronously(plugin, 100L, 100L);
    }

    private boolean shouldChat() {
        int realPlayers = getRealPlayerCount();
        return realPlayers >= config.getAiMinRealPlayers() && config.isAiEnabled();
    }

    private int getRealPlayerCount() {
        int count = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!fakePlayerManager.isFakePlayer(p)) {
                count++;
            }
        }
        return count;
    }

    private void runAiChatCycle() {
        if (chatTaskRunning) {
            return;
        }
        chatTaskRunning = true;

        try {
            selectAiPlayers();
            handleInterPlayerChat();
        } finally {
            chatTaskRunning = false;
        }
    }

    private void selectAiPlayers() {
        Collection<FakePlayer> allFakes = fakePlayerManager.getAllFakePlayers();
        int enabledCount = config.getAiEnabledCount();
        
        for (FakePlayer fake : allFakes) {
            String name = fake.getName();
            
            if (aiTracker.hasFailed(name)) {
                continue;
            }
            
            if (!aiTracker.isAiEnabled(name) && aiTracker.getAiEnabledCount() < enabledCount) {
                if (ThreadLocalRandom.current().nextInt(100) < config.getAiSelectionChance()) {
                    aiTracker.setAiEnabled(name, true);
                    plugin.debug("Enabled AI for: " + name);
                }
            }
        }
        
        if (aiTracker.getAiEnabledCount() > enabledCount) {
            List<String> enabled = new ArrayList<>(aiTracker.getAiEnabledPlayers());
            while (enabled.size() > enabledCount) {
                String toRemove = enabled.remove(ThreadLocalRandom.current().nextInt(enabled.size()));
                aiTracker.setAiEnabled(toRemove, false);
                endSession(toRemove);
            }
        }
    }

    private void handleInterPlayerChat() {
        Collection<FakePlayer> allFakes = fakePlayerManager.getAllFakePlayers();
        List<FakePlayer> aiFakes = new ArrayList<>();
        
        for (FakePlayer fake : allFakes) {
            if (aiTracker.isAiEnabled(fake.getName()) && !aiTracker.isTalkingWithAI(fake.getName())) {
                aiFakes.add(fake);
            }
        }
        
        if (aiFakes.isEmpty()) {
            return;
        }
        
        if (ThreadLocalRandom.current().nextInt(100) < config.getAiSelfChatChance()) {
            if (aiFakes.size() >= 2) {
                FakePlayer speaker = aiFakes.get(ThreadLocalRandom.current().nextInt(aiFakes.size()));
                FakePlayer target = aiFakes.get(ThreadLocalRandom.current().nextInt(aiFakes.size()));
                
                if (speaker != target) {
                    fakePlayerChat(speaker, target.getName());
                }
            } else if (aiFakes.size() == 1) {
                FakePlayer speaker = aiFakes.get(0);
                fakePlayerChat(speaker, "everyone");
            }
        }
    }

    public void onRealPlayerChat(Player player, String message) {
        if (!shouldChat()) {
            return;
        }
        
        Collection<FakePlayer> allFakes = fakePlayerManager.getAllFakePlayers();
        
        for (FakePlayer fake : allFakes) {
            String fakeName = fake.getName();
            
            if (aiTracker.isAiEnabled(fakeName) && !aiTracker.hasFailed(fakeName)) {
                if (!aiTracker.isTalkingWithAI(fakeName)) {
                    if (ThreadLocalRandom.current().nextInt(100) < config.getAiResponseChance()) {
                        respondToPlayer(fake, player.getName(), message);
                    }
                }
            }
        }
    }

    private void respondToPlayer(FakePlayer fake, String playerName, String message) {
        String fakeName = fake.getName();
        String sessionName = getSessionName(fakeName);
        
        aiTracker.setTalkingWithAI(fakeName, true);
        
        ChatSession session = getOrCreateSession(fakeName);
        
        if (session == null) {
            return;
        }
        
        String prompt = "<" + playerName + "> " + message;
        
        session.sendMessage(prompt).thenAccept(response -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                aiTracker.setTalkingWithAI(fakeName, false);
                
                if (!response.isSuccess()) {
                    plugin.getLogger().severe("AI chat error for " + fakeName + ": " + response.getErrorMessage());
                    
                    if (response.getErrorMessage() != null && 
                        (response.getErrorMessage().toLowerCase().contains("rate") ||
                         response.getErrorMessage().toLowerCase().contains("429") ||
                         response.getErrorMessage().toLowerCase().contains("limit"))) {
                        aiTracker.markFailed(fakeName);
                        plugin.getLogger().warning("Marking " + fakeName + " as failed due to rate limiting");
                    }
                    return;
                }
                
                String reply = response.getContent();
                if (reply != null && !reply.isEmpty()) {
                    reply = cleanResponse(reply);
                    if (!reply.isEmpty()) {
                        fakePlayerManager.chat(fake, reply);
                    }
                }
            });
        });
    }

    private void fakePlayerChat(FakePlayer fake, String target) {
        String fakeName = fake.getName();
        String sessionName = getSessionName(fakeName);
        
        aiTracker.setTalkingWithAI(fakeName, true);
        
        ChatSession session = getOrCreateSession(fakeName);
        
        if (session == null) {
            return;
        }
        
        String prompt = "A nearby player says: Hey " + target + ", what's up? Respond naturally.";
        
        session.sendMessage(prompt).thenAccept(response -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                aiTracker.setTalkingWithAI(fakeName, false);
                
                if (!response.isSuccess()) {
                    plugin.getLogger().severe("AI self-chat error for " + fakeName + ": " + response.getErrorMessage());
                    return;
                }
                
                String reply = response.getContent();
                if (reply != null && !reply.isEmpty()) {
                    reply = cleanResponse(reply);
                    if (!reply.isEmpty()) {
                        fakePlayerManager.chat(fake, reply);
                    }
                }
            });
        });
    }

    private ChatSession getOrCreateSession(String fakePlayerName) {
        String sessionName = getSessionName(fakePlayerName);
        
        ChatSession session = sessions.get(sessionName);
        if (session != null) {
            return session;
        }
        
        if (!personalityManager.hasPersonality(fakePlayerName)) {
            plugin.getLogger().warning("No personality found for: " + fakePlayerName);
            return null;
        }
        
        try {
            session = ChatAPI.get().createSession(sessionName, personalityManager.buildFullPrompt(fakePlayerName));
            sessions.put(sessionName, session);
            return session;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create chat session for " + fakePlayerName + ": " + e.getMessage());
            return null;
        }
    }

    private void endSession(String fakePlayerName) {
        String sessionName = getSessionName(fakePlayerName);
        ChatSession session = sessions.remove(sessionName);
        if (session != null) {
            try {
                ChatAPI.get().endSession(sessionName);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private String getSessionName(String fakePlayerName) {
        return "fp_" + fakePlayerName.toLowerCase();
    }

    private String cleanResponse(String response) {
        String cleaned = response.trim();
        
        if (cleaned.length() > 200) {
            int lastPeriod = cleaned.lastIndexOf('.', 200);
            int lastSpace = cleaned.lastIndexOf(' ', 200);
            int cutoff = Math.max(lastPeriod, lastSpace);
            if (cutoff > 50) {
                cleaned = cleaned.substring(0, cutoff + 1);
            } else {
                cleaned = cleaned.substring(0, 200);
            }
        }
        
        cleaned = cleaned.replaceAll("^[\"']|[\"']$", "");
        
        return cleaned.trim();
    }

    public void onFakePlayerRemoved(String fakePlayerName) {
        aiTracker.removePlayer(fakePlayerName);
        endSession(fakePlayerName);
    }

    public void onReload() {
        personalityManager.load();
        
        for (String fakeName : new ArrayList<>(sessions.keySet())) {
            endSession(fakeName.substring(3));
        }
        
        aiTracker.clear();
        
        if (config.isAiEnabled() && isApiAvailable()) {
            start();
        }
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }

    public int getAiEnabledPlayerCount() {
        return aiTracker.getAiEnabledCount();
    }
}
