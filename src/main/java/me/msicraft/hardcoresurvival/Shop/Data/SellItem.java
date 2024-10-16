package me.msicraft.hardcoresurvival.Shop.Data;

import org.bukkit.inventory.ItemStack;

public class SellItem {

    private final String id;
    private final ItemStack itemStack;
    private final double totalPrice;

    public SellItem(String id, ItemStack itemStack, double totalPrice) {
        this.id = id;
        this.itemStack = itemStack;
        this.totalPrice = totalPrice;
    }

    public String getId() {
        return id;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

}
