package me.msicraft.hardcoresurvival.PlayerData.Data;

import me.msicraft.hardcoresurvival.DeathPenalty.Data.DeathPenaltyChestLog;
import me.msicraft.hardcoresurvival.DeathPenalty.DeathPenaltyManager;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.ItemBox.Data.ItemBox;
import me.msicraft.hardcoresurvival.ItemBox.Data.ItemBoxStack;
import me.msicraft.hardcoresurvival.PlayerData.File.PlayerDataFile;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class OfflinePlayerData {

    private final UUID uuid;
    private final PlayerDataFile playerDataFile;

    private final Map<String, Object> dataMap = new HashMap<>();
    private final List<String> tagList = new ArrayList<>();
    private final Map<PersonalOption, Object> personalOptionMap = new HashMap<>();

    private final DeathPenaltyChestLog deathPenaltyChestLog;
    private final ItemBox itemBox;

    private long lastLogin;
    private UUID guildUUID;

    public OfflinePlayerData(UUID uuid) {
        this.uuid = uuid;
        this.playerDataFile = new PlayerDataFile(uuid);
        this.deathPenaltyChestLog = new DeathPenaltyChestLog();
        this.itemBox = new ItemBox();
    }

    public void loadData() {
        FileConfiguration playerDataConfig = playerDataFile.getConfig();

        this.lastLogin = playerDataConfig.getLong("LastLogin", System.currentTimeMillis());
        String guildUUIDS = playerDataConfig.getString("Guild.UUID", null);
        if (guildUUIDS == null) {
            MessageUtil.sendDebugMessage("Invalid Guild UUID", "Player UUID: " + uuid);
        } else {
            this.guildUUID = UUID.fromString(guildUUIDS);
        }

        List<String> tags = playerDataConfig.getStringList("Tags");
        tagList.addAll(tags);

        ConfigurationSection dataSection = playerDataConfig.getConfigurationSection("Data");
        if (dataSection != null) {
            Set<String> keys = dataSection.getKeys(false);
            for (String key : keys) {
                Object object = playerDataConfig.get("Data." + key);
                dataMap.put(key, object);
            }
        }

        List<String> itemBoxDataFormatList = playerDataConfig.getStringList("ItemBoxData");
        for (String itemBoxDataFormat : itemBoxDataFormatList) {
            ItemBoxStack itemBoxStack = ItemBoxStack.fromFormat(itemBoxDataFormat);
            itemBox.addItemBoxStack(itemBoxStack);
        }

        PersonalOption[] personalOptions = PersonalOption.values();
        for (PersonalOption option : personalOptions) {
            String path = "PersonalOption." + option.name();
            if (playerDataConfig.contains(path)) {
                switch (option) {
                    case DISPLAY_ACTIONBAR -> {
                        personalOptionMap.put(option, playerDataConfig.getBoolean(path, (boolean) option.getBaseValue()));
                    }
                }
            } else {
                setPersonalOption(option, option.getBaseValue());
            }
        }

        DeathPenaltyManager deathPenaltyManager = HardcoreSurvival.getPlugin().getDeathPenaltyManager();
        List<String> formatList = playerDataConfig.getStringList("DeathPenaltyChestLog");
        for (String format : formatList) {
            Location location = deathPenaltyManager.formatToLocation(format);
            if (location != null) {
                Block block = location.getBlock();
                if (deathPenaltyManager.isContainerMaterial(block.getType())) {
                    deathPenaltyChestLog.addLocation(location);
                }
            }
        }
    }

    public void saveData() {
        FileConfiguration playerDataConfig = playerDataFile.getConfig();

        playerDataConfig.set("LastLogin", lastLogin);
        if (guildUUID == null) {
            playerDataConfig.set("Guild", null);
        } else {
            playerDataConfig.set("Guild.UUID", guildUUID.toString());
        }

        playerDataConfig.set("Tags", tagList);

        Set<String> dataKeys = dataMap.keySet();
        for (String key : dataKeys) {
            Object value = dataMap.get(key);
            playerDataConfig.set("Data." + key, value);
        }

        List<ItemBoxStack> itemBoxStackList = itemBox.getList();
        List<String> itemBoxDataList = new ArrayList<>(itemBoxStackList.size());
        for (ItemBoxStack itemBoxStack : itemBoxStackList) {
            String format = itemBoxStack.toFormat();
            itemBoxDataList.add(format);
        }
        playerDataConfig.set("ItemBoxData", itemBoxDataList);

        PersonalOption[] personalOptions = PersonalOption.values();
        for (PersonalOption option : personalOptions) {
            Object object = personalOptionMap.get(option);
            String path = "PersonalOption." + option.name();
            switch (option) {
                case DISPLAY_ACTIONBAR -> {
                    playerDataConfig.set(path, object);
                }
            }
        }

        DeathPenaltyManager deathPenaltyManager = HardcoreSurvival.getPlugin().getDeathPenaltyManager();
        List<Location> locationList = deathPenaltyChestLog.getChestLocationList();
        List<String> chestLogList = new ArrayList<>(locationList.size());
        for (Location location : locationList) {
            String format = deathPenaltyManager.locationToFormat(location);
            chestLogList.add(format);
        }
        playerDataConfig.set("DeathPenaltyChestLog", chestLogList);

        playerDataFile.saveConfig();
    }

    public UUID getUuid() {
        return uuid;
    }

    public PlayerDataFile getPlayerDataFile() {
        return playerDataFile;
    }

    public void setData(String key, Object object) {
        dataMap.put(key, object);
    }

    public Object getData(String key, Object def) {
        if (hasData(key)) {
            return dataMap.get(key);
        } else {
            dataMap.put(key, def);
            return def;
        }
    }

    public boolean hasData(String key) {
        return dataMap.containsKey(key);
    }

    public void removeData(String key) {
        dataMap.remove(key);
    }

    public void setPersonalOption(PersonalOption personalOption, Object value) {
        personalOptionMap.put(personalOption, value);
    }

    public Object getPersonalOption(PersonalOption personalOption) {
        return personalOptionMap.get(personalOption);
    }

    public Object getPersonalOption(PersonalOption personalOption, Object def) {
        return personalOptionMap.getOrDefault(personalOption, def);
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

    public UUID getGuildUUID() {
        return guildUUID;
    }

    public void setGuildUUID(UUID guildUUID) {
        this.guildUUID = guildUUID;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public DeathPenaltyChestLog getDeathPenaltyChestLog() {
        return deathPenaltyChestLog;
    }

    public ItemBox getItemBox() {
        return itemBox;
    }

}
