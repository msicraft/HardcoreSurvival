package me.msicraft.hardcoresurvival.Menu;

import me.msicraft.hardcoresurvival.Menu.Data.CustomGui;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Utils.GuiUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MenuGui extends CustomGui {

    private final Inventory gui;

    public MenuGui() {
        this.gui = Bukkit.createInventory(this, 54, Component.text("Menu"));
        setMain();
    }

    public void setMain() {
        NamespacedKey namespacedKey = new NamespacedKey(HardcoreSurvival.getPlugin(), "MenuGui_Main");
        ItemStack itemStack;
        itemStack = GuiUtil.createItemStack(Material.CHEST, "아이템 우편함", GuiUtil.EMPTY_LORE, -1,
                namespacedKey, "item-box");
        gui.setItem(10, itemStack);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return gui;
    }

}
