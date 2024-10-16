package me.msicraft.hardcoresurvival.CustomItem.Event;

import me.msicraft.hardcoresurvival.CustomItem.CustomItemManager;
import me.msicraft.hardcoresurvival.CustomItem.Data.CustomItem;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerUseCustomItem(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.HAND) {
            Action action = e.getAction();
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                ItemStack itemStack = e.getItem();
                String internalName = customItemManager.getCustomItemInternalName(itemStack);
                if (internalName != null) {
                    CustomItem customItem = customItemManager.getCustomItem(internalName);
                    if (customItem != null) {
                        Player player = e.getPlayer();
                        customItem.applyAbility(player);

                        if (plugin.useDebug()) {
                            MessageUtil.sendDebugMessage("CustomItemUse",
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
