package me.msicraft.hardcoresurvival.PlayerData;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class PlayerDataManager {

    private final HardcoreSurvival plugin;

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final List<UUID> whiteList = new ArrayList<>();

    private boolean useWhiteList = false;
    private String whitelistMessage = ChatColor.RED + "접속 권한이 없습니다";

    public PlayerDataManager(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    public void reloadVariables() {
        this.useWhiteList = plugin.getConfig().contains("Whitelist.Enabled") && plugin.getConfig().getBoolean("Whitelist.Enabled");
        String wMessage = plugin.getConfig().getString("Whitelist.Message", null);
        if (wMessage != null) {
            this.whitelistMessage = MessageUtil.translateColorCodes(wMessage);
        }
        whiteList.clear();

        List<String> uuidList = plugin.getConfig().getStringList("Whitelist.List");
        for (String uuidString : uuidList) {
            UUID uuid = UUID.fromString(uuidString);
            whiteList.add(uuid);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (hasWhiteList(player)) {
                continue;
            }
            player.kick(Component.text(whitelistMessage));
        }

        if (plugin.useDebug()) {
            MessageUtil.sendDebugMessage("WhiteList loaded successfully", "Size: " + whiteList.size());
        }
    }

    public void registerPlayerData(Player player) {
        PlayerData playerData = new PlayerData(player);
        playerDataMap.put(player.getUniqueId(), playerData);

        if (plugin.useDebug()) {
            MessageUtil.sendDebugMessage("PlayerData Registered", "Player: " + player.getName());
        }
    }

    public void unregisterPlayerData(Player player) {
        playerDataMap.remove(player.getUniqueId());

        if (plugin.useDebug()) {
            MessageUtil.sendDebugMessage("PlayerData UnRegistered", "Player: " + player.getName());
        }
    }

    public PlayerData getPlayerData(Player player) {
        return playerDataMap.getOrDefault(player.getUniqueId(), new PlayerData(player));
    }

    public Set<UUID> getUUIDSets() {
        return playerDataMap.keySet();
    }

    public List<String> getPlayerFileNames() {
        List<String> list = new ArrayList<>();
        File file = new File(plugin.getDataFolder() + File.separator + "PlayerData");
        String[] fileNames = file.list();
        if (fileNames != null) {
            for (String fileName : fileNames) {
                if (fileName.endsWith(".yml")) {
                    list.add(fileName.replace(".yml", ""));
                }
            }
        }
        return list;
    }

    public boolean hasWhiteList(Player player) {
        return whiteList.contains(player.getUniqueId());
    }

    public void addWhiteList(Player player) {
        whiteList.add(player.getUniqueId());
    }

    public void removeWhiteList(Player player) {
        whiteList.remove(player.getUniqueId());
    }

    public boolean isUseWhiteList() {
        return useWhiteList;
    }

    public String getWhitelistMessage() {
        return whitelistMessage;
    }

    public List<UUID> getWhiteList() {
        return whiteList;
    }

}
