package me.msicraft.hardcoresurvival.Guild;

import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.OfflinePlayerData;
import me.msicraft.hardcoresurvival.PlayerData.PlayerDataManager;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import me.msicraft.hardcoresurvival.Utils.TimeUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class GuildManager {

    private final HardcoreSurvival plugin;
    private final Map<UUID, Guild> guildMap = new HashMap<>(); //leader-uuid, guild

    private int maxInviteCount = -1;

    public GuildManager(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    public void reloadVariables() {
        FileConfiguration mainConfig = plugin.getConfig();
        this.maxInviteCount = mainConfig.getInt("Streamer.MaxInviteCount", -1);
    }

    public void loadGuild() {
        plugin.getPlayerDataManager().getStreamerList().forEach(uuid -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            OfflinePlayerData offlinePlayerData = new OfflinePlayerData(offlinePlayer);
            Guild guild = new Guild(offlinePlayerData);
            guildMap.put(uuid, guild);
        });

        if (plugin.useDebug()) {
            MessageUtil.sendDebugMessage("Guild loaded", "Size: " + guildMap.size());
        }
    }

    public void saveGuild() {
        Set<UUID> guildIds = guildMap.keySet();
        for (UUID uuid : guildIds) {
            Guild guild = guildMap.get(uuid);
            OfflinePlayer offlinePlayer = guild.getLeaderPlayer();
            OfflinePlayerData offlinePlayerData = new OfflinePlayerData(offlinePlayer);
            FileConfiguration config = offlinePlayerData.getPlayerDataFile().getConfig();
            config.set("Guild.InviteCount", guild.getInviteCount());

            List<String> memberList = new ArrayList<>();
            guild.getMembers().forEach(memberUUID -> {
                memberList.add(memberUUID.toString());
            });
            config.set("Guild.MemberList", memberList);

            List<String> tempKickFormatList = new ArrayList<>();
            guild.getTempKickList().forEach(tempKickUUID -> {
                long time = guild.getTempKickTime(tempKickUUID);
                String format = tempKickUUID + ":" + time;
                tempKickFormatList.add(format);
            });
            config.set("Guild.TempKickList", tempKickFormatList);

            offlinePlayerData.getPlayerDataFile().saveConfig();
        }

        if (plugin.useDebug()) {
            MessageUtil.sendDebugMessage("Guild saved", "Size: " + guildMap.size());
        }
    }

    public int getMaxInviteCount() {
        return maxInviteCount;
    }

    public void registerGuild(UUID leaderUUID, Guild guild) {
        guildMap.put(leaderUUID, guild);
    }

    public Guild getGuild(UUID leaderUUID) {
        return guildMap.getOrDefault(leaderUUID, null);
    }

    public void removeGuild(UUID leaderUUID) {
        guildMap.remove(leaderUUID);
    }

    public boolean inviteGuild(Player leader, OfflinePlayer viewer) {
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        if (playerDataManager.hasWhiteList(viewer)) {
            leader.sendMessage(ChatColor.RED + "이미 등록된 플레이어입니다");
            return false;
        }
        OfflinePlayerData offlinePlayerData = new OfflinePlayerData(viewer);
        offlinePlayerData.loadData();
        offlinePlayerData.setGuildUUID(leader.getUniqueId());
        offlinePlayerData.saveData();

        plugin.getPlayerDataManager().addWhiteList(viewer.getUniqueId());

        Guild guild = getGuild(leader.getUniqueId());
        guild.addMember(viewer.getUniqueId());

        leader.sendMessage(ChatColor.GREEN + "접속권한이 추가되었습니다");
        leader.sendMessage(ChatColor.GREEN + "Player: " + viewer.getName());
        return true;
    }

    public void kickGuild(Player leader, OfflinePlayer target) {
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        if (!playerDataManager.hasWhiteList(target)) {
            leader.sendMessage(ChatColor.RED + "이미 접속권한이 없는 플레이어입니다");
            return;
        }
        if (target.isOnline()) {
            target.getPlayer().kick(Component.text("스트리머에 의해 추방당하였습니다"));
        }
        playerDataManager.removeWhiteList(target.getUniqueId());
        OfflinePlayerData offlinePlayerData = new OfflinePlayerData(target);
        offlinePlayerData.loadData();
        offlinePlayerData.setGuildUUID(null);
        offlinePlayerData.saveData();

        plugin.getPlayerDataManager().removeWhiteList(target.getUniqueId());

        Guild guild = getGuild(leader.getUniqueId());
        guild.removeMember(target.getUniqueId());

        leader.sendMessage(ChatColor.GREEN + "접속권한이 제거되었습니다");
        leader.sendMessage(ChatColor.GREEN + "Player: " + target.getName());
    }

    public void tempKickGuild(Player leader, OfflinePlayer target, int seconds) {
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        if (!playerDataManager.hasWhiteList(target)) {
            leader.sendMessage(ChatColor.RED + "이미 접속권한이 없는 플레이어입니다");
            return;
        }
        UUID targetUUID = target.getUniqueId();
        Guild guild = getGuild(leader.getUniqueId());
        if (guild == null) {
            leader.sendMessage(ChatColor.RED + "잘못된 {g-uuid} 입니다");
            return;
        }
        if (guild.isTempKickMember(targetUUID)) {
            if (seconds == -1) {
                guild.removeTempKickMember(targetUUID);
                leader.sendMessage(ChatColor.GREEN + "해당 플레이어의 임시 추방이 취소되었습니다");
                leader.sendMessage(ChatColor.GREEN + "Player: " + target.getName());
                return;
            }
        }
        guild.applyTempKick(targetUUID, seconds);
        String leftTime = TimeUtil.getTimeToFormat(guild.getTempKickTime(targetUUID));
        if (target.isOnline()) {
            target.getPlayer().kick(Component.text("스트리머에 의해 임시 추방당하였습니다\n만료 기간: " + leftTime));
        }

        leader.sendMessage(ChatColor.GREEN + "해당 플레이어가 임시추방 되었습니다");
        leader.sendMessage(ChatColor.GREEN + "Player: " + target.getName());
        leader.sendMessage(ChatColor.GREEN + "만료 기간: " + leftTime);
    }

}
