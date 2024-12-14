package me.msicraft.hardcoresurvival.ItemBox.Data;

import org.bukkit.ChatColor;
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

    public void takeAll(Player player) {
        synchronized (list) {
            for (ItemBoxStack itemBoxStack : list) {
                if (itemBoxStack.isExpired()) {
                    continue;
                }
                int slot = player.getInventory().firstEmpty();
                if (slot == -1) {
                    player.sendMessage(ChatColor.RED + "인벤토리에 충분한 공간이 없습니다");
                    return;
                }
                player.getInventory().setItem(slot, itemBoxStack.getItemStack());
            }
            list.clear();
        }
    }

}
