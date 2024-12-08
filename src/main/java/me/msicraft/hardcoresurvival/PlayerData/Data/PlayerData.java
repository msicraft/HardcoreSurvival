package me.msicraft.hardcoresurvival.PlayerData.Data;

import me.msicraft.hardcoresurvival.API.CustomEvent.PlayerDataLoadEndEvent;
import me.msicraft.hardcoresurvival.API.CustomEvent.PlayerDataLoadStartEvent;
import me.msicraft.hardcoresurvival.DeathPenalty.Data.DeathPenaltyChestLog;
import me.msicraft.hardcoresurvival.DeathPenalty.DeathPenaltyManager;
import me.msicraft.hardcoresurvival.Guild.Menu.GuildGui;
import me.msicraft.hardcoresurvival.Guild.Menu.GuildRegionGui;
import me.msicraft.hardcoresurvival.Guild.Menu.GuildRegionOptionGui;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.ItemBox.Data.ItemBox;
import me.msicraft.hardcoresurvival.ItemBox.Data.ItemBoxStack;
import me.msicraft.hardcoresurvival.ItemBox.Menu.ItemBoxGui;
import me.msicraft.hardcoresurvival.Menu.Data.CustomGui;
import me.msicraft.hardcoresurvival.Menu.Data.GuiType;
import me.msicraft.hardcoresurvival.Menu.MenuGui;
import me.msicraft.hardcoresurvival.PlayerData.Task.CombatTask;
import me.msicraft.hardcoresurvival.PlayerData.Task.PlayerTask;
import me.msicraft.hardcoresurvival.Shop.Menu.ShopGui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {

    private long lastCachedTime;

    private String lastName;
    private final UUID uuid;
    private final PlayerDataFile playerDataFile;

    private OfflinePlayer offlinePlayer;

    private final Map<String, Object> dataMap = new ConcurrentHashMap<>();
    private final Map<String, Object> tempDataMap = new ConcurrentHashMap<>();
    private final Set<String> tags = ConcurrentHashMap.newKeySet();
    private final Map<PersonalOption, Object> personalOptionMap = new ConcurrentHashMap<>();

    private final DeathPenaltyChestLog deathPenaltyChestLog;
    private final ItemBox itemBox;

    private long lastLogin;
    private UUID guildUUID;

    private final Map<GuiType, CustomGui> customGuiMap = new HashMap<>();
    private PlayerTask playerTask;
    private CombatTask combatTask;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        this.playerDataFile = new PlayerDataFile(uuid);
        this.deathPenaltyChestLog = new DeathPenaltyChestLog();
        this.itemBox = new ItemBox();

        this.lastCachedTime = System.currentTimeMillis();
    }

    public void loadData() {
        Bukkit.getScheduler().runTask(HardcoreSurvival.getPlugin(), () -> {
            Bukkit.getPluginManager().callEvent(new PlayerDataLoadStartEvent(offlinePlayer, this));
        });

        FileConfiguration playerDataConfig = playerDataFile.getConfig();

        setLastName(playerDataConfig.getString("LastName", offlinePlayer.getName()));
        setLastLogin(playerDataConfig.getLong("LastLogin", System.currentTimeMillis()));

        String guildUUIDS = playerDataConfig.getString("GuildUUID", null);
        if (guildUUIDS != null) {
            setGuildUUID(UUID.fromString(guildUUIDS));
        }

        List<String> tags = playerDataConfig.getStringList("Tags");
        for (String s : tags) {
            addTag(s);
        }

        ConfigurationSection dataSection = playerDataConfig.getConfigurationSection("Data");
        if (dataSection != null) {
            Set<String> keys = dataSection.getKeys(false);
            for (String key : keys) {
                Object object = playerDataConfig.get("Data." + key);
                setData(key, object);
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
                        setPersonalOption(option, playerDataConfig.getBoolean(path, (boolean) option.getBaseValue()));
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

        Bukkit.getScheduler().runTask(HardcoreSurvival.getPlugin(), () -> {
            Bukkit.getPluginManager().callEvent(new PlayerDataLoadEndEvent(offlinePlayer, this));
        });
    }

    public void saveData() {
        FileConfiguration playerDataConfig = playerDataFile.getConfig();

        playerDataConfig.set("LastName", getLastName());
        playerDataConfig.set("LastLogin", getLastLogin());

        UUID guildUUID = getGuildUUID();
        if (guildUUID == null) {
            playerDataConfig.set("GuildUUID", null);
        } else {
            playerDataConfig.set("GuildUUID", getGuildUUID().toString());
        }

        playerDataConfig.set("Tags", List.copyOf(tags));

        playerDataConfig.set("Data", null);
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
        Set<Location> locationList = deathPenaltyChestLog.getChestLocationSets();
        List<String> chestLogList = new ArrayList<>(locationList.size());
        for (Location location : locationList) {
            String format = deathPenaltyManager.locationToFormat(location);
            chestLogList.add(format);
        }
        playerDataConfig.set("DeathPenaltyChestLog", chestLogList);

        playerDataFile.saveConfig();
    }

    public boolean backup() {
        return false;
    }

    public CustomGui getCustomGui(GuiType guiType) {
        CustomGui customGui = null;
        if (customGuiMap.containsKey(guiType)) {
            customGui = customGuiMap.get(guiType);
        }
        if (customGui == null) {
            switch (guiType) {
                case MAIN -> {
                    customGui = new MenuGui(this);
                    customGuiMap.put(guiType, customGui);
                }
                case ITEM_BOX -> {
                    customGui = new ItemBoxGui(this);
                    customGuiMap.put(guiType, customGui);
                }
                case SHOP -> {
                    customGui = new ShopGui(HardcoreSurvival.getPlugin(), this);
                    customGuiMap.put(guiType, customGui);
                }
                case GUILD -> {
                    customGui = new GuildGui(HardcoreSurvival.getPlugin(), this);
                    customGuiMap.put(guiType, customGui);
                }
                case GUILD_REGION -> {
                    customGui = new GuildRegionGui(HardcoreSurvival.getPlugin(), this);
                    customGuiMap.put(guiType, customGui);
                }
                case GUILD_REGION_OPTIONS -> {
                    customGui = new GuildRegionOptionGui(HardcoreSurvival.getPlugin(), this);
                    customGuiMap.put(guiType, customGui);
                }
                default -> {
                    customGui = new MenuGui(this);
                    Bukkit.getConsoleSender().sendMessage(HardcoreSurvival.PREFIX + ChatColor.YELLOW + "플레이어: " + offlinePlayer.getName(),
                            ChatColor.YELLOW + "메뉴 생성중 기본값 사용이 발생하였습니다.");
                }
            }
        }
        return customGui;
    }

    public void updateTask(int ticks) {
        if (playerTask != null) {
            playerTask.cancel();
        }
        playerTask = new PlayerTask(this);
        playerTask.runTaskTimer(HardcoreSurvival.getPlugin(), 20L, ticks);
    }

    public Player getPlayer() {
        return offlinePlayer.getPlayer();
    }

    public OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    public void setOfflinePlayer(OfflinePlayer offlinePlayer) {
        this.offlinePlayer = offlinePlayer;
    }

    public void setTempData(String key, Object object) {
        if (object == null) {
            removeTempData(key);
            return;
        }
        tempDataMap.put(key, object);
    }

    public Object getTempData(String key, Object def) {
        if (hasTempData(key)) {
            return tempDataMap.get(key);
        }
        if (def != null) {
            tempDataMap.put(key, def);
        }
        return def;
    }

    public Object getTempData(String key) {
        if (hasTempData(key)) {
            return tempDataMap.get(key);
        }
        return null;
    }

    public boolean hasTempData(String key) {
        return tempDataMap.containsKey(key);
    }

    public void removeTempData(String key) {
        tempDataMap.remove(key);
    }

    public void resetTempData() {
        tempDataMap.clear();
    }

    public void setData(String key, Object object) {
        if (object == null) {
            removeData(key);
            return;
        }
        dataMap.put(key, object);
    }

    public Object getData(String key, Object def) {
        if (hasData(key)) {
            return dataMap.get(key);
        }
        if (def != null) {
            setData(key, def);
        }
        return def;
    }

    public void removeData(String key) {
        dataMap.remove(key);
    }

    public boolean hasData(String key) {
        return dataMap.containsKey(key);
    }

    public void setCombatTask(CombatTask combatTask) {
        this.combatTask = combatTask;
    }

    public boolean isInCombat() {
        return this.combatTask != null;
    }

    public void updateCombat() {
        if (isInCombat()) {
            combatTask.update();
        } else {
            combatTask = new CombatTask(this);
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public PlayerDataFile getPlayerDataFile() {
        return playerDataFile;
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

    public boolean hasTag(String key) {
        return tags.contains(key);
    }

    public void addTag(String key) {
        tags.add(key);
    }

    public void removeTag(String key) {
        tags.remove(key);
    }

    public Set<String> getTags() {
        return tags;
    }

    public UUID getGuildUUID() {
        return guildUUID;
    }

    public synchronized void setGuildUUID(UUID guildUUID) {
        this.guildUUID = guildUUID;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public synchronized void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public DeathPenaltyChestLog getDeathPenaltyChestLog() {
        return deathPenaltyChestLog;
    }

    public ItemBox getItemBox() {
        return itemBox;
    }

    public long getLastCachedTime() {
        return lastCachedTime;
    }

    public synchronized void setLastCachedTime(long lastCachedTime) {
        this.lastCachedTime = lastCachedTime;
    }

    public String getLastName() {
        return lastName;
    }

    public synchronized void setLastName(String lastName) {
        this.lastName = lastName;
    }

}
