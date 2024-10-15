package me.msicraft.hardcoresurvival.ItemBox;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.ItemBox.Data.ItemBoxGui;
import me.msicraft.hardcoresurvival.ItemBox.Data.ItemBoxStack;
import me.msicraft.hardcoresurvival.Menu.Data.GuiType;
import me.msicraft.hardcoresurvival.PlayerData.Data.OfflinePlayerData;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemBoxManager {

    private final HardcoreSurvival plugin;

    public ItemBoxManager(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    public void openItemBox(PlayerData playerData) {
        ItemBoxGui itemBoxGui = (ItemBoxGui) playerData.getCustomGui(GuiType.ITEM_BOX);
        itemBoxGui.open(playerData);
    }

    public void sendItemStackToItemBox(PlayerData playerData, ItemStack itemStack, String provider) {
        ItemBoxStack itemBoxStack = new ItemBoxStack(itemStack, System.currentTimeMillis(), provider);
        playerData.getItemBox().addItemBoxStack(itemBoxStack);

        if (plugin.useDebug()) {
            MessageUtil.sendDebugMessage("ItemBox-SendItemStack-Player",
                    "Player: " + playerData.getPlayer().getName() + " | Provider: " + provider);
        }
    }

    public void sendItemStackToItemBox(OfflinePlayerData offlinePlayerData, ItemStack itemStack, String provider) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }
        ItemBoxStack itemBoxStack = new ItemBoxStack(itemStack, System.currentTimeMillis(), provider);
        offlinePlayerData.getItemBox().addItemBoxStack(itemBoxStack);

        if (plugin.useDebug()) {
            MessageUtil.sendDebugMessage("ItemBox-SendItemStack-OfflinePlayer",
                    "OfflinePlayer: " + offlinePlayerData.getOfflinePlayer().getName()  + " | Provider: " + provider);
        }
    }

}
