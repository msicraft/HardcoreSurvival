package me.msicraft.hardcoresurvival.Guild.Data;

import me.msicraft.hardcoresurvival.PlayerData.Data.OfflinePlayerData;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class Guild {

    private final OfflinePlayer leaderPlayer;
    private final List<UUID> members = new ArrayList<>();

    private final Map<UUID, Long> tempKickMap = new HashMap<>();

    private int inviteCount;

    public Guild(OfflinePlayerData offlinePlayerData) {
        this.leaderPlayer = offlinePlayerData.getOfflinePlayer();

        FileConfiguration config = offlinePlayerData.getPlayerDataFile().getConfig();
        this.inviteCount = config.getInt("Guild.InviteCount", 0);
        config.getStringList("Guild.MemberList").forEach(s -> {
            UUID uuid = UUID.fromString(s);
            if (!members.contains(uuid)) {
                members.add(uuid);
            }
        });
        config.getStringList("Guild.TempKickList").forEach(format -> {
            String[] split = format.split(":");
            UUID target = UUID.fromString(split[0]);
            long time = Long.parseLong(split[1]);
            tempKickMap.put(target, time);
        });
    }

    public void applyTempKick(UUID target, int seconds) {
        long time = System.currentTimeMillis() + (seconds * 1000L);
        if (tempKickMap.containsKey(target)) {
            time = tempKickMap.get(target) + (seconds * 1000L);
        }
        tempKickMap.put(target, time);
    }

    public void removeTempKickMember(UUID target) {
        tempKickMap.remove(target);
    }

    public boolean isTempKickMember(UUID target) {
        return tempKickMap.containsKey(target) && tempKickMap.get(target) > System.currentTimeMillis();
    }

    public long getTempKickTime(UUID target) {
        return tempKickMap.getOrDefault(target, 0L);
    }

    public Set<UUID> getTempKickList() {
        return tempKickMap.keySet();
    }

    public OfflinePlayer getLeaderPlayer() {
        return leaderPlayer;
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public List<UUID> getMembers() {
        return members;
    }

    public int getInviteCount() {
        return inviteCount;
    }

    public void addInviteCount(int amount) {
        inviteCount = inviteCount + amount;
        if (inviteCount < 0) {
            inviteCount = 0;
        }
    }

    public void disband() {
    }

}
