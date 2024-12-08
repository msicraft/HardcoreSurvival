package me.msicraft.hardcoresurvival.PlayerData;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.CustomHealthRegen;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerDataFile;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final HardcoreSurvival plugin;
    private final Map<UUID, PlayerData> cachedMap = new ConcurrentHashMap<>();
    private final List<UUID> streamerList = new ArrayList<>();
    private final CustomHealthRegen customHealthRegen;

    public PlayerDataManager(HardcoreSurvival plugin) {
        this.plugin = plugin;
        this.customHealthRegen = new CustomHealthRegen();

        customHealthRegen.setBase(plugin.getConfig().getDouble("Setting.CustomHealthRegen.BaseRegen", -1));
        customHealthRegen.setTaskSeconds(plugin.getConfig().getInt("Setting.CustomHealthRegen.Seconds", -1));
        customHealthRegen.setDisableVanillaRegen(plugin.getConfig().getBoolean("Setting.CustomHealthRegen.DisableVanillaRegen", false));
        customHealthRegen.setMinFoodLevel(plugin.getConfig().getInt("Setting.CustomHealthRegen.MinFoodLevel", 6));

        plugin.getConfig().getStringList("Streamer.List").forEach(s -> {
            UUID uuid = UUID.fromString(s);
            streamerList.add(uuid);
        });

        Bukkit.getScheduler().runTask(plugin, () -> {
            File file = new File(plugin.getDataFolder() + File.separator + PlayerDataFile.FOLDER_NAME);
            if (!file.exists()) {
                file.mkdirs();
            }
            String[] fileNames = file.list();
            if (fileNames != null) {
                for (String fileName : fileNames) {
                    if (fileName.endsWith(".yml")) {
                        fileName = fileName.replace(".yml", "");
                        UUID uuid = UUID.fromString(fileName);

                        PlayerData playerData = new PlayerData(uuid);
                        cachedMap.put(uuid, playerData);
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            playerData.loadData();
                            playerData.setLastCachedTime(System.currentTimeMillis());
                        });
                    }
                }
                if (plugin.useDebug()) {
                    MessageUtil.sendDebugMessage("Load-PlayerData", "Loaded " + cachedMap.size() + " PlayerData");
                }
            } else {
                if (plugin.useDebug()) {
                    MessageUtil.sendDebugMessage("Empty-PlayerData", "None PlayerData");
                }
            }
        });
    }

    public void reloadVariables() {
        FileConfiguration config = plugin.getConfig();

        customHealthRegen.setBase(config.getDouble("Setting.CustomHealthRegen.BaseRegen", -1));
        customHealthRegen.setTaskSeconds(config.getInt("Setting.CustomHealthRegen.Seconds", -1));
        customHealthRegen.setDisableVanillaRegen(config.getBoolean("Setting.CustomHealthRegen.DisableVanillaRegen", false));
        customHealthRegen.setMinFoodLevel(config.getInt("Setting.CustomHealthRegen.MinFoodLevel", 6));
    }

    public PlayerData getPlayerData(UUID uuid) {
        PlayerData playerData = null;
        if (cachedMap.containsKey(uuid)) {
            playerData = cachedMap.get(uuid);
            playerData.setLastCachedTime(System.currentTimeMillis());
        }
        return playerData;
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public PlayerData createPlayerData(UUID uuid) {
        PlayerData playerData = new PlayerData(uuid);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, playerData::loadData);
        playerData.setLastCachedTime(System.currentTimeMillis());

        cachedMap.put(uuid, playerData);

        if (plugin.useDebug()) {
            MessageUtil.sendDebugMessage("Create-PlayerData", "UUID: " + uuid);
        }
        return playerData;
    }

    public void saveData() {
        List<String> streamerList = new ArrayList<>();
        getStreamerList().forEach(uuid -> {
            streamerList.add(uuid.toString());
        });
        plugin.getConfig().set("Streamer.List", streamerList);

        plugin.saveConfig();

        Set<UUID> playerUUIDs = cachedMap.keySet();
        for (UUID uuid : playerUUIDs) {
            PlayerData playerData = cachedMap.get(uuid);
            playerData.saveData();
        }
    }

    public Set<UUID> getPlayerUUIDs() {
        return cachedMap.keySet();
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

    public CustomHealthRegen getCustomHealthRegen() {
        return customHealthRegen;
    }

}
