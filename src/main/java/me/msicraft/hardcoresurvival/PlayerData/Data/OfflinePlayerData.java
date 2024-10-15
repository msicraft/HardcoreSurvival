package me.msicraft.hardcoresurvival.PlayerData.Data;

import me.msicraft.hardcoresurvival.DeathPenalty.Data.DeathPenaltyChestLog;
import me.msicraft.hardcoresurvival.DeathPenalty.DeathPenaltyManager;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.ItemBox.Data.ItemBox;
import me.msicraft.hardcoresurvival.ItemBox.Data.ItemBoxStack;
import me.msicraft.hardcoresurvival.PlayerData.File.PlayerDataFile;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class OfflinePlayerData {

    private final OfflinePlayer offlinePlayer;
    private final PlayerDataFile playerDataFile;

    private final Map<String, Object> dataMap = new HashMap<>();
    private final List<String> tagList = new ArrayList<>();

    private final DeathPenaltyChestLog deathPenaltyChestLog;
    private final ItemBox itemBox;

    public OfflinePlayerData(OfflinePlayer offlinePlayer) {
        this.offlinePlayer = offlinePlayer;
        this.playerDataFile = new PlayerDataFile(offlinePlayer);
        this.deathPenaltyChestLog = new DeathPenaltyChestLog();
        this.itemBox = new ItemBox();
    }

    public void loadData() {
        FileConfiguration playerDataConfig = playerDataFile.getConfig();

        List<String> tags = playerDataConfig.getStringList("Tags");
        tagList.addAll(tags);

        ConfigurationSection dataSection = playerDataConfig.getConfigurationSection("Data");
        if (dataSection!= null) {
            Set<String> keys = dataSection.getKeys(false);
            for (String key : keys) {
                Object object = playerDataConfig.get("Data." + key);
                dataMap.put(key, object);
            }
        }

        DeathPenaltyManager deathPenaltyManager = HardcoreSurvival.getPlugin().getDeathPenaltyManager();
        playerDataConfig.getStringList("DeathPenaltyChestLog").forEach(format -> {
            Location location = deathPenaltyManager.formatToLocation(format);
            if (location != null) {
                Block block = location.getBlock();
                if (deathPenaltyManager.isContainerMaterial(block.getType())) {
                    deathPenaltyChestLog.addLocation(location);
                }
            }
        });

        playerDataConfig.getStringList("ItemBoxData").forEach(itemBoxDataFormat -> {
            ItemBoxStack itemBoxStack = ItemBoxStack.fromFormat(itemBoxDataFormat);
            itemBox.addItemBoxStack(itemBoxStack);
        });

        if (HardcoreSurvival.getPlugin().useDebug()) {
            MessageUtil.sendDebugMessage("PlayerData Loaded", "Player: " + offlinePlayer.getName());
        }
    }

    public void saveData() {
        FileConfiguration playerDataConfig = playerDataFile.getConfig();

        playerDataConfig.set("Tags", tagList);

        Set<String> dataKeys = dataMap.keySet();
        for (String key : dataKeys) {
            Object value = dataMap.get(key);
            playerDataConfig.set("Data." + key, value);
        }

        DeathPenaltyManager deathPenaltyManager = HardcoreSurvival.getPlugin().getDeathPenaltyManager();
        List<String> chestLogList = new ArrayList<>();
        deathPenaltyChestLog.getChestLocationList().forEach(location -> {
            String format = deathPenaltyManager.locationToFormat(location);
            chestLogList.add(format);
        });
        playerDataConfig.set("DeathPenaltyChestLog", chestLogList);

        List<String> itemBoxDataList = new ArrayList<>();
        itemBox.getList().forEach(itemBoxStack -> {
            String format = itemBoxStack.toFormat();
            itemBoxDataList.add(format);
        });
        playerDataConfig.set("ItemBoxData", itemBoxDataList);

        playerDataFile.saveConfig();

        if (HardcoreSurvival.getPlugin().useDebug()) {
            MessageUtil.sendDebugMessage("PlayerData Saved", "Player: " + offlinePlayer.getName());
        }
    }

    public OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    public PlayerDataFile getPlayerDataFile() {
        return playerDataFile;
    }

    public void setData(String key, Object object) {
        dataMap.put(key, object);
    }

    public Object getData(String key) {
        return dataMap.getOrDefault(key, null);
    }

    public Object getData(String key, Object def) {
        Object object = getData(key);
        if (!hasData(key) || object == null) {
            return def;
        }
        return object;
    }

    public boolean hasData(String key) {
        return dataMap.containsKey(key);
    }

    public void removeData(String key) {
        dataMap.remove(key);
    }

    public Set<String> getDataKeySet() {
        return dataMap.keySet();
    }

    public boolean hasTag(String key) {
        return tagList.contains(key);
    }

    public void addTag(String key) {
        tagList.add(key);
    }

    public void removeTag(String key) {
        tagList.remove(key);
    }

    public List<String> getTagList() {
        return tagList;
    }

    public DeathPenaltyChestLog getDeathPenaltyChestLog() {
        return deathPenaltyChestLog;
    }

    public ItemBox getItemBox() {
        return itemBox;
    }

}
