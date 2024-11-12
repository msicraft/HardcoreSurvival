package me.msicraft.hardcoresurvival.Guild;

import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.Guild.Data.GuildRegion;
import me.msicraft.hardcoresurvival.Guild.Data.GuildSpawnLocation;
import me.msicraft.hardcoresurvival.Guild.Data.RegionOptions;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.OfflinePlayerData;
import me.msicraft.hardcoresurvival.PlayerData.File.PlayerDataFile;
import me.msicraft.hardcoresurvival.PlayerData.PlayerDataManager;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import me.msicraft.hardcoresurvival.Utils.TimeUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GuildManager {

    private final HardcoreSurvival plugin;
    private final Map<UUID, Guild> guildMap = new HashMap<>(); //leader-uuid, guild

    private final Map<Integer, Integer> regionPriceMap = new HashMap<>();
    private final Map<Integer, Double> shopPenaltyMap = new HashMap<>();
    private final Set<String> regionWorldNames = new HashSet<>();

    private int maxInviteCount = -1;
    private int regionProtectRadius = 150;
    private int regionPaySeconds = 86400;

    public GuildManager(HardcoreSurvival plugin) {
        this.plugin = plugin;

        new BukkitRunnable() {
            private int count = 0;
            @Override
            public void run() {
                Set<UUID> guildUUIDs = guildMap.keySet();
                for (UUID uuid : guildUUIDs) {
                    Guild guild = guildMap.get(uuid);
                    GuildSpawnLocation guildSpawnLocation = guild.getGuildRegion().getGuildSpawnLocation();
                    if (guildSpawnLocation.getSpawnLocation() == null) {
                        guildSpawnLocation.update();
                    }
                }
                count++;
                if (count > 60) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 100L, 100L);
    }

    public void reloadVariables() {
        FileConfiguration mainConfig = plugin.getConfig();
        this.maxInviteCount = mainConfig.getInt("Streamer.MaxInviteCount", -1);
        this.regionProtectRadius = mainConfig.getInt("Streamer.RegionProtectRadius", 150);
        this.regionPaySeconds = mainConfig.getInt("Streamer.RegionPaySeconds", 86400);

        regionWorldNames.addAll(mainConfig.getStringList("Streamer.RegionWorld"));

        regionPriceMap.clear();
        ConfigurationSection regionPriceSection = mainConfig.getConfigurationSection("Streamer.RegionPrice");
        if (regionPriceSection != null) {
            regionPriceSection.getKeys(false).forEach(s -> {
                String path = "Streamer.RegionPrice." + s;
                int price = mainConfig.getInt(path);
                regionPriceMap.put(Integer.parseInt(s), price);
            });
        }

        shopPenaltyMap.clear();
        ConfigurationSection shopPenaltySection = mainConfig.getConfigurationSection("Streamer.ShopPenalty");
        if (shopPenaltySection != null) {
            shopPenaltySection.getKeys(false).forEach(s -> {
                String path = "Streamer.ShopPenalty." + s;
                double penalty = mainConfig.getDouble(path);
                shopPenaltyMap.put(Integer.parseInt(s), penalty);
            });
        }

        if (plugin.useDebug()) {
            MessageUtil.sendDebugMessage("GuildRegionPrice", "Size: " + regionPriceMap.size());
            MessageUtil.sendDebugMessage("GuildRegion-ShopPenalty", "Size: " + shopPenaltyMap.size());
        }
    }

    public void loadGuild() {
        plugin.getPlayerDataManager().getStreamerList().forEach(uuid -> {
            PlayerDataFile playerDataFile = new PlayerDataFile(uuid);
            Guild guild = new Guild(uuid, playerDataFile);
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

            OfflinePlayerData offlinePlayerData = new OfflinePlayerData(uuid);
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

            GuildRegion guildRegion = guild.getGuildRegion();
            guildRegion.worldNameSets().forEach(s -> {
                List<String> regionFormatList = guildRegion.getBuyRegionToFormatList(s);
                config.set("Guild.Region.BuyRegion." + s, regionFormatList);
            });

            config.set("Guild.Region.LastRegionPayTime", guildRegion.getLastRegionPayTime());

            GuildSpawnLocation guildSpawnLocation = guildRegion.getGuildSpawnLocation();
            config.set("Guild.Region.SpawnLocation.WorldName", guildSpawnLocation.getSpawnWorldName());
            config.set("Guild.Region.SpawnLocation.X", guildSpawnLocation.getSpawnX());
            config.set("Guild.Region.SpawnLocation.Y", guildSpawnLocation.getSpawnY());
            config.set("Guild.Region.SpawnLocation.Z", guildSpawnLocation.getSpawnZ());

            RegionOptions[] regionOptions = RegionOptions.values();
            for (RegionOptions option : regionOptions) {
                Object o = guildRegion.getRegionOption(option);
                String path = "Guild.Region.RegionOptions." + option.name();
                config.set(path, o);
            }

            offlinePlayerData.getPlayerDataFile().saveConfig();
        }

        if (plugin.useDebug()) {
            MessageUtil.sendDebugMessage("Guild saved", "Size: " + guildMap.size());
        }
    }

    public void buyRegion(Player player, Guild guild, Location location, double balance) {
        GuildRegion guildRegion = guild.getGuildRegion();
        Chunk chunk = location.getChunk();
        PersistentDataContainer chunkData = chunk.getPersistentDataContainer();
        chunkData.set(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING, guild.getLeader().toString());

        plugin.getEconomy().withdrawPlayer(player, balance);
        if (guildRegion.getLastRegionPayTime() == -1) {
            guildRegion.setLastRegionPayTime(System.currentTimeMillis());
        }
        guildRegion.addRegion(location.getWorld().getName(), chunk);
    }

    public void removeRegion(Guild guild, Location location) {
        Chunk chunk = location.getChunk();
        removeRegion(guild, location.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public void removeRegion(Guild guild, String worldName, int chunkX, int chunkZ) {
        guild.getGuildRegion().removeRegion(worldName, chunkX, chunkZ);

        Location location = plugin.getWorldManager().getCenterChunkLocation(worldName, chunkX, chunkZ);
        Chunk chunk = location.getChunk();
        PersistentDataContainer chunkData = chunk.getPersistentDataContainer();
        if (chunkData.has(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING)) {
            chunkData.remove(GuildRegion.GUILD_REGION_KEY);
        }
    }

    public boolean canBuyRegionInWorld(Location location) {
        return regionWorldNames.contains(location.getWorld().getName());
    }

    public int getRegionPrice(int count) {
        return regionPriceMap.getOrDefault(count, 0);
    }

    public double getShopPenalty(int count) {
        return shopPenaltyMap.getOrDefault(count, 0.0);
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
        OfflinePlayerData offlinePlayerData = new OfflinePlayerData(viewer.getUniqueId());
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
        OfflinePlayerData offlinePlayerData = new OfflinePlayerData(target.getUniqueId());
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
        if (seconds == -1) {
            if (guild.isTempKickMember(targetUUID)) {
                guild.removeTempKickMember(targetUUID);
                leader.sendMessage(ChatColor.GREEN + "해당 플레이어의 임시 추방이 취소되었습니다");
                leader.sendMessage(ChatColor.GREEN + "Player: " + target.getName());
            } else {
                leader.sendMessage(ChatColor.RED + "해당 플레이어는 임시 추방 상태가 아닙니다");
            }
            return;
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

    public int getRegionProtectRadius() {
        return regionProtectRadius;
    }

    public int getRegionPaySeconds() {
        return regionPaySeconds;
    }

    public int getMaxRegionPrice() {
        return regionPriceMap.size();
    }

    public Set<Integer> getRegionPriceKeySet() {
        return regionPriceMap.keySet();
    }

    public int getMaxShopPenalty() {
        return shopPenaltyMap.size();
    }

    public Set<Integer> getShopPenaltyKeySet() {
        return shopPenaltyMap.keySet();
    }

    public boolean isInGuildRegion(Player player) {
        Location location = player.getLocation();
        Chunk chunk = location.getChunk();
        PersistentDataContainer dataContainer = chunk.getPersistentDataContainer();
        if (dataContainer.has(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING)) {
            String guildRegionKey = dataContainer.get(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING);
            if (guildRegionKey != null) {
                Guild guild = getGuild(UUID.fromString(guildRegionKey));
                if (guild != null) {
                    return guild.isMember(player);
                }
            }
        }
        return false;
    }

}
