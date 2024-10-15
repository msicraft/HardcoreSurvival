package me.msicraft.hardcoresurvival.PlayerData;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class PlayerDataManager {

    private final HardcoreSurvival plugin;

    public PlayerDataManager(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

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

}
