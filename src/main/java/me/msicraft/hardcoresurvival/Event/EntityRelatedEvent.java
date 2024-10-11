package me.msicraft.hardcoresurvival.Event;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

}
