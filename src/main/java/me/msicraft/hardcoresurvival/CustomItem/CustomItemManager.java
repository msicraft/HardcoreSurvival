package me.msicraft.hardcoresurvival.CustomItem;

import me.msicraft.hardcoresurvival.CustomItem.Data.CustomItem;
import me.msicraft.hardcoresurvival.CustomItem.File.CustomItemDataFile;
import me.msicraft.hardcoresurvival.CustomItem.Item.ReturnScroll;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CustomItemManager {

    private final HardcoreSurvival plugin;
    private final CustomItemDataFile customItemDataFile;

    public CustomItemManager(HardcoreSurvival plugin) {
        this.plugin = plugin;
        this.customItemDataFile = new CustomItemDataFile(plugin);
    }

    private final Map<String, CustomItem> customItemMap = new HashMap<>();

    public void reloadVariables() {
        customItemDataFile.reloadConfig();

        FileConfiguration config = customItemDataFile.getConfig();
        ConfigurationSection section = config.getConfigurationSection("CustomItem");
        if (section != null) {
            int success = 0;
            int fail = 0;
            Set<String> internalNames = section.getKeys(false);
            for (String internalName : internalNames) {
                CustomItem customItem = null;
                if (customItemMap.containsKey(internalName)) {
                    customItem = customItemMap.get(internalName);
                    customItem.update(customItemDataFile);
                } else {
                    switch (internalName) {
                        case "ReturnScroll" -> customItem = new ReturnScroll(internalName, customItemDataFile);
                    }
                }
                if (customItem != null) {
                    customItemMap.put(internalName, customItem);
                    success++;
                } else {
                    fail++;
                }
            }
            if (plugin.useDebug()) {
                MessageUtil.sendDebugMessage("CustomItem-Registered",
                        "Success: " + success, "Fail: " + fail);
            }
        } else {
            customItemMap.clear();
        }
    }

    public String getCustomItemInternalName(ItemStack itemStack) {
        if (itemStack != null && itemStack.getType() != Material.AIR) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            if (dataContainer.has(CustomItem.CUSTOM_ITEM_KEY)) {
                return dataContainer.get(CustomItem.CUSTOM_ITEM_KEY, PersistentDataType.STRING);
            }
        }
        return null;
    }

    public Set<String> getInternalNames() {
        return customItemMap.keySet();
    }

    public boolean hasCustomItem(String internalName) {
        return customItemMap.containsKey(internalName);
    }

    public CustomItem getCustomItem(String internalName) {
        return customItemMap.getOrDefault(internalName, null);
    }

}
