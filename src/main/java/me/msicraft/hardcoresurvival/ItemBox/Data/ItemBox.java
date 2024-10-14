package me.msicraft.hardcoresurvival.ItemBox.Data;

import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ItemBox {

    private final PlayerData playerData;
    private final List<ItemBoxStack> list = new ArrayList<>();

    public ItemBox(PlayerData playerData) {
        this.playerData = playerData;
    }

    public void addItemBoxStack(ItemBoxStack itemBoxStack) {
        list.add(itemBoxStack);
    }

    public boolean removeItemBoxStack(int index) {
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

    public boolean receiveItemBoxStack(int index) {
        if (index >= 0 && index < list.size()) {
            ItemBoxStack itemBoxStack = list.get(index);
            Player player = playerData.getPlayer();
            int slot = player.getInventory().firstEmpty();
            if (slot != -1) {
                player.getInventory().addItem(itemBoxStack.getItemStack());
                list.remove(index);
                return true;
            }
            return false;
        }
        return false;
    }

}
