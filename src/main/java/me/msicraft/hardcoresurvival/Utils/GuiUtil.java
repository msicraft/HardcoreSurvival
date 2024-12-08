package me.msicraft.hardcoresurvival.Utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GuiUtil {

    public static final ItemStack AIR_STACK = new ItemStack(Material.AIR, 1);
    public static final List<String> EMPTY_LORE = Collections.emptyList();

    public static ItemStack createItemStack(Material material, String name, List<String> list, int customModelData, String dataTag, String data) {
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(Component.text(name));
        List<Component> l = new ArrayList<>();
        for (String s : list) {
            l.add(Component.text(s));
        }
        itemMeta.lore(l);
        if (customModelData != -1) {
            itemMeta.setCustomModelData(customModelData);
        }
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(new NamespacedKey(HardcoreSurvival.getPlugin(), dataTag), PersistentDataType.STRING, data);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack createItemStack(Material material, String name, List<String> list, int customModelData, String dataTag, String data, ItemFlag[] flags) {
        ItemStack itemStack = createItemStack(material, name, list, customModelData, dataTag, data);
        ItemMeta itemMeta = itemStack.getItemMeta();
        for (ItemFlag flag : flags) {
            itemMeta.addItemFlags(flag);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack createItemStack(Material material, String name, List<String> list, int customModelData, NamespacedKey key, String data) {
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(Component.text(name));
        List<Component> l = new ArrayList<>();
        for (String s : list) {
            l.add(Component.text(s));
        }
        itemMeta.lore(l);
        if (customModelData != -1) {
            itemMeta.setCustomModelData(customModelData);
        }
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(key, PersistentDataType.STRING, data);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack createItemStack(Material material, String name, List<String> list, int customModelData, NamespacedKey key, String data, ItemFlag[] flags) {
        ItemStack itemStack = createItemStack(material, name, list, customModelData, key, data);
        ItemMeta itemMeta = itemStack.getItemMeta();
        for (ItemFlag flag : flags) {
            itemMeta.addItemFlags(flag);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack createItemStack(String textureValue, String name, List<String> list, int customModelData, NamespacedKey key, String data) {
        ItemStack itemStack = createItemStack(Material.PLAYER_HEAD, name, list, customModelData, key, data);
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "CustomHead");
        profile.setProperty(new ProfileProperty("textures", textureValue));
        skullMeta.setPlayerProfile(profile);
        itemStack.setItemMeta(skullMeta);
        return itemStack;
    }

}
