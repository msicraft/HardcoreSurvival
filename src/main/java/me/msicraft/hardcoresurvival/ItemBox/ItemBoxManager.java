package me.msicraft.hardcoresurvival.ItemBox;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.ItemBox.Data.ItemBoxStack;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemBoxManager {

    private final HardcoreSurvival plugin;

    public ItemBoxManager(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    public void sendItemStackToItemBox(ItemStack itemStack, Player targetPlayer) {
        ItemBoxStack itemBoxStack = new ItemBoxStack(itemStack, System.currentTimeMillis(), "[시스템]");
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(targetPlayer);
        playerData.getItemBox().addItemBoxStack(itemBoxStack);
    }

}
