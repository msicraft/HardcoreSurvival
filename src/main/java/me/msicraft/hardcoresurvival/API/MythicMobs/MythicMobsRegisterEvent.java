package me.msicraft.hardcoresurvival.API.MythicMobs;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent;
import me.msicraft.hardcoresurvival.API.MythicMobs.CustomConditions.HasMMOItems;
import me.msicraft.hardcoresurvival.API.MythicMobs.CustomDrop.CustomItemDrop;
import me.msicraft.hardcoresurvival.API.MythicMobs.CustomDrop.OraxenItemDrop;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicMobsRegisterEvent implements Listener {

    private final HardcoreSurvival plugin;

    public MythicMobsRegisterEvent(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void registerDrop(MythicDropLoadEvent e) {
        MythicLineConfig mlc = e.getConfig();
        String dropName = e.getDropName();
        switch (dropName) {
            case "hscustomitem" -> {
                e.register(new CustomItemDrop(plugin, mlc));
            }
            case "oraxenitem" -> {
                e.register(new OraxenItemDrop(plugin, mlc));
            }
        }
    }

    @EventHandler
    public void registerCondition(MythicConditionLoadEvent e) {
        MythicLineConfig mlc = e.getConfig();
        String conditionName = e.getConditionName();
        switch (conditionName) {
            case "hasmmoitems" -> {
                e.register(new HasMMOItems(plugin, mlc));
            }
        }
    }

}
