package me.msicraft.hardcoresurvival.CustomItem.Event;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.msicraft.hardcoresurvival.CustomItem.CustomItemManager;
import me.msicraft.hardcoresurvival.CustomItem.Data.CustomItem;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CustomItemRelatedEvent implements Listener {

    private final HardcoreSurvival plugin;
    private final CustomItemManager customItemManager;

    public CustomItemRelatedEvent(HardcoreSurvival plugin) {
        this.plugin = plugin;
        this.customItemManager = plugin.getCustomItemManager();
    }

    @EventHandler
    public void customItemChatEdit(AsyncChatEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        String itemId = (String) playerData.getTempData("GuildInviteScroll_Chat_Edit", null);
        if (itemId != null) {
            e.setCancelled(true);
            String message = PlainTextComponentSerializer.plainText().serialize(e.message());
            if (message.equalsIgnoreCase("cancel")) {
                playerData.removeTempData("GuildInviteScroll_Chat_Edit");
                player.sendMessage(ChatColor.RED + "초대장 사용을 취소하였습니다");
                CustomItem customItem = plugin.getCustomItemManager().getCustomItem(itemId);
                if (customItem != null) {
                    player.getInventory().addItem(customItem.getItemStack());
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (plugin.useDebug()) {
                        MessageUtil.sendDebugMessage("Cancel GuildInviteScroll", "Player: " + player);
                    }
                });
                return;
            }
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(message);
            if (plugin.getGuildManager().inviteGuild(player, offlinePlayer)) {
                player.sendMessage(ChatColor.GREEN + "초대장을 사용하였습니다");

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (plugin.useDebug()) {
                        MessageUtil.sendDebugMessage("Use GuildInviteScroll", "Player: " + player, "Target: " + offlinePlayer.getName());
                    }
                });
            } else {
                CustomItem customItem = plugin.getCustomItemManager().getCustomItem(itemId);
                if (customItem != null) {
                    player.getInventory().addItem(customItem.getItemStack());
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (plugin.useDebug()) {
                        MessageUtil.sendDebugMessage("Cancel GuildInviteScroll-already exist player",
                                "Player: " + player, "Target: " + offlinePlayer.getName());
                    }
                });
            }
            playerData.removeTempData("GuildInviteScroll_Chat_Edit");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerUseCustomItem(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.HAND) {
            ItemStack itemStack = e.getItem();
            String internalName = customItemManager.getCustomItemInternalName(itemStack);
            if (internalName != null) {
                CustomItem customItem = customItemManager.getCustomItem(internalName);
                if (customItem != null) {
                    Player player = e.getPlayer();
                    PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
                    Action action = e.getAction();
                    if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                        customItem.leftClick(playerData, itemStack);

                        if (plugin.useDebug()) {
                            MessageUtil.sendDebugMessage("CustomItemUse-LeftClick",
                                    "Player: " + player.getName(), "CustomItem: " + internalName);
                        }
                    } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                        customItem.rightClick(playerData, itemStack);

                        if (plugin.useDebug()) {
                            MessageUtil.sendDebugMessage("CustomItemUse-RightClick",
                                    "Player: " + player.getName(), "CustomItem: " + internalName);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void disableCrafting(PrepareItemCraftEvent e) {
        ItemStack[] itemStacks = e.getInventory().getMatrix();
        for (ItemStack itemStack : itemStacks) {
            String internalName = customItemManager.getCustomItemInternalName(itemStack);
            if (internalName != null) {
                e.getInventory().setResult(null);
            }
        }
    }

    @EventHandler
    public void disableSmelting(FurnaceSmeltEvent e) {
        ItemStack itemStack = e.getSource();
        String internalName = customItemManager.getCustomItemInternalName(itemStack);
        if (internalName != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void disableSmelting2(FurnaceBurnEvent e) {
        ItemStack itemStack = e.getFuel();
        String internalName = customItemManager.getCustomItemInternalName(itemStack);
        if (internalName != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void disableAnvil(PrepareAnvilEvent e) {
        ItemStack[] itemStacks = e.getInventory().getStorageContents();
        for (ItemStack itemStack : itemStacks) {
            String internalName = customItemManager.getCustomItemInternalName(itemStack);
            if (internalName != null) {
                e.setResult(null);
            }
        }
    }

    @EventHandler
    public void disableEnchant(PrepareItemEnchantEvent e) {
        ItemStack[] itemStacks = e.getInventory().getStorageContents();
        for (ItemStack itemStack : itemStacks) {
            String internalName = customItemManager.getCustomItemInternalName(itemStack);
            if (internalName != null) {
                e.setCancelled(true);
            }
        }
    }

}
