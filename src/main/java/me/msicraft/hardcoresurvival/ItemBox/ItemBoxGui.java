package me.msicraft.hardcoresurvival.ItemBox;

import me.msicraft.hardcoresurvival.Menu.Data.CustomGui;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class ItemBoxGui extends CustomGui {

    private final Inventory gui;

    public ItemBoxGui() {
        this.gui = Bukkit.createInventory(this, 54, Component.text("아이템 우편함"));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return gui;
    }
}
