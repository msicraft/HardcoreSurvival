package me.msicraft.hardcoresurvival.Shop.Menu;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Menu.Data.CustomGui;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Shop.Data.SellItem;
import me.msicraft.hardcoresurvival.Shop.Data.ShopItem;
import me.msicraft.hardcoresurvival.Shop.ShopManager;
import me.msicraft.hardcoresurvival.Utils.GuiUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ShopGui extends CustomGui {

    public enum Type {
        BUY, SELL
    }

    private final PlayerData playerData;

    public static final NamespacedKey SELL_KEY = new NamespacedKey(HardcoreSurvival.getPlugin(), "ShopGui_Sell");
    public static final NamespacedKey BUY_KEY = new NamespacedKey(HardcoreSurvival.getPlugin(), "ShopGui_Buy");
    private final Inventory gui;
    private final HardcoreSurvival plugin;

    public ShopGui(HardcoreSurvival plugin, PlayerData playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
        this.gui = Bukkit.createInventory(this, 54, Component.text("Shop"));
    }

    public void setGui(Player player, Type type) {
        gui.clear();
        switch (type) {
            case BUY -> {
                player.openInventory(getInventory());
                setShopBuyInv(player);
            }
            case SELL -> {
                player.openInventory(getInventory());
                setShopSellInv(player);
            }
        }
    }

    public void setShopBuyInv(Player player) {
        ItemStack itemStack;
        itemStack = GuiUtil.createItemStack(Material.ARROW, "다음 페이지", GuiUtil.EMPTY_LORE, -1, BUY_KEY, "Next");
        gui.setItem(50, itemStack);
        itemStack = GuiUtil.createItemStack(Material.ARROW, "이전 페이지", GuiUtil.EMPTY_LORE, -1, BUY_KEY, "Previous");
        gui.setItem(48, itemStack);
        itemStack = GuiUtil.createItemStack(Material.CHEST, "아이템 판매", GuiUtil.EMPTY_LORE, -1, BUY_KEY, "Sell");
        gui.setItem(53, itemStack);

        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        ShopManager shopManager = plugin.getShopManager();

        List<String> internalNames = shopManager.getInternalNameList();
        int maxSize = internalNames.size();
        int page = (int) playerData.getTempData("ShopGui_Buy_Page", 0);
        int guiCount = 0;
        int lastCount = page * 45;

        String pageS = "페이지: " + (page + 1) + "/" + ((maxSize / 45) + 1);
        itemStack = GuiUtil.createItemStack(Material.BOOK, pageS, GuiUtil.EMPTY_LORE, -1, BUY_KEY, "Page");
        gui.setItem(49, itemStack);

        for (int a = lastCount; a < maxSize; a++) {
            String internalName = internalNames.get(a);
            ShopItem shopItem = shopManager.getShopItem(internalName);
            if (shopItem != null) {
                if (shopItem.getBasePrice() <= 0) {
                    continue;
                }
                int selectCount = (int) playerData.getTempData("ShopGui_" + internalName + "_SelectCount", 1);
                ItemStack cloneStack = new ItemStack(shopItem.getItemStack());
                ItemMeta itemMeta = cloneStack.getItemMeta();
                PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text(ChatColor.WHITE + "현재 가격: " + shopItem.getPrice(false)
                        + " (개당 가격: " + shopItem.getPrice(true) + ") " + shopItem.getChangePercentString()));
                if (shopItem.isUnlimitStock()) {
                    lore.add(Component.text(ChatColor.WHITE + "남은 재고: 무제한"));
                } else {
                    lore.add(Component.text(ChatColor.WHITE + "남은 재고: " + shopItem.getStock()));
                }
                lore.add(Component.text(""));
                lore.add(Component.text(ChatColor.YELLOW + "선택된 개수: " + selectCount));
                lore.add(Component.text(""));
                lore.add(Component.text(ChatColor.YELLOW + "좌 클릭: " + ChatColor.GREEN + "구매"));
                lore.add(Component.text(ChatColor.YELLOW + "우 클릭: " + ChatColor.GREEN + "수량 입력"));

                dataContainer.set(BUY_KEY, PersistentDataType.STRING, internalName);

                if (itemMeta.hasLore()) {
                    lore.add(Component.text(""));
                    lore.addAll(itemMeta.lore());
                }
                itemMeta.lore(lore);
                cloneStack.setItemMeta(itemMeta);
                gui.setItem(guiCount, cloneStack);
                guiCount++;
                if (guiCount >= 45) {
                    break;
                }
            }
        }
    }

    public void setShopSellInv(Player player) {
        //String dataTag = "ShopInventory_Sell";
        ItemStack itemStack;
        itemStack = GuiUtil.createItemStack(Material.BARRIER, "뒤로", GuiUtil.EMPTY_LORE, -1, SELL_KEY, "Back");
        gui.setItem(45, itemStack);

        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        ShopManager shopManager = plugin.getShopManager();
        int totalPrice = 0;
        SellItem[] sellItems = (SellItem[]) playerData.getTempData("ShopGui_SellItems", null);
        if (sellItems != null) {
            int size = sellItems.length;
            for (int i = 0; i < size; i++) {
                SellItem sellItem = sellItems[i];
                if (sellItem != null) {
                    ItemStack sellStack = sellItem.getItemStack();
                    if (sellStack == null || sellStack.getType() == Material.AIR) {
                        continue;
                    }
                    ShopItem shopItem = shopManager.getShopItem(sellItem.getId());
                    if (shopItem == null) {
                        continue;
                    }
                    ItemStack cloneStack = new ItemStack(sellStack);
                    ItemMeta itemMeta = cloneStack.getItemMeta();

                    int price = shopItem.getPrice(true) * cloneStack.getAmount();
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text(ChatColor.GREEN + "판매 가격: " + price));
                    itemMeta.lore(lore);
                    cloneStack.setItemMeta(itemMeta);

                    gui.setItem(i, cloneStack);
                    totalPrice = totalPrice + price;
                }
            }
        }
        itemStack = GuiUtil.createItemStack(Material.CHEST, "판매 확인",
                List.of(ChatColor.GREEN + "총 판매 가격: " + totalPrice),
                -1, SELL_KEY, "SellConfirm");
        gui.setItem(49, itemStack);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return gui;
    }
}
