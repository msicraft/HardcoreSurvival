package me.msicraft.hardcoresurvival.ItemBox.Menu.Event;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.ItemBox.Data.ItemBox;
import me.msicraft.hardcoresurvival.ItemBox.Menu.ItemBoxGui;
import me.msicraft.hardcoresurvival.Menu.Data.GuiType;
import me.msicraft.hardcoresurvival.Menu.MenuGui;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemBoxGuiEvent implements Listener {

    private final HardcoreSurvival plugin;

    public ItemBoxGuiEvent(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void partyMenuClickEvent(InventoryClickEvent e) {
        Inventory topInventory = e.getView().getTopInventory();
        if (topInventory.getHolder(false) instanceof ItemBoxGui itemBoxGui) {
            ClickType type = e.getClick();
            if (type == ClickType.NUMBER_KEY || type == ClickType.SWAP_OFFHAND
                    || type == ClickType.SHIFT_LEFT || type == ClickType.SHIFT_RIGHT) {
                e.setCancelled(true);
                return;
            }
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();
            ItemStack itemStack = e.getCurrentItem();
            if (itemStack == null) {
                return;
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                return;
            }
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            if (dataContainer.has(ItemBoxGui.SELECT_KEY)) {
                String data = dataContainer.get(ItemBoxGui.SELECT_KEY, PersistentDataType.STRING);
                if (data != null) {
                    int maxPage = playerData.getItemBox().getList().size() / 45;
                    int current = (int) playerData.getTempData("ItemBox_Select_Page", 1);
                    switch (data) {
                        case "Next" -> {
                            int next = current + 1;
                            if (next > maxPage) {
                                next = 0;
                            }
                            playerData.setTempData("ItemBox_Select_Page", next);
                            itemBoxGui.open(playerData);
                        }
                        case "Previous" -> {
                            int previous = current - 1;
                            if (previous < 0) {
                                previous = maxPage;
                            }
                            playerData.setTempData("ItemBox_Select_Page", previous);
                            itemBoxGui.open(playerData);
                        }
                        case "Page" -> {
                        }
                        case "Back" -> {
                            MenuGui menuGui = (MenuGui) playerData.getCustomGui(GuiType.MAIN);
                            player.openInventory(menuGui.getInventory());
                        }
                        case "TakeAll" -> {
                            ItemBox itemBox = playerData.getItemBox();
                            itemBox.takeAll(player);

                            player.closeInventory();
                            player.sendMessage(ChatColor.GREEN + "우편함으로부터 아이템을 받았습니다");
                        }
                        default -> {
                            ItemBox itemBox = playerData.getItemBox();
                            int index = Integer.parseInt(data);
                            if (e.isLeftClick()) {
                                if (itemBox.receiveItemBoxStack(index, player)) {
                                    player.sendMessage(ChatColor.GREEN + "우편함으로부터 해당 아이템을 받았습니다");
                                } else {
                                    player.sendMessage(ChatColor.RED + "인벤토리에 빈 공간이 없거나, 만료된 아이템입니다");
                                }
                                itemBoxGui.open(playerData);
                            } else if (e.isRightClick()) {
                                if (itemBox.removeItemBoxStack(index)) {
                                    player.sendMessage(ChatColor.GREEN + "우편함에서 해당 아이템을 삭제하였습니다");
                                } else {
                                    player.sendMessage(ChatColor.RED + "아이템을 찾을 수 없습니다");
                                }
                                itemBoxGui.open(playerData);
                            }
                        }
                    }
                }
            }
        }
    }

}
