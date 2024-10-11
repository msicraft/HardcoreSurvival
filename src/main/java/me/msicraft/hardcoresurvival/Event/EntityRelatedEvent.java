package me.msicraft.hardcoresurvival.Event;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class EntityRelatedEvent implements Listener {

    private static EntityRelatedEvent instance = null;

    public static EntityRelatedEvent getInstance() {
        if (instance == null) {
            instance = new EntityRelatedEvent();
        }
        return instance;
    }

    private final HardcoreSurvival plugin;

    private EntityRelatedEvent() {
        this.plugin = HardcoreSurvival.getPlugin();
    }

    private boolean disableEntityVehicleEnter = false;

    public void reloadVariables() {
        FileConfiguration config = plugin.getConfig();

        disableEntityVehicleEnter = config.getBoolean("Setting.DisableEntityVehicleEnter");
    }

    @EventHandler
    public void entityVehicleEvent(VehicleEnterEvent e) {
        if (disableEntityVehicleEnter) {
            Entity entity = e.getEntered();
            if (entity instanceof LivingEntity livingEntity) {
                if (livingEntity.getType() == EntityType.PLAYER) {
                    return;
                }
                e.setCancelled(true);

                if (plugin.useDebug()) {
                    MessageUtil.sendDebugMessage("Disable-EntityVehicle", "Entity: " + livingEntity.getType().name());
                }
            }
        }
    }

    @EventHandler
    public void disableEnchantTrade(TradeSelectEvent e) {
        int index = e.getIndex();
        MerchantRecipe merchantRecipe = e.getMerchant().getRecipe(index);
        ItemStack resultItemStack = merchantRecipe.getResult();
        if (resultItemStack.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) resultItemStack.getItemMeta();
            if (enchantmentStorageMeta.hasStoredEnchant(Enchantment.MENDING)) {
                e.setCancelled(true);
                Player player = (Player) e.getWhoClicked();
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "해당 거래를 사용할 수 없습니다");

                if (plugin.useDebug()) {
                    MessageUtil.sendDebugMessage("DisableMendingTrade", "Player: " + player.getName());
                }
            }
        }
    }

}
