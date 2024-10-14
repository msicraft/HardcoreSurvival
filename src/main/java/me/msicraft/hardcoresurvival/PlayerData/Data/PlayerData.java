package me.msicraft.hardcoresurvival.PlayerData.Data;

import me.msicraft.hardcoresurvival.Menu.Data.CustomGui;
import me.msicraft.hardcoresurvival.Menu.Data.GuiType;
import me.msicraft.hardcoresurvival.DeathPenalty.Data.DeathPenaltyChestLog;
import me.msicraft.hardcoresurvival.DeathPenalty.DeathPenaltyManager;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Menu.MenuGui;
import me.msicraft.hardcoresurvival.PlayerData.File.PlayerDataFile;
import me.msicraft.hardcoresurvival.PlayerData.Task.PlayerTask;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerData {

    private final Player player;
    private final DeathPenaltyChestLog deathPenaltyChestLog;
    private final PlayerDataFile playerDataFile;

    private final Map<GuiType, CustomGui> customGuiMap = new HashMap<>();

    private final Map<String, Object> tempDataMap = new HashMap<>();
    private final Map<String, Object> dataMap = new HashMap<>();
    private final List<String> tagList = new ArrayList<>();

    private PlayerTask playerTask;

    public PlayerData(Player player) {
        this.player = player;
        this.playerDataFile = new PlayerDataFile(player);
        this.deathPenaltyChestLog = new DeathPenaltyChestLog(this);
    }

    public void updateTask(int ticks) {
        if (playerTask != null) {
            playerTask.cancel();
        }
        playerTask = new PlayerTask(player);
        playerTask.runTaskTimer(HardcoreSurvival.getPlugin(), 0, ticks);
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

        if (HardcoreSurvival.getPlugin().useDebug()) {
            MessageUtil.sendDebugMessage("PlayerData Loaded", "Player: " + player.getName());
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

        playerDataFile.saveConfig();

        if (HardcoreSurvival.getPlugin().useDebug()) {
            MessageUtil.sendDebugMessage("PlayerData Saved", "Player: " + player.getName());
        }
    }

    public CustomGui getCustomGui(GuiType guiType) {
        CustomGui customGui = null;
        if (customGuiMap.containsKey(guiType)) {
            customGui = customGuiMap.get(guiType);
        }
        if (customGui == null) {
            switch (guiType) {
                case MAIN -> {
                    customGui = new MenuGui();
                    customGuiMap.put(guiType, customGui);
                }
                default -> {
                    customGui = new MenuGui();
                    Bukkit.getConsoleSender().sendMessage(HardcoreSurvival.PREFIX + ChatColor.YELLOW + "플레이어: " + player.getName(),
                            ChatColor.YELLOW + "메뉴 생성중 기본값 사용이 발생하였습니다.");
                }
            }
        }
        return customGui;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerDataFile getPlayerDataFile() {
        return playerDataFile;
    }

    public void setTempData(String key, Object object) {
        tempDataMap.put(key, object);
    }

    public Object getTempData(String key) {
        return tempDataMap.getOrDefault(key, null);
    }

    public Object getTempData(String key, Object def) {
        Object object = getTempData(key);
        if (!hasTempData(key) || object == null) {
            return def;
        }
        return object;
    }

    public boolean hasTempData(String key) {
        return tempDataMap.containsKey(key);
    }

    public void removeTempData(String key) {
        tempDataMap.remove(key);
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

    public boolean hasTag(String key) {
        return tagList.contains(key);
    }

    public void addTag(String key) {
        tagList.add(key);
    }

    public void removeTag(String key) {
        tagList.remove(key);
    }

    public DeathPenaltyChestLog getDeathPenaltyChestLog() {
        return deathPenaltyChestLog;
    }

}
