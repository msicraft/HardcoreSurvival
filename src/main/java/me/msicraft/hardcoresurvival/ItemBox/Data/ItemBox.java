package me.msicraft.hardcoresurvival.ItemBox.Data;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ItemBox {

    private final List<ItemBoxStack> list = new ArrayList<>();

    public ItemBox() {
    }

    public synchronized void addItemBoxStack(ItemBoxStack itemBoxStack) {
        list.add(itemBoxStack);
    }

    public synchronized boolean removeItemBoxStack(int index) {
        if (index >= 0 && index < list.size()) {
            list.remove(index);
            return true;
        }
        return false;
    }

    public List<ItemBoxStack> getList() {
        return list;
    }

    public ItemBoxStack getItemBoxStack(int index) {
        return list.get(index);
    }

    public boolean receiveItemBoxStack(int index, Player player) {
        if (index >= 0 && index < list.size()) {
            ItemBoxStack itemBoxStack = list.get(index);
            if (itemBoxStack.isExpired()) {
                return false;
            }
            int slot = player.getInventory().firstEmpty();
            if (slot != -1) {
                player.getInventory().addItem(itemBoxStack.getItemStack());
                removeItemBoxStack(index);
                return true;
            }
            return false;
        }
        return false;
    }

}
