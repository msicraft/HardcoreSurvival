package me.msicraft.hardcoresurvival.Shop.Menu.Event;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Menu.Data.GuiType;
import me.msicraft.hardcoresurvival.Menu.MenuGui;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Shop.Data.SellItem;
import me.msicraft.hardcoresurvival.Shop.Data.ShopItem;
import me.msicraft.hardcoresurvival.Shop.Menu.ShopGui;
import me.msicraft.hardcoresurvival.Shop.ShopManager;
import me.msicraft.hardcoresurvival.Utils.GuiUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ShopGuiEvent implements Listener {

    private final HardcoreSurvival plugin;

    public ShopGuiEvent(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void shopGuiChatEdit(AsyncChatEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        String itemInternalName = (String) playerData.getTempData("ShopGui_SelectCount_Edit", null);
        if (itemInternalName != null) {
            if (plugin.getShopManager().hasInternalName(itemInternalName)) {
                e.setCancelled(true);
                String message = PlainTextComponentSerializer.plainText().serialize(e.message());
                if (message.equalsIgnoreCase("cancel")) {
                    playerData.removeTempData("ShopGui_SelectCount_Edit");
                    Bukkit.getScheduler().runTask(plugin, ()-> {
                        plugin.getShopManager().openShopInventory(player, ShopGui.Type.BUY);
                    });
                    return;
                }
                int amount = Integer.parseInt(message.replaceAll("[^0-9]", ""));
                playerData.setTempData("ShopGui_" + itemInternalName + "_SelectCount", amount);
            } else {
                player.sendMessage(ChatColor.RED + "잘못된 내부이름 데이터입니다. 관리자에게 문의해주시기바랍니다.");
            }
            playerData.removeTempData("ShopGui_SelectCount_Edit");
            Bukkit.getScheduler().runTask(plugin, ()-> {
                plugin.getShopManager().openShopInventory(player, ShopGui.Type.BUY);
            });
        }
    }

    @EventHandler
    public void shopGuiClose(InventoryCloseEvent e) {
        InventoryCloseEvent.Reason reason = e.getReason();
        if (reason == InventoryCloseEvent.Reason.OPEN_NEW) {
            return;
        }
        Inventory topInventory = e.getView().getTopInventory();
        if (topInventory.getHolder(false) instanceof ShopGui shopGui) {
            Player player = (Player) e.getPlayer();
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
            SellItem[] sellItemSlots = (SellItem[]) playerData.getTempData("ShopGui_SellItemSlot", null);
            if (sellItemSlots != null) {
                for (SellItem sellItem : sellItemSlots) {
                    if (sellItem != null) {
                        ItemStack itemStack = sellItem.getItemStack();
                        if (player.getInventory().firstEmpty() != -1) {
                            player.getInventory().addItem(itemStack);
                        } else {
                            player.getWorld().dropItem(player.getLocation(), itemStack);
                        }
                    }
                }
            }
            playerData.setTempData("ShopGui_SellItemSlot", null);

            if (reason != InventoryCloseEvent.Reason.CANT_USE) {
                ShopManager shopManager = plugin.getShopManager();
                shopManager.removeViewer(player);
            }
        }
    }

    @EventHandler
    public void shopGuiClick(InventoryClickEvent e) {
        Inventory topInventory = e.getView().getTopInventory();
        if (topInventory.getHolder(false) instanceof ShopGui shopGui) {
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
            ShopManager shopManager = plugin.getShopManager();
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            if (dataContainer.has(ShopGui.BUY_KEY)) {
                String data = dataContainer.get(ShopGui.BUY_KEY, PersistentDataType.STRING);
                if (data != null) {
                    int maxPage = shopManager.getInternalNameList().size() / 45;
                    int current = (int) playerData.getTempData("ShopGui_Buy_Page", 1);
                    switch (data) {
                        case "Next" -> {
                            int next = current + 1;
                            if (next > maxPage) {
                                next = 0;
                            }
                            playerData.setTempData("ShopGui_Buy_Page", next);
                            shopManager.openShopInventory(player, ShopGui.Type.BUY);
                        }
                        case "Previous" -> {
                            int previous = current - 1;
                            if (previous < 0) {
                                previous = maxPage;
                            }
                            playerData.setTempData("ShopGui_Buy_Page", previous);
                            shopManager.openShopInventory(player, ShopGui.Type.BUY);
                        }
                        case "Sell" -> {
                            shopManager.openShopInventory(player, ShopGui.Type.SELL);
                        }
                        case "Back" -> {
                            MenuGui menuGui = (MenuGui) playerData.getCustomGui(GuiType.MAIN);
                            player.openInventory(menuGui.getInventory());
                        }
                        case "Page" -> {
                            return;
                        }
                        default -> {
                            if (e.isLeftClick()) {
                                ShopItem shopItem = shopManager.getShopItem(data);
                                if (shopItem != null) {
                                    int amount = (int) playerData.getTempData("ShopGui_" + data + "_SelectCount", 1);
                                    //shopManager.buyShopItem(player, data, amount);
                                }
                            } else if (e.isRightClick()) {
                                player.sendMessage(ChatColor.GRAY + "========================================");
                                player.sendMessage(ChatColor.GRAY + "개수를 입력해주세요.");
                                player.sendMessage(ChatColor.GRAY + "'cancel' 입력시 취소");
                                player.sendMessage(ChatColor.GRAY + "========================================");

                                playerData.setTempData("ShopGui_SelectCount_Edit", data);
                                player.closeInventory();
                            }
                        }
                    }
                    return;
                }
            } else if (dataContainer.has(ShopGui.SELL_KEY)) {
                String data = dataContainer.get(ShopGui.SELL_KEY, PersistentDataType.STRING);
                if (data != null) {
                    switch (data) {
                        case "Back" -> {
                            shopManager.openShopInventory(player, ShopGui.Type.BUY);
                        }
                        case "SellConfirm" -> {
                            //shopManager.sellShopItem(player);
                        }
                    }
                    return;
                }
            }
            ItemStack sellInvCheckStack = shopGui.getInventory().getItem(49);
            if (sellInvCheckStack != null && sellInvCheckStack.getType() == Material.CHEST) {
                InventoryType inventoryType = e.getClickedInventory().getType();
                ItemStack selectItemStack = e.getCurrentItem();
                if (selectItemStack != null && selectItemStack.getType() != Material.AIR) {
                    SellItem[] sellItems = (SellItem[]) playerData.getTempData("ShopGui_SellItemSlot", null);
                    if (sellItems == null) {
                        sellItems = new SellItem[45];
                    }
                    int clickSlot = e.getSlot();
                    switch (inventoryType) {
                        case PLAYER -> {
                            int emptySlot = -1;
                            for (int i = 0; i<sellItems.length; i++) {
                                SellItem sellItem = sellItems[i];
                                if (sellItem == null || sellItem.getItemStack().getType() == Material.AIR) {
                                    emptySlot = i;
                                    break;
                                }
                            }
                            if (emptySlot != -1) {
                                ShopItem shopItem = shopManager.searchShopItem(selectItemStack);
                                if (shopItem != null) {
                                    SellItem sellItemSlot = new SellItem(shopItem.getId(), selectItemStack, (shopItem.getPrice(true) * selectItemStack.getAmount()));
                                    sellItems[emptySlot] = sellItemSlot;

                                    player.getInventory().setItem(clickSlot, GuiUtil.AIR_STACK);
                                } else {
                                    player.sendMessage(ChatColor.RED + "해당 아이템을 판매할 수 없습니다");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "빈 슬롯이 없습니다");
                            }
                        }
                        case CHEST -> {
                            int emptySlot = player.getInventory().firstEmpty();
                            if (emptySlot != -1) {
                                SellItem sellItem = sellItems[clickSlot];
                                if (sellItem != null) {
                                    player.getInventory().addItem(sellItem.getItemStack());
                                    sellItems[clickSlot] = null;
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "인벤토리에 빈 공간이 없습니다.");
                            }
                        }
                    }
                    playerData.setTempData("ShopGui_SellItemSlot", sellItems);
                    shopManager.openShopInventory(player, ShopGui.Type.SELL);
                }
            }
        }
    }

}
