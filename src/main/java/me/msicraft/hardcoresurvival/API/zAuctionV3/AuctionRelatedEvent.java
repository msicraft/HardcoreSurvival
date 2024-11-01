package me.msicraft.hardcoresurvival.API.zAuctionV3;

import fr.maxlego08.zauctionhouse.api.AuctionItem;
import fr.maxlego08.zauctionhouse.api.event.events.AuctionItemExpireEvent;
import fr.maxlego08.zauctionhouse.api.event.events.AuctionPostBuyEvent;
import fr.maxlego08.zauctionhouse.api.event.events.AuctionRetrieveEvent;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.ItemBox.Data.ItemBoxStack;
import me.msicraft.hardcoresurvival.PlayerData.Data.OfflinePlayerData;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class AuctionRelatedEvent implements Listener {

    private final HardcoreSurvival plugin;;

    public AuctionRelatedEvent(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void auctionRetrieve(AuctionRetrieveEvent e) {
        Player player = e.getPlayer();
        AuctionItem auctionItem = e.getAuctionItem();
        ItemStack itemStack = auctionItem.getItemStack();
        if (itemStack.getType() == Material.AIR) {
            return;
        }
        ItemStack clone = itemStack.clone();
        auctionItem.getItemStack().setAmount(0);

        int emptySlot = player.getInventory().firstEmpty();
        if (emptySlot == -1) {
            player.sendMessage(ChatColor.RED + "인벤토리에 빈공간이 없어서 회수한 아이템이 바닥에 떨어졌습니다");
            player.getWorld().dropItem(player.getLocation(), clone);
        } else {
            player.getInventory().addItem(clone);
        }
    }

    @EventHandler
    public void auctionItemExpired(AuctionItemExpireEvent e) {
        AuctionItem auctionItem = e.getAuctionItem();
        ItemStack itemStack = auctionItem.getItemStack();
        if (itemStack.getType() == Material.AIR) {
            return;
        }
        ItemStack clone = itemStack.clone();
        itemStack.setAmount(0);

        OfflinePlayer offlinePlayer = auctionItem.getSeller();
        UUID ownerUUID = auctionItem.getSellerUniqueId();
        long expiredTime = System.currentTimeMillis() + (1000L * 172800);
        ItemBoxStack itemBoxStack = new ItemBoxStack(clone, System.currentTimeMillis(), "[경매장]", expiredTime);
        if (offlinePlayer.isOnline()) {
            Player onlinePlayer = offlinePlayer.getPlayer();
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(ownerUUID);
            playerData.getItemBox().addItemBoxStack(itemBoxStack);
            onlinePlayer.sendMessage(Component.text(ChatColor.GREEN + "경매장에서 만료된 아이템이 아이템우편함으로 전송되었습니다"));
        } else {
            OfflinePlayerData offlinePlayerData = new OfflinePlayerData(ownerUUID);
            offlinePlayerData.loadData();
            offlinePlayerData.getItemBox().addItemBoxStack(itemBoxStack);
            offlinePlayerData.saveData();
        }
    }

    @EventHandler
    public void auctionBuy(AuctionPostBuyEvent e) {
        Player player = e.getPlayer();
        AuctionItem auctionItem = e.getAuctionItem();
        ItemStack itemStack = auctionItem.getItemStack();
        if (itemStack.getType() == Material.AIR) {
            return;
        }
        ItemStack clone = itemStack.clone();
        auctionItem.getItemStack().setAmount(0);

        int emptySlot = player.getInventory().firstEmpty();
        if (emptySlot == -1) {
            player.sendMessage(ChatColor.RED + "인벤토리에 빈공간이 없어서 구매한 아이템이 바닥에 떨어졌습니다");
            player.getWorld().dropItem(player.getLocation(), clone);
        } else {
            player.getInventory().addItem(clone);
        }
    }

}
