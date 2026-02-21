package dev.fakeplayers.nms;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry;
import net.minecraft.world.level.GameType;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class FakePlayer {

    private final String name;
    private final UUID uuid;
    private final GameProfile profile;
    private boolean valid = true;

    public FakePlayer(String name) {
        this.name = name;
        this.uuid = UUID.nameUUIDFromBytes(("FakePlayer:" + name).getBytes());
        this.profile = new GameProfile(uuid, name);
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public GameProfile getProfile() {
        return profile;
    }

    public void invalidate() {
        this.valid = false;
    }

    public boolean isValid() {
        return valid;
    }

    private static Entry createEntry(FakePlayer fp) {
        return new Entry(
            fp.getUuid(),
            fp.getProfile(),
            true,
            0,
            GameType.SURVIVAL,
            null,
            true,
            0,
            null
        );
    }

    public static void broadcastAddToAll(Collection<FakePlayer> players) {
        if (players.isEmpty()) return;

        List<Entry> entries = new ArrayList<>();
        for (FakePlayer fp : players) {
            entries.add(createEntry(fp));
        }

        EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = EnumSet.of(
            ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
            ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_HAT,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LIST_ORDER
        );

        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(actions, entries);

        for (Player online : Bukkit.getOnlinePlayers()) {
            net.minecraft.server.level.ServerPlayer nmsPlayer = ((CraftPlayer) online).getHandle();
            if (nmsPlayer.connection != null) {
                nmsPlayer.connection.send(packet);
            }
        }
    }

    public static void broadcastRemoveFromAll(Collection<FakePlayer> players) {
        if (players.isEmpty()) return;

        List<UUID> uuids = new ArrayList<>();
        for (FakePlayer fp : players) {
            uuids.add(fp.getUuid());
        }
        ClientboundPlayerInfoRemovePacket packet = new ClientboundPlayerInfoRemovePacket(uuids);

        for (Player online : Bukkit.getOnlinePlayers()) {
            net.minecraft.server.level.ServerPlayer nmsPlayer = ((CraftPlayer) online).getHandle();
            if (nmsPlayer.connection != null) {
                nmsPlayer.connection.send(packet);
            }
        }
    }

    public static void sendAddToPlayer(Player player, Collection<FakePlayer> players) {
        if (players.isEmpty()) return;

        List<Entry> entries = new ArrayList<>();
        for (FakePlayer fp : players) {
            entries.add(createEntry(fp));
        }

        EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = EnumSet.of(
            ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
            ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_HAT,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LIST_ORDER
        );

        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(actions, entries);

        net.minecraft.server.level.ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        if (nmsPlayer.connection != null) {
            nmsPlayer.connection.send(packet);
        }
    }
}
