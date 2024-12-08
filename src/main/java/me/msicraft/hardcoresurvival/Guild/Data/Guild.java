package me.msicraft.hardcoresurvival.Guild.Data;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class Guild {

    private final UUID guildUUID;
    private final UUID leader;
    private final GuildDataFile guildDataFile;

    private final List<UUID> members = new ArrayList<>();
    private final Set<UUID> subLeaders = new HashSet<>();

    private final Map<UUID, Long> tempKickMap = new HashMap<>();

    private String prefix = null;
    private int inviteCount;

    private final GuildRegion guildRegion;

    public static Guild createGuild(UUID guildUUID, UUID leader) {
        return new Guild(guildUUID, leader);
    }

    public static Guild loadGuild(UUID guildUUId) {
        return new Guild(guildUUId);
    }

    private Guild(UUID guildUUID, UUID leader) {
        this.guildUUID = guildUUID;
        this.leader = leader;

        this.guildDataFile = new GuildDataFile(guildUUID);
        this.guildRegion = new GuildRegion(guildDataFile);

        this.inviteCount = 0;

        members.add(leader);
    }

    private Guild(UUID guildUUID) {
        this.guildUUID = guildUUID;
        this.guildDataFile = new GuildDataFile(guildUUID);

        this.guildRegion = new GuildRegion(guildDataFile);

        FileConfiguration config = guildDataFile.getConfig();
        this.leader = UUID.fromString(config.getString("Leader"));

        String prefix = config.getString("Prefix", null);
        if (prefix != null) {
            setPrefix(MessageUtil.translateColorCodes(prefix));
        }

        this.inviteCount = config.getInt("InviteCount", 0);

        config.getStringList("MemberList").forEach(s -> {
            UUID uuid = UUID.fromString(s);
            if (!members.contains(uuid)) {
                members.add(uuid);
            }
        });
        config.getStringList("TempKickList").forEach(format -> {
            String[] split = format.split(":");
            UUID target = UUID.fromString(split[0]);
            long time = Long.parseLong(split[1]);
            tempKickMap.put(target, time);
        });
        config.getStringList("SubLeader").forEach(s -> {
            UUID uuid = UUID.fromString(s);
            subLeaders.add(uuid);
        });
    }

    public void save() {
        FileConfiguration config = guildDataFile.getConfig();

        config.set("InviteCount", this.inviteCount);
        config.set("Prefix", prefix);
        config.set("Leader", leader.toString());

        List<String> memberList = new ArrayList<>();
        getMembers().forEach(memberUUID -> {
            memberList.add(memberUUID.toString());
        });
        config.set("MemberList", memberList);

        List<String> tempKickFormatList = new ArrayList<>();
        getTempKickList().forEach(tempKickUUID -> {
            long time = getTempKickTime(tempKickUUID);
            String format = tempKickUUID + ":" + time;
            tempKickFormatList.add(format);
        });
        config.set("TempKickList", tempKickFormatList);

        config.set("SubLeader", List.copyOf(subLeaders));

        GuildRegion guildRegion = getGuildRegion();
        guildRegion.worldNameSets().forEach(s -> {
            List<String> regionFormatList = guildRegion.getBuyRegionToFormatList(s);
            config.set("Region.BuyRegion." + s, regionFormatList);
        });

        config.set("Region.LastRegionPayTime", guildRegion.getLastRegionPayTime());

        GuildSpawnLocation guildSpawnLocation = guildRegion.getGuildSpawnLocation();
        config.set("Region.SpawnLocation.WorldName", guildSpawnLocation.getSpawnWorldName());
        config.set("Region.SpawnLocation.X", guildSpawnLocation.getSpawnX());
        config.set("Region.SpawnLocation.Y", guildSpawnLocation.getSpawnY());
        config.set("Region.SpawnLocation.Z", guildSpawnLocation.getSpawnZ());

        RegionOptions[] regionOptions = RegionOptions.values();
        for (RegionOptions option : regionOptions) {
            Object o = guildRegion.getRegionOption(option);
            String path = "Region.RegionOptions." + option.name();
            config.set(path, o);
        }

        guildDataFile.saveConfig();
    }

    public UUID getGuildUUID() {
        return guildUUID;
    }

    public GuildDataFile getGuildDataFile() {
        return guildDataFile;
    }

    public boolean isLeader(UUID uuid) {
        return leader.equals(uuid);
    }

    public boolean isLeader(Player player) {
        return isLeader(player.getUniqueId());
    }

    public boolean isSubLeader(UUID uuid) {
        return subLeaders.contains(uuid);
    }

    public boolean isSubLeader(Player player) {
        return isSubLeader(player.getUniqueId());
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

    public UUID getLeader() {
        return leader;
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public boolean isMember(Player player) {
        return isMember(player.getUniqueId());
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public List<UUID> getMembers() {
        return members;
    }

    public int getInviteCount() {
        return inviteCount;
    }

    public void setInviteCount(int inviteCount) {
        this.inviteCount = inviteCount;
    }

    public void addInviteCount(int amount) {
        inviteCount = inviteCount + amount;
        if (inviteCount < 0) {
            inviteCount = 0;
        }
    }

    public void disband() {
    }

    public GuildRegion getGuildRegion() {
        return guildRegion;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getLeftInviteCount() {
        int max = HardcoreSurvival.getPlugin().getGuildManager().getMaxInviteCount();
        if (max == -1) {
            return -1;
        }
        int cal = max - inviteCount;
        if (cal < 0) {
            cal = 0;
        }
        return cal;
    }

    public int getInvitePrice() {
        int memberSize = members.size();
        if (memberSize == 0 || memberSize == 1
                || memberSize == 2 || memberSize == 3) {
            return 0;
        }
        return HardcoreSurvival.getPlugin().getGuildManager().getBaseInvitePrice() +
                (HardcoreSurvival.getPlugin().getGuildManager().getPerInvitePrice() * (memberSize - 3));
    }

}
