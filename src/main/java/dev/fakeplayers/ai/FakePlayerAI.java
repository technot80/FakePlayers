package dev.fakeplayers.ai;

import dev.fakeplayers.nms.FakePlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FakePlayerAI {

    private final Set<String> aiEnabledPlayers;
    private final Set<String> failedPlayers;
    private final Map<String, Long> talkingWithAI;

    public FakePlayerAI() {
        this.aiEnabledPlayers = ConcurrentHashMap.newKeySet();
        this.failedPlayers = ConcurrentHashMap.newKeySet();
        this.talkingWithAI = new ConcurrentHashMap<>();
    }

    public void setAiEnabled(String playerName, boolean enabled) {
        if (enabled) {
            aiEnabledPlayers.add(playerName);
            failedPlayers.remove(playerName);
        } else {
            aiEnabledPlayers.remove(playerName);
            talkingWithAI.remove(playerName);
        }
    }

    public boolean isAiEnabled(String playerName) {
        return aiEnabledPlayers.contains(playerName);
    }

    public boolean hasFailed(String playerName) {
        return failedPlayers.contains(playerName);
    }

    public void markFailed(String playerName) {
        failedPlayers.add(playerName);
        aiEnabledPlayers.remove(playerName);
        talkingWithAI.remove(playerName);
    }

    public void setTalkingWithAI(String playerName, boolean talking) {
        if (talking) {
            talkingWithAI.put(playerName, System.currentTimeMillis());
        } else {
            talkingWithAI.remove(playerName);
        }
    }

    public boolean isTalkingWithAI(String playerName) {
        return talkingWithAI.containsKey(playerName);
    }

    public void removePlayer(String playerName) {
        aiEnabledPlayers.remove(playerName);
        failedPlayers.remove(playerName);
        talkingWithAI.remove(playerName);
    }

    public void clear() {
        aiEnabledPlayers.clear();
        failedPlayers.clear();
        talkingWithAI.clear();
    }

    public void clearTalkingStatus() {
        talkingWithAI.clear();
    }

    public int getAiEnabledCount() {
        return aiEnabledPlayers.size();
    }

    public Set<String> getAiEnabledPlayers() {
        return new HashSet<>(aiEnabledPlayers);
    }

    public boolean isAnyTalkingWithAI() {
        return !talkingWithAI.isEmpty();
    }

    public int clearStuckTalkers(long timeoutMillis) {
        int cleared = 0;
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : talkingWithAI.entrySet()) {
            if (now - entry.getValue() > timeoutMillis) {
                talkingWithAI.remove(entry.getKey());
                cleared++;
            }
        }
        return cleared;
    }
}
