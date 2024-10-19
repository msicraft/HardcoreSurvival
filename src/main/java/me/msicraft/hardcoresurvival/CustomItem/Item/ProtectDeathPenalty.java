package me.msicraft.hardcoresurvival.CustomItem.Item;

import me.msicraft.hardcoresurvival.CustomItem.Data.CustomItem;
import me.msicraft.hardcoresurvival.CustomItem.File.CustomItemDataFile;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ProtectDeathPenalty extends CustomItem {

    public ProtectDeathPenalty(String id, CustomItemDataFile customItemDataFile) {
        super(id, customItemDataFile);
    }

    @Override
    public void rightClick(PlayerData playerData, ItemStack useItemStack) {
        Player player = playerData.getPlayer();
        if (playerData.isInCombat()) {
            player.sendMessage(ChatColor.RED + "전투중에는 사용 불가능합니다");
            return;
        }
        if (playerData.isIgnoreDeathPenalty()) {
            player.sendMessage(ChatColor.RED + "이미 죽은 패널티 면역이 적용중인 상태입니다");
            return;
        }
        long checkTime = playerData.getLastIgnoreDeathPenaltyTime() + (3600 * 1000L);
        long time = System.currentTimeMillis();
        if (checkTime > time) {
            player.sendMessage(ChatColor.RED + "재사용 대기시간이 지나지 않았습니다");
            player.sendMessage(ChatColor.RED + "남은시간: " + ((checkTime - time) / 1000) + "초");
            return;
        }

        playerData.setIgnoreDeathPenalty(true);
        player.sendMessage(ChatColor.GREEN + "죽음 패널티 면역이 적용되었습니다");
        useItemStack.setAmount(useItemStack.getAmount() - 1);
    }

    @Override
    public void leftClick(PlayerData playerData, ItemStack useItemStack) {
    }
}
