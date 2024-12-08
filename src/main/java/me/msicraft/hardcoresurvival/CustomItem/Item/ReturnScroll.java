package me.msicraft.hardcoresurvival.CustomItem.Item;

import me.msicraft.hardcoresurvival.CustomItem.Data.CustomItem;
import me.msicraft.hardcoresurvival.CustomItem.Data.CustomItemDataFile;
import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ReturnScroll extends CustomItem {

    public ReturnScroll(String id, CustomItemDataFile customItemDataFile) {
        super(id, customItemDataFile);
    }

    @Override
    public boolean rightClick(PlayerData playerData, ItemStack useItemStack) {
        Player player = playerData.getPlayer();
        if (player == null) {
            return false;
        }
        if (playerData.isInCombat()) {
            player.sendMessage(ChatColor.RED + "전투중에는 사용 불가능합니다");
            return false;
        }
        Location spawnLocation = HardcoreSurvival.getPlugin().getShopManager().getShopRegion().getCenterLocation();
        if (spawnLocation == null) {
            player.sendMessage(ChatColor.RED + "스폰위치가 존재하지 않습니다. 관리자에게 문의해주시기 바랍니다");
            return false;
        }
        if (!spawnLocation.getWorld().getName().equals(player.getWorld().getName())) {
            player.sendMessage(ChatColor.RED + "같은 월드내에서만 이동가능합니다");
            return false;
        }
        player.teleport(spawnLocation);
        useItemStack.setAmount(useItemStack.getAmount() - 1);
        return true;
    }

    @Override
    public boolean leftClick(PlayerData playerData, ItemStack useItemStack) {
        Player player = playerData.getPlayer();
        if (player == null) {
            return false;
        }
        if (playerData.isInCombat()) {
            player.sendMessage(ChatColor.RED + "전투중에는 사용 불가능합니다");
            return false;
        }
        UUID guildUUID = playerData.getGuildUUID();
        if (guildUUID == null) {
            player.sendMessage(ChatColor.RED + "길드가 없습니다");
            return false;
        }
        Guild guild = HardcoreSurvival.getPlugin().getGuildManager().getGuild(guildUUID);
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "길드가 존재하지 않습니다 {" + guildUUID.toString() + "}");
            return false;
        }
        Location guildSpawnLocation = guild.getGuildRegion().getGuildSpawnLocation().getSpawnLocation();
        if (guildSpawnLocation == null) {
            player.sendMessage(ChatColor.RED + "길드 스폰위치가 존재하지 않습니다");
            return false;
        }
        if (!guildSpawnLocation.getWorld().getName().equals(player.getWorld().getName())) {
            player.sendMessage(ChatColor.RED + "같은 월드내에서만 이동가능합니다");
            return false;
        }
        if (!HardcoreSurvival.getPlugin().getGuildManager().isInOwnGuildRegion(guildSpawnLocation, player, true)) {
            player.sendMessage(ChatColor.RED + "길드 스폰지점의 땅이 만료된 상태입니다.");
            return false;
        }
        player.teleport(guildSpawnLocation);
        useItemStack.setAmount(useItemStack.getAmount() - 1);
        return true;
    }

}
