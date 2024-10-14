package me.msicraft.hardcoresurvival.ItemBox.Data;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Menu.Data.CustomGui;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.GuiUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemBoxGui extends CustomGui {

    public static final NamespacedKey SELECT_KEY = new NamespacedKey(HardcoreSurvival.getPlugin(), "ItemBoxGui_Select");

    private final Inventory gui;

    public ItemBoxGui() {
        this.gui = Bukkit.createInventory(this, 54, Component.text("아이템 우편함"));
    }

    public void open(PlayerData playerData) {
        playerData.getPlayer().openInventory(getInventory());
        gui.clear();
        setSelectGui(playerData);
    }

    private void setSelectGui(PlayerData playerData) {
        ItemStack itemStack;
        itemStack = GuiUtil.createItemStack(Material.ARROW, "다음 페이지", GuiUtil.EMPTY_LORE, -1,
                SELECT_KEY, "Next");
        gui.setItem(48, itemStack);
        itemStack = GuiUtil.createItemStack(Material.ARROW, "이전 페이지", GuiUtil.EMPTY_LORE, -1,
                SELECT_KEY, "Previous");
        gui.setItem(50, itemStack);
        itemStack = GuiUtil.createItemStack(Material.BARRIER, "뒤로", GuiUtil.EMPTY_LORE, -1,
                SELECT_KEY, "Back");
        gui.setItem(45, itemStack);

        List<ItemBoxStack> itemBoxList = playerData.getItemBox().getList();
        int maxSize = itemBoxList.size();
        int page = (int) playerData.getTempData("ItemBox_Select_Page",0);
        int guiCount = 0;
        int lastCount = page * 45;

        String pageS = "페이지: " + (page + 1) + "/" + ((maxSize / 45) + 1);
        itemStack = GuiUtil.createItemStack(Material.BOOK, pageS, GuiUtil.EMPTY_LORE, -1,
                SELECT_KEY, "Page");
        gui.setItem(49, itemStack);

        for (int a = lastCount; a <maxSize; a++) {
            ItemBoxStack itemBoxStack = itemBoxList.get(a);
            ItemStack guiStack = itemBoxStack.getGuiStack();
            ItemMeta itemMeta = guiStack.getItemMeta();
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            dataContainer.set(SELECT_KEY, PersistentDataType.STRING, String.valueOf(a));
            guiStack.setItemMeta(itemMeta);

            gui.setItem(guiCount, guiStack);
            guiCount++;
            if (guiCount >= 45) {
                break;
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return gui;
    }

}
