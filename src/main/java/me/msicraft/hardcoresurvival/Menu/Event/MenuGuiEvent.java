package me.msicraft.hardcoresurvival.Menu.Event;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Menu.Data.CustomGui;
import me.msicraft.hardcoresurvival.Menu.Data.GuiType;
import me.msicraft.hardcoresurvival.Menu.MenuGui;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.PlayerData.PlayerDataManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class MenuGuiEvent implements Listener {

    private final HardcoreSurvival plugin;

    public MenuGuiEvent(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void menuOpenEvent(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        ItemStack handStack = player.getInventory().getItemInMainHand();
        if (handStack != null && handStack.getType() == Material.AIR) {
            if (player.isSneaking()) {
                PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
                PlayerData playerData = playerDataManager.getPlayerData(player);
                CustomGui customGui = playerData.getCustomGui(GuiType.MAIN);
                if (customGui instanceof MenuGui menuGui) {
                    player.openInventory(menuGui.getInventory());
                }
            }
        }
    }

    @EventHandler
    public void menuGuiClickEvent(InventoryClickEvent e) {
        Inventory topInventory = e.getView().getTopInventory();
        if (topInventory.getHolder(false) instanceof MenuGui menuGui) {
            ClickType type = e.getClick();
            if (type == ClickType.NUMBER_KEY || type == ClickType.SWAP_OFFHAND
                    || type == ClickType.SHIFT_LEFT || type == ClickType.SHIFT_RIGHT) {
                e.setCancelled(true);
                return;
            }
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();
            PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
            PlayerData playerData = playerDataManager.getPlayerData(player);

            ItemStack itemStack = e.getCurrentItem();
            if (itemStack != null) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {
                    PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
                    if (dataContainer.has(new NamespacedKey(plugin, "MenuGui_Main"))) {
                        String data = dataContainer.get(new NamespacedKey(plugin, "MenuGui_Main"), PersistentDataType.STRING);
                        if (data != null) {
                            switch (data) {
                                case "item-box" -> {
                                    plugin.getItemBoxManager().openItemBox(playerData);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
