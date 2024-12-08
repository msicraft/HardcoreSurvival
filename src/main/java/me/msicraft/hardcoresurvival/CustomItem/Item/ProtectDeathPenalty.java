package me.msicraft.hardcoresurvival.CustomItem.Item;

import me.msicraft.hardcoresurvival.CustomItem.Data.CustomItem;
import me.msicraft.hardcoresurvival.CustomItem.Data.CustomItemDataFile;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ProtectDeathPenalty extends CustomItem {

    private int coolDown;

    public ProtectDeathPenalty(String id, CustomItemDataFile customItemDataFile) {
        super(id, customItemDataFile);
        this.coolDown = customItemDataFile.getConfig().getInt(getPath() + ".CoolDown", 7200);
    }

    @Override
    public void update(CustomItemDataFile customItemDataFile) {
        super.update(customItemDataFile);
        this.coolDown = customItemDataFile.getConfig().getInt(getPath() + ".CoolDown", 7200);
    }

    @Override
    public boolean rightClick(PlayerData playerData, ItemStack useItemStack) {
        Player player = playerData.getPlayer();
        if (playerData.isInCombat()) {
            player.sendMessage(ChatColor.RED + "전투중에는 사용 불가능합니다");
            return false;
        }
        if ((boolean) playerData.getData("IgnoreDeathPenalty", false)) {
            player.sendMessage(ChatColor.RED + "이미 죽음 패널티 면역이 적용중인 상태입니다");
            return false;
        }
        long t = this.coolDown * 1000L;
        long checkTime = (long) playerData.getData("LastIgnoreDeathPenaltyTime", (System.currentTimeMillis() - t - 1000)) + (this.coolDown * 1000L);
        long time = System.currentTimeMillis();
        if (checkTime > time) {
            player.sendMessage(ChatColor.RED + "재사용 대기시간이 지나지 않았습니다");
            player.sendMessage(ChatColor.RED + "남은시간: " + ((checkTime - time) / 1000) + "초");
            return false;
        }
        playerData.setData("IgnoreDeathPenalty", true);
        player.sendMessage(ChatColor.GREEN + "죽음 패널티 면역이 적용되었습니다");
        useItemStack.setAmount(useItemStack.getAmount() - 1);
        return true;
    }

    @Override
    public boolean leftClick(PlayerData playerData, ItemStack useItemStack) {
        return true;
    }
}
