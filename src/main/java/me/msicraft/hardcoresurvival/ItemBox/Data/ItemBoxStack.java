package me.msicraft.hardcoresurvival.ItemBox.Data;

import me.msicraft.hardcoresurvival.Utils.Base64Util;
import me.msicraft.hardcoresurvival.Utils.TimeUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemBoxStack {

    private final ItemStack itemStack;
    private final ItemStack guiStack;
    private final long receivedTime;
    private final String provider;

    public ItemBoxStack(ItemStack itemStack, long receivedTime, String provider) {
        this.itemStack = itemStack;
        this.receivedTime = receivedTime;
        this.provider = provider;

        guiStack = new ItemStack(itemStack);
        ItemMeta itemMeta = guiStack.getItemMeta();
        List<Component> lore = List.of(Component.text(ChatColor.YELLOW + "좌 클릭: 받기"),
                Component.text(ChatColor.YELLOW + "우 클릭: 버리기"), Component.text(""),
                Component.text(ChatColor.GRAY + "받은 시간: " + TimeUtil.getTimeToFormat(receivedTime)),
                Component.text(ChatColor.GRAY + "제공자: " + ChatColor.BOLD + ChatColor.GOLD  +  provider));
        itemMeta.lore(lore);

        guiStack.setItemMeta(itemMeta);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemStack getGuiStack() {
        return guiStack;
    }

    public long getReceivedTime() {
        return receivedTime;
    }

    public static ItemBoxStack fromFormat(String format) {
        String[] a = format.split(":");
        String itemData = a[0];
        long receivedTime = Long.parseLong(a[1]);
        String provider = a[2];
        ItemStack itemStack = ItemStack.deserializeBytes(Base64Util.stringToByteArray(itemData));
        return new ItemBoxStack(itemStack, receivedTime, provider);
    }

    public String toFormat() {
        return Base64Util.byteArrayToString(itemStack.serializeAsBytes()) + ":" + receivedTime + ":" + provider;
    }

}
