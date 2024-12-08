package me.msicraft.hardcoresurvival;

import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WorldManager {

    private final HardcoreSurvival plugin;
    private final Map<String, String> worldNameMap = new HashMap<>();

    public WorldManager(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    public void reloadVariables() {
        worldNameMap.clear();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("Setting.WorldName");
        if (section != null) {
            Set<String> worldNameKeys = section.getKeys(false);
            for (String worldName : worldNameKeys) {
                String path = "Setting.WorldName." + worldName;
                String displayName = config.getString(path, "[Unknown]");
                displayName = MessageUtil.translateColorCodes(displayName);
                worldNameMap.put(worldName, displayName);
            }
        }
    }

    public String getCurrentWorldName(String worldName) {
        if (worldNameMap.containsKey(worldName)) {
            return worldNameMap.get(worldName);
        }
        return "[Unknown]";
    }

    public String timeTo24Format(long time) {
        long hours = time / 1000 + 6;
        long minutes = (time % 1000) * 60 / 1000;
        String ampm = "AM";
        if (hours >= 12) {
            hours -= 12;
            ampm = "PM";
        }
        if (hours >= 12) {
            hours -= 12;
            ampm = "AM";
        }
        if (hours == 0) hours = 12;
        String mm = "0" + minutes;
        mm = mm.substring(mm.length() - 2);
        return hours + ":" + mm + " " + ampm;
    }

    public Location getCenterChunkLocation(String worldName, int x, int z) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        Location center = new Location(world, x << 4, 64, z << 4).add(8, 0, 8);
        center.setY(center.getWorld().getHighestBlockYAt(center) + 1);
        return center;
    }

}
