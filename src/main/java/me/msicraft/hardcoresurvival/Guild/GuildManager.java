package me.msicraft.hardcoresurvival.Guild;

import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.Guild.Data.GuildDataFile;
import me.msicraft.hardcoresurvival.Guild.Data.GuildRegion;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.PlayerData.PlayerDataManager;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import me.msicraft.hardcoresurvival.Utils.TimeUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GuildManager {

    private final HardcoreSurvival plugin;
    private final Map<UUID, Guild> guildMap = new ConcurrentHashMap<>();

    private final Map<Integer, Integer> regionPriceMap = new HashMap<>();
    private final Map<Integer, Double> shopPenaltyMap = new HashMap<>();
    private final Set<String> regionWorldNames = new HashSet<>();

    private int maxInviteCount = -1;
    private int regionProtectRadius = 150;
    private int regionPaySeconds = 86400;

    private int prefixChangePrice = -1;
    private int baseInvitePrice;
    private int perInvitePrice;

    public GuildManager(HardcoreSurvival plugin) {
        this.plugin = plugin;

        File file = new File(plugin.getDataFolder() + File.separator + GuildDataFile.FOLDER_NAME);
        if (!file.exists()) {
            file.mkdirs();
        }
        String[] fileNames = file.list();
        if (fileNames != null) {
            for (String fileName : fileNames) {
                if (fileName.endsWith(".yml")) {
                    fileName = fileName.replace(".yml", "");
                    UUID uuid = UUID.fromString(fileName);
                    Guild guild = Guild.loadGuild(uuid);
                    guildMap.put(uuid, guild);
                }
            }
            if (plugin.useDebug()) {
                MessageUtil.sendDebugMessage("Load-Guild", "Loaded " + guildMap.size() + " Guild");
            }
        } else {
            if (plugin.useDebug()) {
                MessageUtil.sendDebugMessage("Empty-Guild", "None Guild");
            }
        }
    }

    public void reloadVariables() {
        FileConfiguration mainConfig = plugin.getConfig();
        this.maxInviteCount = mainConfig.getInt("Streamer.MaxInviteCount", -1);
        this.regionProtectRadius = mainConfig.getInt("Streamer.RegionProtectRadius", 150);
        this.regionPaySeconds = mainConfig.getInt("Streamer.RegionPaySeconds", 86400);

        this.baseInvitePrice = mainConfig.getInt("Streamer.BaseInvitePrice", 200000);
        this.perInvitePrice = mainConfig.getInt("Streamer.PerInvitePrice", 10000);
        this.prefixChangePrice = mainConfig.getInt("Streamer.PrefixChangePrice", -1);

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

    public void saveGuild() {
        Set<UUID> guildIds = guildMap.keySet();
        for (UUID uuid : guildIds) {
            Guild guild = guildMap.get(uuid);
            guild.save();
        }

        if (plugin.useDebug()) {
            MessageUtil.sendDebugMessage("Guild saved", "Size: " + guildMap.size());
        }
    }

    public Guild createGuild(UUID leader) {
        UUID guildUUID = UUID.randomUUID();
        Guild guild = Guild.createGuild(guildUUID, leader);
        registerGuild(guild);
        return guild;
    }

    public void buyRegion(Player player, Guild guild, Location location, double balance) {
        GuildRegion guildRegion = guild.getGuildRegion();
        Chunk chunk = location.getChunk();
        PersistentDataContainer chunkData = chunk.getPersistentDataContainer();
        chunkData.set(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING, guild.getGuildUUID().toString());

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

    private void registerGuild(Guild guild) {
        guildMap.put(guild.getGuildUUID(), guild);
    }

    public Guild getGuild(UUID guildUUID) {
        if (guildUUID == null) {
            return null;
        }
        if (guildMap.containsKey(guildUUID)) {
            return guildMap.get(guildUUID);
        }
        return null;
    }

    public Guild getGuild(Player player) {
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        if (playerData != null) {
            return getGuild(playerData.getGuildUUID());
        }
        return null;
    }

    public void removeGuild(UUID guildUUID) {
        guildMap.remove(guildUUID);
        if (guildMap.containsKey(guildUUID)) {
            Guild guild = guildMap.get(guildUUID);
            guild.getMembers().forEach(memberUUID -> {
                OfflinePlayer memberO = Bukkit.getOfflinePlayer(memberUUID);
                if (memberO.isOnline()) {
                    memberO.getPlayer().kick(Component.text(ChatColor.RED + "길드가 해체되었습니다"));
                }
                PlayerData memberData = plugin.getPlayerDataManager().getPlayerData(memberUUID);
                memberData.setGuildUUID(null);
            });
            guild.getGuildRegion().getGuildChunks().forEach(guildChunk -> {
                Location location = plugin.getWorldManager().getCenterChunkLocation(guildChunk.getWorldName(),
                        guildChunk.getChunkPair().getV1(), guildChunk.getChunkPair().getV2());
                Chunk chunk = location.getChunk();
                PersistentDataContainer dataContainer = chunk.getPersistentDataContainer();
                if (dataContainer.has(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING)) {
                    dataContainer.remove(GuildRegion.GUILD_REGION_KEY);
                }
            });
        }
    }

    public boolean inviteGuild(Player leader, OfflinePlayer offlinePlayer) {
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        UUID targetUUID = offlinePlayer.getUniqueId();
        PlayerData playerData = playerDataManager.getPlayerData(targetUUID);
        if (playerData == null) {
            leader.sendMessage(Component.text(ChatColor.RED + "존재하지 않는 플레이어입니다"));
            return false;
        }

        Guild guild = getGuild(leader);
        if (guild == null) {
            return false;
        }
        guild.addMember(targetUUID);
        guild.addInviteCount(1);

        playerData.setGuildUUID(guild.getGuildUUID());

        leader.sendMessage(ChatColor.GREEN + "해당 플레이어를 길드에 초대하였습니다");
        leader.sendMessage(ChatColor.GREEN + "플레이어: " + playerData.getLastName());
        return true;
    }

    public void kickGuild(Player leader, OfflinePlayer target) {
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        if (target.isOnline()) {
            target.getPlayer().kick(Component.text("길드장에 의해 추방당하였습니다"));
        }
        PlayerData playerData = playerDataManager.getPlayerData(target.getUniqueId());
        playerData.setGuildUUID(null);

        Guild guild = getGuild(leader);
        guild.removeMember(target.getUniqueId());
        guild.addInviteCount(-1);

        leader.sendMessage(ChatColor.GREEN + "길드에서 추방하였습니다");
        leader.sendMessage(ChatColor.GREEN + "플레이어: " + target.getName());
    }

    public void tempKickGuild(Player leader, OfflinePlayer target, int seconds) {
        UUID targetUUID = target.getUniqueId();
        Guild guild = getGuild(leader);
        if (guild == null) {
            leader.sendMessage(ChatColor.RED + "잘못된 {g-uuid} 입니다");
            return;
        }
        if (seconds == -1) {
            if (guild.isTempKickMember(targetUUID)) {
                guild.removeTempKickMember(targetUUID);
                leader.sendMessage(ChatColor.GREEN + "해당 플레이어의 임시 추방이 취소되었습니다");
                leader.sendMessage(ChatColor.GREEN + "플레이어: " + target.getName());
            } else {
                leader.sendMessage(ChatColor.RED + "해당 플레이어는 임시 추방 상태가 아닙니다");
            }
            return;
        }
        guild.applyTempKick(targetUUID, seconds);
        String leftTime = TimeUtil.getTimeToFormat(guild.getTempKickTime(targetUUID));
        if (target.isOnline()) {
            target.getPlayer().kick(Component.text("길드장에 의해 임시 추방당하였습니다\n만료 기간: " + leftTime));
        }

        leader.sendMessage(ChatColor.GREEN + "해당 플레이어가 임시추방 되었습니다");
        leader.sendMessage(ChatColor.GREEN + "플레이어: " + target.getName());
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

    public boolean isInOwnGuildRegion(Location location, Player checkPlayer, boolean checkExpired) {
        Chunk chunk = location.getChunk();
        PersistentDataContainer dataContainer = chunk.getPersistentDataContainer();
        if (dataContainer.has(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING)) {
            String guildRegionKey = dataContainer.get(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING);
            if (guildRegionKey != null) {
                Guild guild = getGuild(UUID.fromString(guildRegionKey));
                if (guild != null) {
                    if (checkExpired) {
                        return guild.isMember(checkPlayer) && guild.getGuildRegion().getOverdueDay(false) == 0;
                    } else {
                        return guild.isMember(checkPlayer);
                    }
                }
            }
        }
        return false;
    }

    public boolean isInOwnGuildRegion(Player player) {
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

    public boolean isGuildRegion(Location location, boolean expired) {
        Chunk chunk = location.getChunk();
        PersistentDataContainer dataContainer = chunk.getPersistentDataContainer();
        if (dataContainer.has(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING)) {
            String guildRegionKey = dataContainer.get(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING);
            if (guildRegionKey != null) {
                Guild guild = getGuild(UUID.fromString(guildRegionKey));
                if (guild != null) {
                    if (expired) {
                        return guild.getGuildRegion().getOverdueDay(false) == 0;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public Set<UUID> getGuildUUIDs() {
        return guildMap.keySet();
    }

    public int getBaseInvitePrice() {
        return baseInvitePrice;
    }

    public int getPerInvitePrice() {
        return perInvitePrice;
    }

    public int getPrefixChangePrice() {
        return prefixChangePrice;
    }

}
