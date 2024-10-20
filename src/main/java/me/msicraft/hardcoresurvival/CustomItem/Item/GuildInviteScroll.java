package me.msicraft.hardcoresurvival.CustomItem.Item;

import me.msicraft.hardcoresurvival.CustomItem.Data.CustomItem;
import me.msicraft.hardcoresurvival.CustomItem.File.CustomItemDataFile;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GuildInviteScroll extends CustomItem {

    public GuildInviteScroll(String id, CustomItemDataFile customItemDataFile) {
        super(id, customItemDataFile);
    }

    @Override
    public void rightClick(PlayerData playerData, ItemStack useItemStack) {
        Player player = playerData.getPlayer();
        player.sendMessage(ChatColor.GRAY + "========================================");
        player.sendMessage(ChatColor.GRAY + "초대할 플레이어 이름을 입력해주세요");
        player.sendMessage(ChatColor.GRAY + "'cancel' 입력시 취소");
        player.sendMessage(ChatColor.GRAY + "========================================");

        playerData.setTempData("GuildInviteScroll_Chat_Edit", getId());
        useItemStack.setAmount(useItemStack.getAmount() - 1);
    }

    @Override
    public void leftClick(PlayerData playerData, ItemStack useItemStack) {
    }

}