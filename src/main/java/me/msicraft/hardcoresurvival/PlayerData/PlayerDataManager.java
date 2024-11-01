package me.msicraft.hardcoresurvival.PlayerData;

import me.msicraft.hardcoresurvival.API.CustomEvent.PlayerDataUnLoadEvent;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.GuiUtil;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final HardcoreSurvival plugin;

    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    private final List<UUID> streamerList = new ArrayList<>();
    private final List<UUID> whiteList = new ArrayList<>();

    private boolean useWhiteList = false;
    private String whitelistMessage = ChatColor.RED + "접속 권한이 없습니다";

    public PlayerDataManager(HardcoreSurvival plugin) {
        this.plugin = plugin;

        plugin.getConfig().getStringList("Streamer.List").forEach(s -> {
            UUID uuid = UUID.fromString(s);
            streamerList.add(uuid);
        });

        List<String> uuidList = plugin.getConfig().getStringList("Whitelist.List");
        for (String uuidString : uuidList) {
            UUID uuid = UUID.fromString(uuidString);
            whiteList.add(uuid);
        }
    }

    public void reloadVariables() {
        this.useWhiteList = plugin.getConfig().contains("Whitelist.Enabled") && plugin.getConfig().getBoolean("Whitelist.Enabled");
        String wMessage = plugin.getConfig().getString("Whitelist.Message", null);
        if (wMessage != null) {
            this.whitelistMessage = MessageUtil.translateColorCodes(wMessage);
        }

        if (plugin.useDebug()) {
            MessageUtil.sendDebugMessage("WhiteList loaded successfully", "Size: " + whiteList.size());
        }
    }

    public void saveData() {
        List<String> whiteList = new ArrayList<>();
        getWhiteListUUIDs().forEach(uuid -> {
            whiteList.add(uuid.toString());
        });
        plugin.getConfig().set("Whitelist.List", whiteList);

        List<String> streamerList = new ArrayList<>();
        getStreamerList().forEach(uuid -> {
            streamerList.add(uuid.toString());
        });
        plugin.getConfig().set("Streamer.List", streamerList);

        plugin.saveConfig();
    }

    public void registerPlayerData(Player player, PlayerData playerData) {
        playerDataMap.put(player.getUniqueId(), playerData);
    }

    public void unregisterPlayerData(Player player) {
        PlayerData playerData = getPlayerData(player);
        playerDataMap.remove(player.getUniqueId());

        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().callEvent(new PlayerDataUnLoadEvent(player, playerData));
        });
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public Set<UUID> getUUIDSets() {
        return playerDataMap.keySet();
    }

    public List<String> getPlayerFileNames() {
        File file = new File(plugin.getDataFolder() + File.separator + "PlayerData");
        String[] fileNames = file.list();
        if (fileNames != null) {
            List<String> list = new ArrayList<>(fileNames.length);
            for (String fileName : fileNames) {
                if (fileName.endsWith(".yml")) {
                    list.add(fileName.replace(".yml", ""));
                }
            }
            return list;
        }
        return GuiUtil.EMPTY_LORE;
    }

    public boolean hasWhiteList(OfflinePlayer offlinePlayer) {
        return hasWhiteList(offlinePlayer.getUniqueId());
    }

    public boolean hasWhiteList(Player player) {
        return hasWhiteList(player.getUniqueId());
    }

    public boolean hasWhiteList(UUID uuid) {
        return whiteList.contains(uuid);
    }

    public void addWhiteList(Player player) {
        addWhiteList(player.getUniqueId());
    }

    public void addWhiteList(UUID uuid) {
        whiteList.add(uuid);
    }

    public void removeWhiteList(Player player) {
        removeWhiteList(player.getUniqueId());
    }

    public void removeWhiteList(UUID uuid) {
        whiteList.remove(uuid);
    }

    public boolean isUseWhiteList() {
        return useWhiteList;
    }

    public String getWhitelistMessage() {
        return whitelistMessage;
    }

    public List<UUID> getWhiteListUUIDs() {
        return whiteList;
    }

    public void addStreamer(UUID uuid) {
        streamerList.add(uuid);
    }

    public void removeStreamer(UUID uuid) {
        streamerList.remove(uuid);
    }

    public boolean isStreamer(UUID uuid) {
        return streamerList.contains(uuid);
    }

    public List<UUID> getStreamerList() {
        return streamerList;
    }

}
