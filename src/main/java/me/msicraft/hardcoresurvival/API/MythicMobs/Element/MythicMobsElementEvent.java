package me.msicraft.hardcoresurvival.API.MythicMobs.Element;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.variables.VariableRegistry;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.element.Element;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MythicMobsElementEvent implements Listener {

    private final HardcoreSurvival plugin;
    private static MythicMobsElementEvent instance;

    public static MythicMobsElementEvent getInstance() {
        if (instance == null) {
            instance = new MythicMobsElementEvent();
        }
        return instance;
    }

    private final Map<String, String> variablesMap = new HashMap<>();

    private MythicMobsElementEvent() {
        this.plugin = HardcoreSurvival.getPlugin();

        reloadVariables();
    }

    public void reloadVariables() {
        variablesMap.clear();

        FileConfiguration config = plugin.getConfig();
        config.getStringList("Compatibility.MythicLib.MythicMobsElement.VariablesKeys").forEach(s -> {
            String[] split = s.split(":");
            variablesMap.put(split[0], split[1]);
        });
    }

    @EventHandler
    public void playerAttackEvent(PlayerAttackEvent e) {
        LivingEntity livingEntity = e.getEntity();
        if (MythicBukkit.inst().getMobManager().isMythicMob(livingEntity)) {
            ActiveMob activeMob = MythicBukkit.inst().getMobManager().getMythicMobInstance(livingEntity);
            if (activeMob != null) {
                DamageMetadata damageMetadata = e.getAttack().getDamage();
                VariableRegistry variableRegistry = activeMob.getVariables();
                Set<String> keys = variablesMap.keySet();
                for (String key : keys) {
                    if (variableRegistry.has(key)) {
                        float defensePercent = variableRegistry.getFloat(key);
                        Element element = Element.valueOf(variablesMap.get(key).toUpperCase());
                        if (element == null) {
                            continue;
                        }
                        damageMetadata.multiplicativeModifier((1-defensePercent), element);
                    }
                }
            }
        }
    }

}
