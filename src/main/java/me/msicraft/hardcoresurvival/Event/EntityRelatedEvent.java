package me.msicraft.hardcoresurvival.Event;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

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
    private boolean disableTrade = false;
    private double breedChance = 0;

    public void reloadVariables() {
        FileConfiguration config = plugin.getConfig();

        this.disableEntityVehicleEnter = config.contains("Setting.DisableEntityVehicleEnter") && config.getBoolean("Setting.DisableEntityVehicleEnter");
        this.disableTrade = config.contains("Setting.DisableTrade") && config.getBoolean("Setting.DisableTrade");
        this.breedChance = config.contains("Setting.BreedChance") ? config.getDouble("Setting.BreedChance") : 0;
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
    public void disableTradeEvent(TradeSelectEvent e) {
        if (disableTrade) {
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "거래를 사용할 수 없습니다");

            if (plugin.useDebug()) {
                MessageUtil.sendDebugMessage("DisableTrade", "Player: " + player.getName());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void disableBreedEvent(EntityBreedEvent e) {
        double random = Math.random();
        if (random <= breedChance) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void disableEntityPortal(EntityPortalEnterEvent e) {
        if (e.getEntityType() == EntityType.PLAYER) {
            return;
        }
        e.setCancelled(true);
    }

}
