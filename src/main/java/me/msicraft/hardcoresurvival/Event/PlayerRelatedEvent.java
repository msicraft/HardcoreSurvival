package me.msicraft.hardcoresurvival.Event;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerRelatedEvent implements Listener {

    private static PlayerRelatedEvent instance = null;

    public static PlayerRelatedEvent getInstance() {
        if (instance == null) {
            instance = new PlayerRelatedEvent();
        }
        return instance;
    }

    private final HardcoreSurvival plugin;

    private PlayerRelatedEvent() {
        this.plugin = HardcoreSurvival.getPlugin();
    }

    private boolean disableBoneMeal = false;
    private double mendingEnchantChance = 0.0;
    private boolean disableBedExplode = false;

    public void reloadVariables() {
        FileConfiguration config = plugin.getConfig();

        this.disableBoneMeal = config.contains("Setting.DisableBoneMeal") && config.getBoolean("Setting.DisableBoneMeal");
        this.mendingEnchantChance = config.contains("Setting.MendingEnchantChance") ? config.getDouble("Setting.MendingEnchantChance") : 0.0;
        this.disableBedExplode = config.contains("Setting.DisableBedExplode") && config.getBoolean("Setting.DisableBedExplode");
    }

    @EventHandler
    public void disableBoneMeal(PlayerInteractEvent e) {
        if (disableBoneMeal) {
            if (e.getPlayer().isOp()) {
                return;
            }
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                ItemStack itemStack = e.getItem();
                if (itemStack != null && itemStack.getType() == Material.BONE_MEAL) {
                    e.setCancelled(true);

                    if (plugin.useDebug()) {
                        MessageUtil.sendDebugMessage("Disable-BoneMeal", "Player: " + e.getPlayer().getName());
                    }
                }
            }
        }
    }

    @EventHandler
    public void mendingEnchant(EnchantItemEvent e) {
        if (e.getExpLevelCost() >= 30) {
            ItemStack itemStack = e.getItem();
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                if (!itemMeta.hasEnchant(Enchantment.MENDING)) {
                    double random = Math.random();
                    boolean success = false;
                    if (random <= mendingEnchantChance) {
                        success = true;
                        itemMeta.addEnchant(Enchantment.MENDING, 1, true);
                        itemStack.setItemMeta(itemMeta);
                    }

                    if (plugin.useDebug()) {
                        MessageUtil.sendDebugMessage("Player-EnchantItem-Mending",
                                "Player: " + e.getEnchanter().getName(),
                                "성공여부: " + success,
                                "랜덤: " + random + " | " + "설정확률: " + mendingEnchantChance);
                    }
                }
            }
        }
    }

    @EventHandler
    public void disableBedExplode(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = e.getClickedBlock();
            if (block == null) {
                return;
            }
            if (block.getType().name().contains("_BED")) {
                if (disableBedExplode) {
                    World world = player.getWorld();
                    if (world.isBedWorks()) {
                        return;
                    }
                    e.setCancelled(true);
                    block.breakNaturally();

                    if (plugin.useDebug()) {
                        MessageUtil.sendDebugMessage("DisableBedExplode",
                                "Player: " + player.getName(), "World: " + world.getName());
                    }
                }
            }
        }
    }

}
