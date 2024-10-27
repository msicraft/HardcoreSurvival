package me.msicraft.hardcoresurvival.Shop.Task;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Shop.Data.ShopRegion;
import me.msicraft.hardcoresurvival.Shop.ShopManager;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.scheduler.BukkitRunnable;

public class ShopLocationTask extends BukkitRunnable {

    private final HardcoreSurvival plugin;
    private final ShopManager shopManager;
    private int count = 0;

    public ShopLocationTask(HardcoreSurvival plugin, ShopManager shopManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;

        this.runTaskTimer(plugin, 20L, 20L);
    }

    @Override
    public void run() {
        ShopRegion shopRegion = shopManager.getShopRegion();
        if (shopRegion == null) {
            cancel();
            return;
        }
        if (shopRegion.update()) {
            cancel();
            if (HardcoreSurvival.getPlugin().useDebug()) {
                MessageUtil.sendDebugMessage("Shop CenterLocation update", "Shop region: " + shopRegion.toString());
            }
            return;
        }
        count++;
        if (count > 60) {
            cancel();
        }
    }
}
