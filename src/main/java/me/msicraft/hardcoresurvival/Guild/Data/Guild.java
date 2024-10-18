package me.msicraft.hardcoresurvival.Guild.Data;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.OfflinePlayerData;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class Guild {

    private final OfflinePlayer leaderPlayer;
    private final List<UUID> members = new ArrayList<>();

    private final Map<UUID, Long> tempKickMap = new HashMap<>();

    private int inviteCount = 0;

    public Guild(OfflinePlayer leaderPlayer) {
        this.leaderPlayer = leaderPlayer;
        members.add(leaderPlayer.getUniqueId());
    }

    public void loadGuildData() {
        if (leaderPlayer.isOnline()) {
            return;
        }
        OfflinePlayerData offlinePlayerData = new OfflinePlayerData(leaderPlayer);
        offlinePlayerData.loadData();
    }

    public void saveGuildData() {
    }

    public void applyTempKickMember(UUID target, int seconds) {
        long time = System.currentTimeMillis() + (seconds * 1000L);
        if (tempKickMap.containsKey(target)) {
            time = time + tempKickMap.get(target);
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
