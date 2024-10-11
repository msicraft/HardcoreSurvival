package me.msicraft.hardcoresurvival.DeathPenalty;

import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class DeathPenaltyChestLog {

    private final List<Location> chestLocationList = new ArrayList<>();

    public DeathPenaltyChestLog(PlayerData playerData) {
    }

    public void addLocation(Location location) {
        chestLocationList.add(location);
    }

    public void removeLocation(Location location) {
        chestLocationList.remove(location);
    }

    public boolean hasLocation(Location location) {
        return chestLocationList.contains(location);
    }

    public List<Location> getChestLocationList() {
        return chestLocationList;
    }

    public String locationToString(Location location) {
        return location.getWorld().getName() + ":" + location.getX() + ":" + location.getY() + ":" + location.getZ();
    }

    public Location formatToLocation(String format) {
        String[] split = format.split(":");
        String worldName = split[0];
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        double x = Double.parseDouble(split[1]);
        double y = Double.parseDouble(split[2]);
        double z = Double.parseDouble(split[3]);
        return new Location(world, x, y, z);
    }

}
