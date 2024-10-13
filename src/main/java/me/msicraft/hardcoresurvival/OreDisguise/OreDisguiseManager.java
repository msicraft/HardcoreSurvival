package me.msicraft.hardcoresurvival.OreDisguise;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.OreDisguise.Data.OreDisguise;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OreDisguiseManager {

    private final HardcoreSurvival plugin;

    public OreDisguiseManager(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    private boolean isEnabled = false;
    private final Map<Material, OreDisguise> oreDisguiseMap = new HashMap<>();

    public void reloadVariables() {
        oreDisguiseMap.clear();
        FileConfiguration config = plugin.getConfig();

        this.isEnabled = config.contains("Setting.OreDisguise.Enabled") && config.getBoolean("Setting.OreDisguise.Enabled");

        ConfigurationSection section = config.getConfigurationSection("Setting.OreDisguise.List");
        if (section != null) {
            Set<String> keys = section.getKeys(false);
            for (String key : keys) {
                Material material = Material.getMaterial(key.toUpperCase());
                if (material == null) {
                    if (plugin.useDebug()) {
                        MessageUtil.sendDebugMessage("OreDisguise-Can't Load Material", "Material: " + key);
                    }
                    continue;
                }
                String path = "Setting.OreDisguise.List." + key;
                String internalName = config.getString(path + ".InternalName", "");
                double chance = Double.parseDouble(config.getString(path + ".Chance", "0.0"));
                OreDisguise oreDisguise = new OreDisguise(internalName, chance);
                oreDisguiseMap.put(material, oreDisguise);
            }
        }
    }

    public boolean hasOre(Block block) {
        return hasOre(block.getType());
    }

    public boolean hasOre(Material material) {
        return oreDisguiseMap.containsKey(material);
    }

    public OreDisguise getOreDisguise(Block block) {
        return getOreDisguise(block.getType());
    }

    public OreDisguise getOreDisguise(Material material) {
        return oreDisguiseMap.get(material);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

}
