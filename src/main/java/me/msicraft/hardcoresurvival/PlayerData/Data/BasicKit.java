package me.msicraft.hardcoresurvival.PlayerData.Data;

import me.msicraft.hardcoresurvival.ItemBox.Data.ItemBox;
import me.msicraft.hardcoresurvival.ItemBox.Data.ItemBoxStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class BasicKit {

    private final Set<BasicKitItemStack> basicKits = new HashSet<>();

    public BasicKit() {
        basicKits.add(new BasicKitItemStack(new ItemStack(Material.BREAD, 32), 1));
        basicKits.add(new BasicKitItemStack(new ItemStack(Material.LEATHER_CHESTPLATE, 1), 1));
        basicKits.add(new BasicKitItemStack(new ItemStack(Material.LEATHER_LEGGINGS, 1), 1));
        basicKits.add(new BasicKitItemStack(new ItemStack(Material.STONE_PICKAXE, 1), 1));
        basicKits.add(new BasicKitItemStack(new ItemStack(Material.STONE_AXE, 1), 1));
    }

    private final String key = "hs_basic_kit";
    private final int id = 1;

    public String getKey() {
        return key + "-" + id;
    }

    public void provide(PlayerData playerData) {
        if (playerData.hasTag("hs_basic_kit-1")) {
            playerData.removeTag("hs_basic_kit-1");
            playerData.setData(key, 1);
        }

        int dataID = 0;
        if (playerData.hasData(key)) {
            Object o = playerData.getData(key, id);
            dataID = (int) o;
        }
        if (dataID == id) {
            return;
        }
        playerData.setData(key, id);

        ItemBox itemBox = playerData.getItemBox();
        long time = System.currentTimeMillis();

        for (BasicKitItemStack basicKitItemStack : basicKits) {
            if (basicKitItemStack.getId() > dataID) {
                itemBox.addItemBoxStack(new ItemBoxStack(basicKitItemStack.getItemStack(), time, "[시스템]", -1));
            }
        }
    }

    public static class BasicKitItemStack {

        private final ItemStack itemStack;
        private final int id;

        public BasicKitItemStack(ItemStack itemStack, int id) {
            this.itemStack = itemStack;
            this.id = id;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public int getId() {
            return id;
        }
    }

}
