package me.msicraft.hardcoresurvival.Menu.Event;

import me.msicraft.hardcoresurvival.Guild.Menu.GuildGui;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Menu.Data.CustomGui;
import me.msicraft.hardcoresurvival.Menu.Data.GuiType;
import me.msicraft.hardcoresurvival.Menu.MenuGui;
import me.msicraft.hardcoresurvival.PlayerData.Data.PersonalOption;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.PlayerData.PlayerDataManager;
import me.msicraft.hardcoresurvival.Shop.Menu.ShopGui;
import org.bukkit.Material;
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
                e.setCancelled(true);
                PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
                PlayerData playerData = playerDataManager.getPlayerData(player);
                CustomGui customGui = playerData.getCustomGui(GuiType.MAIN);
                if (customGui instanceof MenuGui menuGui) {
                    player.openInventory(menuGui.getInventory());
                    menuGui.setMain();
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
                    if (dataContainer.has(MenuGui.MENU_KEY)) {
                        String data = dataContainer.get(MenuGui.MENU_KEY, PersistentDataType.STRING);
                        if (data != null) {
                            switch (data) {
                                case "personal-settings" -> {
                                    player.openInventory(menuGui.getInventory());
                                    menuGui.setPersonalSettings();
                                }
                                case "item-box" -> {
                                    plugin.getItemBoxManager().openItemBox(playerData);
                                }
                                case "shop" -> {
                                    plugin.getShopManager().openShopInventory(player, ShopGui.Type.BUY);
                                }
                                case "Guild" -> {
                                    GuildGui guildGui = (GuildGui) playerData.getCustomGui(GuiType.GUILD);
                                    player.openInventory(guildGui.getInventory());
                                    guildGui.setMain();
                                }
                            }
                        }
                    } else if (dataContainer.has(MenuGui.PERSONAL_SETTINGS_KEY)) {
                        String data = dataContainer.get(MenuGui.PERSONAL_SETTINGS_KEY, PersistentDataType.STRING);
                        if (data != null) {
                            switch (data) {
                                case "Back" -> {
                                    player.openInventory(menuGui.getInventory());
                                    menuGui.setMain();
                                }
                                default -> {
                                    PersonalOption personalOption = PersonalOption.valueOf(data);
                                    switch (personalOption) {
                                        case DISPLAY_ACTIONBAR -> {
                                            if (e.isLeftClick()) {
                                                boolean b = (boolean) playerData.getPersonalOption(personalOption);
                                                if (b) {
                                                    playerData.setPersonalOption(personalOption, false);
                                                } else {
                                                    playerData.setPersonalOption(personalOption, true);
                                                }
                                            } else if (e.isRightClick()) {
                                                playerData.setPersonalOption(personalOption, personalOption.getBaseValue());
                                            }
                                        }
                                    }
                                    player.openInventory(menuGui.getInventory());
                                    menuGui.setPersonalSettings();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
