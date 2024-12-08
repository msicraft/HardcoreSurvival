package me.msicraft.hardcoresurvival.CustomItem.Data;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomItem {

    public static final NamespacedKey CUSTOM_ITEM_KEY = new NamespacedKey(HardcoreSurvival.getPlugin(), "CustomItem");

    private final String path;
    private final String id;
    private ItemStack itemStack;

    public CustomItem(String id, CustomItemDataFile customItemDataFile) {
        this.id = id;
        this.path = "CustomItem." + id;
        update(customItemDataFile);
    }

    public abstract boolean rightClick(PlayerData playerData, ItemStack useItemStack);
    public abstract boolean leftClick(PlayerData playerData, ItemStack useItemStack);

    public void update(CustomItemDataFile customItemDataFile) {
        this.itemStack = createItemStack(customItemDataFile);
    }

    public ItemStack createItemStack(CustomItemDataFile customItemDataFile) {
        FileConfiguration config = customItemDataFile.getConfig();
        String path = "CustomItem." + id;
        String materialS = config.getString(path + ".Material", "PAPER");
        Material material = Material.getMaterial(materialS.toUpperCase());
        if (material == null) {
            material = Material.PAPER;
        }
        ItemStack stack = new ItemStack(material);
        ItemMeta itemMeta = stack.getItemMeta();
        int customModelData = config.getInt(path + ".CustomModelData", -1);
        if (customModelData != -1) {
            itemMeta.setCustomModelData(customModelData);
        }
        String displayName = config.getString(path + ".DisplayName",  null);
        if (displayName != null) {
            itemMeta.displayName(Component.text(MessageUtil.translateColorCodes(displayName)));
        }
        int maxStackSize = config.getInt(path + ".MaxStackSize", -1);
        if (maxStackSize != -1) {
            itemMeta.setMaxStackSize(maxStackSize);
        }
        List<Component> lore = new ArrayList<>();
        config.getStringList(path + ".Lore").forEach(s -> {
            lore.add(Component.text(MessageUtil.translateColorCodes(s)));
        });
        itemMeta.lore(lore);
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(CUSTOM_ITEM_KEY, PersistentDataType.STRING, id);
        stack.setItemMeta(itemMeta);
        return stack;
    }

    public String getId() {
        return id;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public String getPath() {
        return path;
    }
}

