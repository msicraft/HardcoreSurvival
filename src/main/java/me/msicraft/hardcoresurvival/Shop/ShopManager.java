package me.msicraft.hardcoresurvival.Shop;

import io.lumine.mythic.bukkit.MythicBukkit;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Menu.Data.CustomGuiManager;
import me.msicraft.hardcoresurvival.Menu.Data.GuiType;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Shop.Data.ShopItem;
import me.msicraft.hardcoresurvival.Shop.File.ShopDataFile;
import me.msicraft.hardcoresurvival.Shop.Menu.ShopGui;
import me.msicraft.hardcoresurvival.Shop.Task.ShopTask;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShopManager extends CustomGuiManager {

    private final HardcoreSurvival plugin;
    private final ShopDataFile shopDataFile;

    private final Map<String, ShopItem> shopItemMap = new ConcurrentHashMap<>();
    private final List<String> internalNameList = new ArrayList<>();

    private boolean isEnabled = false;
    private int updateSeconds = 3600;
    private double maxPricePercent = 0.0;
    private double minPricePercent = 0.0;
    private double perValueChangeMaxPercent = 0.0;

    private boolean isShopMaintenance = false;
    private ShopTask shopTask = null;

    public ShopManager(HardcoreSurvival plugin) {
        this.plugin = plugin;
        this.shopDataFile = new ShopDataFile(plugin);
    }

    public void sendMaintenanceMessage(Player player) {
        player.sendMessage(ChatColor.RED + "현재 상점 가격 조정 중입니다. 잠시 후 이용해주시기 바랍니다");
    }

    public void closeShopInventory() {
        List<UUID> viewers = getViewers();
        for (UUID uuid : viewers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.closeInventory(InventoryCloseEvent.Reason.CANT_USE);
                sendMaintenanceMessage(player);
            }
        }
        removeAll();
    }

    public void openShopInventory(Player player, ShopGui.Type type) {
        if (isShopMaintenance) {
            player.closeInventory();
            sendMaintenanceMessage(player);
            return;
        }
        addViewer(player);

        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        ShopGui shopGui = (ShopGui) playerData.getCustomGui(GuiType.SHOP);
        shopGui.setGui(player, type);
    }

    public void reloadVariables() {
        shopDataFile.reloadConfig();

        setShopMaintenance(true);
        closeShopInventory();

        FileConfiguration config = shopDataFile.getConfig();

        this.isEnabled = config.getBoolean("Setting.Enable", false);
        this.updateSeconds = config.getInt("Setting.UpdateSeconds", 3600);
        this.maxPricePercent = config.getDouble("Setting.MaxPricePercent", 0.0);
        this.minPricePercent = config.getDouble("Setting.MinPricePercent", 0.0);
        this.perValueChangeMaxPercent = config.getDouble("Setting.PerValueChangeMaxPercent", 0.0);

        loadShopData();

        if (shopTask != null) {
            shopTask.cancel();
            shopTask = null;
        }
        shopTask = new ShopTask(plugin, this, updateSeconds);
        setShopMaintenance(false);
    }

    public void loadShopData() {
        FileConfiguration config = shopDataFile.getConfig();
        ConfigurationSection section = config.getConfigurationSection("Items");
        if (section != null) {
            int success = 0;
            int fail = 0;
            internalNameList.clear();
            Set<String> keys = section.getKeys(false);
            for (String key : keys) {
                String path = "Items." + key;
                ShopItem.ItemType itemType = ShopItem.ItemType.valueOf(config.getString(path + ".ItemType").toUpperCase());
                boolean useStaticPrice = config.getBoolean(path + ".UseStaticPrice", false);
                int basePrice = config.getInt(path + ".BasePrice", -1);
                int price = config.getInt(path + ".Price", -1);
                int stock = config.getInt(path + ".Stock", 0);
                ItemStack itemStack = null;
                ShopItem shopItem;
                switch (itemType) {
                    case NONE -> itemStack = config.getItemStack(path + ".ItemStack");
                    case MYTHICMOBS -> {
                        String internalName = config.getString(path + ".MythicMobsItemInternalName", null);
                        itemStack = MythicBukkit.inst().getItemManager().getItemStack(internalName);
                    }
                }
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    fail++;
                    continue;
                }
                if (shopItemMap.containsKey(key)) {
                    shopItem = shopItemMap.get(key);
                    shopItem.setItemStack(itemStack);
                    shopItem.setUseStaticPrice(useStaticPrice);
                    shopItem.setBasePrice(basePrice);
                    shopItem.setPrice(price);
                    shopItem.setStock(stock);
                } else {
                    shopItem = new ShopItem(itemType, useStaticPrice, itemStack, key, stock, basePrice, price);
                }
                shopItemMap.put(key, shopItem);
                internalNameList.add(key);
                success++;
            }

            if (plugin.useDebug()) {
                MessageUtil.sendDebugMessage("ShopData loaded", "Success: " + success + " | Fail: " + fail);
            }
        } else {
            shopItemMap.clear();
            internalNameList.clear();

            if (plugin.useDebug()) {
                MessageUtil.sendDebugMessage("ShopData Invalid");
            }
        }
    }

    public void saveShopData() {
        FileConfiguration config = shopDataFile.getConfig();
        Set<String> sets = shopItemMap.keySet();
        for (String key : sets) {
            ShopItem shopItem = shopItemMap.get(key);
            String path = "Items." + key;
            config.set(path + ".ItemType", shopItem.getItemType().name());
            config.set(path + ".UseStaticPrice", shopItem.isUseStaticPrice());
            config.set(path + ".BasePrice", shopItem.getBasePrice());
            config.set(path + ".Price", shopItem.getPrice(false));
            config.set(path + ".Stock", shopItem.getStock());
            ItemStack itemStack = shopItem.getItemStack();
            switch (shopItem.getItemType()) {
                case NONE -> {
                    config.set(path + ".ItemStack", itemStack);
                }
                case MYTHICMOBS -> {
                    String internalName = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(itemStack);
                    config.set(path + ".MythicMobsItemInternalName", internalName);
                }
            }
        }
        shopDataFile.saveConfig();

        if (plugin.useDebug()) {
            MessageUtil.sendDebugMessage("ShopData saved", "Count: " + sets.size());
        }
    }

    public ShopItem searchShopItem(ItemStack itemStack) {
        Set<String> keys = shopItemMap.keySet();
        for (String key : keys) {
            ShopItem shopItem = shopItemMap.get(key);
            if (shopItem.getItemStack().isSimilar(itemStack)) {
                return shopItem;
            }
        }
        return null;
    }

    public boolean hasInternalName(String internalName) {
        return shopItemMap.containsKey(internalName);
    }

    public ShopItem getShopItem(String internalName) {
        return shopItemMap.getOrDefault(internalName, null);
    }

    public void addShopItem(String internalName, ShopItem shopItem) {
        shopItemMap.put(internalName, shopItem);
    }

    public void removeShopItem(String internalName) {
        shopItemMap.remove(internalName);
    }

    public List<String> getInternalNameList() {
        return internalNameList;
    }

    public double getMaxPricePercent() {
        return maxPricePercent;
    }

    public double getMinPricePercent() {
        return minPricePercent;
    }

    public double getPerValueChangeMaxPercent() {
        return perValueChangeMaxPercent;
    }

    public boolean isShopMaintenance() {
        return isShopMaintenance;
    }

    public synchronized void setShopMaintenance(boolean shopMaintenance) {
        isShopMaintenance = shopMaintenance;
    }

    public ShopDataFile getShopDataFile() {
        return shopDataFile;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

}
