package me.msicraft.hardcoresurvival.Shop.Task;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Shop.Data.ShopItem;
import me.msicraft.hardcoresurvival.Shop.ShopManager;
import me.msicraft.hardcoresurvival.Utils.MathUtil;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class ShopTask extends BukkitRunnable {

    private final HardcoreSurvival plugin;
    private final ShopManager shopManager;
    private final int updateSeconds;
    private int maintenanceSeconds;
    private boolean isMaintenance = false;
    private boolean isUpdate = false;

    private final double updatePercent = 0.25;
    private final int minUpdateSeconds;
    private final int maxUpdateSeconds;

    private int seconds;

    public ShopTask(HardcoreSurvival plugin, ShopManager shopManager, int updateSeconds) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.updateSeconds = updateSeconds;

        this.minUpdateSeconds = (int) (updateSeconds * (1 - updatePercent));
        this.maxUpdateSeconds = (int) (updateSeconds * (1 + updatePercent));

        this.seconds = MathUtil.getRangeRandomInt(minUpdateSeconds, maxUpdateSeconds);

        this.maintenanceSeconds = 10;
        if (maintenanceSeconds > seconds) {
            maintenanceSeconds = (int) (updateSeconds * 0.2);
        }

        if (plugin.useDebug()) {
            MessageUtil.sendDebugMessage("ShopTask-Init",
                    "Random Seconds: " + seconds, "UpdateSeconds: " + updateSeconds, "MaintenanceSeconds: " + maintenanceSeconds);
        }

        this.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    @Override
    public void run() {
        seconds--;
        if (!isMaintenance && seconds <= maintenanceSeconds) {
            if (plugin.useDebug()) {
                MessageUtil.sendDebugMessage("ShopTask", "Start-Maintenance",
                        "Seconds: " + seconds, "MaintenanceSeconds: " + maintenanceSeconds);
            }

            Bukkit.getScheduler().runTask(plugin, shopManager::closeShopInventory);
            shopManager.setShopMaintenance(true);
            isMaintenance = true;
            isUpdate = true;
            return;
        }

        if (isUpdate) {
            int success = 0;
            int fail = 0;
            List<String> internalNameList = shopManager.getInternalNameList();
            for (String internalName : internalNameList) {
                ShopItem shopItem = shopManager.getShopItem(internalName);
                if (shopItem != null) {
                    shopItem.updatePrice(shopManager);
                    success++;
                } else {
                    fail++;
                }
            }

            isUpdate = false;

            if (plugin.useDebug()) {
                MessageUtil.sendDebugMessage("ShopTask-UpdatePrice", "Success: " + success + " | Fail: " + fail);
            }
        }

        if (seconds <= 0) {
            seconds = MathUtil.getRangeRandomInt(minUpdateSeconds, maxUpdateSeconds);

            shopManager.setShopMaintenance(false);
            isMaintenance = false;

            Bukkit.getServer().broadcast(Component.text(ChatColor.BOLD + "" + ChatColor.GOLD + "상점가격이 조정되었습니다"));

            if (plugin.useDebug()) {
                MessageUtil.sendDebugMessage("ShopTask-End-Maintenance",
                        "Random Seconds: " + seconds, "UpdateSeconds: " + updateSeconds, "MaintenanceSeconds: " + maintenanceSeconds);
            }
        }
    }

}
