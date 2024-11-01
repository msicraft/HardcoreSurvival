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
    //private final ItemStack guiStack;
    private final long receivedTime;
    private final String provider;
    private final long expiredTime;

    public ItemBoxStack(ItemStack itemStack, long receivedTime, String provider, long expiredTime) {
        this.itemStack = itemStack;
        this.receivedTime = receivedTime;
        this.provider = provider;
        this.expiredTime = expiredTime;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemStack createGuiStack() {
        ItemStack guiStack = new ItemStack(itemStack);
        ItemMeta itemMeta = guiStack.getItemMeta();
        String expiredText;
        if (isExpired()) {
            expiredText = ChatColor.RED + "만료됨";
        } else {
            if (expiredTime == -1) {
                expiredText = ChatColor.GREEN + "제한없음";
            } else {
                expiredText = ChatColor.GOLD + TimeUtil.getTimeToFormat(expiredTime);
            }
        }
        List<Component> lore = List.of(Component.text(ChatColor.YELLOW + "좌 클릭: 받기"),
                Component.text(ChatColor.YELLOW + "우 클릭: 버리기"),
                Component.text(""),
                Component.text(ChatColor.GRAY + "받은 시간: " + TimeUtil.getTimeToFormat(receivedTime)),
                Component.text(ChatColor.GRAY + "제공자: " + ChatColor.BOLD + ChatColor.GOLD  +  provider),
                Component.text(""),
                Component.text(ChatColor.GRAY + "만료 기간: " + expiredText));
        itemMeta.lore(lore);
        guiStack.setItemMeta(itemMeta);

        return guiStack;
    }

    /*
    public long getReceivedTime() {
        return receivedTime;
    }

     */

    public static ItemBoxStack fromFormat(String format) {
        String[] a = format.split(":");
        String itemData = a[0];
        long receivedTime = Long.parseLong(a[1]);
        String provider = a[2];
        long expiredTime;
        if (a.length == 3) {
            expiredTime = -1;
        } else {
            expiredTime = Long.parseLong(a[3]);
        }
        ItemStack itemStack = ItemStack.deserializeBytes(Base64Util.stringToByteArray(itemData));
        return new ItemBoxStack(itemStack, receivedTime, provider, expiredTime);
    }

    public String toFormat() {
        return Base64Util.byteArrayToString(itemStack.serializeAsBytes()) + ":" + receivedTime + ":" + provider + ":" + expiredTime;
    }

    /*
    public long getExpiredTime() {
        return expiredTime;
    }
     */

    public boolean isExpired() {
        if (expiredTime == -1) {
            return false;
        }
        return System.currentTimeMillis() > expiredTime;
    }

    public boolean isUnlimitedExpiredTime() {
        return expiredTime == -1;
    }

}
